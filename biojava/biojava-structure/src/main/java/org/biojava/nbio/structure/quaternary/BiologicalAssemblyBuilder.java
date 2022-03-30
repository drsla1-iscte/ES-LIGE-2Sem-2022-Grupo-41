/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.nbio.structure.quaternary;

import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.EntityInfo;
import org.biojava.nbio.structure.Structure;
import org.rcsb.cif.schema.mm.PdbxStructAssembly;
import org.rcsb.cif.schema.mm.PdbxStructAssemblyGen;
import org.rcsb.cif.schema.mm.PdbxStructOperList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import java.util.*;

/**
 * Reconstructs the quaternary structure of a protein from an asymmetric unit
 *
 * @author Peter Rose
 * @author Andreas Prlic
 * @author Jose Duarte
 *
 */
public class BiologicalAssemblyBuilder {

	private static final Logger logger = LoggerFactory.getLogger(BiologicalAssemblyBuilder.class);

	/**
	 * The character separating the original chain identifier from the operator id.
	 */
	public static final String SYM_CHAIN_ID_SEPARATOR = "_";

	/**
	 * The character separating operator ids that are composed.
	 */
	public static final String COMPOSED_OPERATOR_SEPARATOR = "x";

	private OperatorResolver operatorResolver;


	/**
	 * All matrix operators present in _pdbx_struct_oper_list.
	 * Identifiers (_pdbx_struct_oper_list.id) to matrix operators.
	 */
	private Map<String, Matrix4d> allTransformations;

	private List<String> modelIndex = new ArrayList<>();

	public BiologicalAssemblyBuilder(){
		init();
	}

	/**
	 * Builds a Structure object containing the quaternary structure built from given asymUnit and transformations,
	 * by adding symmetry partners as new models.
	 * The output Structure will be different depending on the multiModel parameter:
	 * <li>
	 * the symmetry-expanded chains are added as new models, one per transformId. All original models but
	 * the first one are discarded.
	 * </li>
	 * <li>
	 * as original with symmetry-expanded chains added with renamed chain ids and names (in the form
	 * originalAsymId_transformId and originalAuthId_transformId)
	 * </li>
	 * @param asymUnit
	 * @param transformations
	 * @param useAsymIds if true use {@link Chain#getId()} to match the ids in the BiologicalAssemblyTransformation (needed if data read from mmCIF),
	 * if false use {@link Chain#getName()} for the chain matching (needed if data read from PDB).
	 * @param multiModel if true the output Structure will be a multi-model one with one transformId per model,
	 * if false the outputStructure will be as the original with added chains with renamed asymIds (in the form originalAsymId_transformId and originalAuthId_transformId).
	 * @return
	 */
	public Structure rebuildQuaternaryStructure(Structure asymUnit, List<BiologicalAssemblyTransformation> transformations, boolean useAsymIds, boolean multiModel) {

		// ensure that new chains are build in the same order as they appear in the asymmetric unit
		orderTransformationsByChainId(asymUnit, transformations);

		Structure s = asymUnit.clone();

		Map<Integer, EntityInfo> entityInfoMap = new HashMap<>();
		// this resets all models (not only the first one): this is important for NMR (multi-model)
		// like that we can be sure we start with an empty structures and we add models or chains to it
		s.resetModels();
		s.setEntityInfos(new ArrayList<>());

		for (BiologicalAssemblyTransformation transformation : transformations){

			List<Chain> chainsToTransform = new ArrayList<>();

			// note: for NMR structures (or any multi-model) we use the first model only and throw away the rest
			if (useAsymIds) {
				Chain c = asymUnit.getChain(transformation.getChainId());
				chainsToTransform.add(c);
			} else {
				Chain polyC = asymUnit.getPolyChainByPDB(transformation.getChainId());
				List<Chain> nonPolyCs = asymUnit.getNonPolyChainsByPDB(transformation.getChainId());
				Chain waterC = asymUnit.getWaterChainByPDB(transformation.getChainId());
				if (polyC!=null)
					chainsToTransform.add(polyC);
				if (!nonPolyCs.isEmpty())
					chainsToTransform.addAll(nonPolyCs);
				if (waterC!=null)
					chainsToTransform.add(waterC);
			}

			for (Chain c: chainsToTransform) {

				Chain chain = (Chain)c.clone();

				Calc.transform(chain, transformation.getTransformationMatrix());

				String transformId = transformation.getId();

				// note that the Structure.addChain/Structure.addModel methods set the parent reference to the new Structure

				if (multiModel)
					addChainMultiModel(s, chain, transformId);
				else
					addChainFlattened(s, chain, transformId);

				EntityInfo entityInfo;
				if (!entityInfoMap.containsKey(chain.getEntityInfo().getMolId())) {
					entityInfo = new EntityInfo(chain.getEntityInfo());
					entityInfoMap.put(chain.getEntityInfo().getMolId(), entityInfo);
					s.addEntityInfo(entityInfo);
				} else {
					entityInfo = entityInfoMap.get(chain.getEntityInfo().getMolId());
				}
				chain.setEntityInfo(entityInfo);
				entityInfo.addChain(chain);

			}
		}

		s.setBiologicalAssembly(true);
		return s;
	}

	/**
	 * Orders model transformations by chain ids in the same order as in the asymmetric unit
	 * @param asymUnit
	 * @param transformations
	 */
	private void orderTransformationsByChainId(Structure asymUnit, List<BiologicalAssemblyTransformation> transformations) {
		final List<String> chainIds = getChainIds(asymUnit);
		Collections.sort(transformations, new Comparator<BiologicalAssemblyTransformation>() {
			@Override
			public int compare(BiologicalAssemblyTransformation t1, BiologicalAssemblyTransformation t2) {
				// set sort order only if the two ids are identical
				if (t1.getId().equals(t2.getId())) {
					 return chainIds.indexOf(t1.getChainId()) - chainIds.indexOf(t2.getChainId());
				} else {
					return t1.getId().compareTo(t2.getId());
				}
		    }
		});
	}

	/**
	 * Returns a list of chain ids in the order they are specified in the ATOM
	 * records in the asymmetric unit
	 * @param asymUnit
	 * @return
	 */
	private List<String> getChainIds(Structure asymUnit) {
		List<String> chainIds = new ArrayList<String>();
		for ( Chain c : asymUnit.getChains()){
			String intChainID = c.getId();
			chainIds.add(intChainID);
		}
		return chainIds;
	}

	/**
	 * Adds a chain to the given structure to form a biological assembly,
	 * adding the symmetry expanded chains as new models per transformId.
	 * @param s
	 * @param newChain
	 * @param transformId
	 */
	private void addChainMultiModel(Structure s, Chain newChain, String transformId) {

		// multi-model bioassembly

		if ( modelIndex.size() == 0)
			modelIndex.add("PLACEHOLDER FOR ASYM UNIT");

		int modelCount = modelIndex.indexOf(transformId);
		if ( modelCount == -1)  {
			modelIndex.add(transformId);
			modelCount = modelIndex.indexOf(transformId);
		}

		if (modelCount == 0) {
			s.addChain(newChain);
		} else if (modelCount > s.nrModels()) {
			List<Chain> newModel = new ArrayList<>();
			newModel.add(newChain);
			s.addModel(newModel);
		} else {
			s.addChain(newChain, modelCount-1);
		}

	}

	/**
	 * Adds a chain to the given structure to form a biological assembly,
	 * adding the symmetry-expanded chains as new chains with renamed
	 * chain ids and names (in the form originalAsymId_transformId and originalAuthId_transformId).
	 * @param s
	 * @param newChain
	 * @param transformId
	 */
	private void addChainFlattened(Structure s, Chain newChain, String transformId) {
		newChain.setId(newChain.getId()+SYM_CHAIN_ID_SEPARATOR+transformId);
		newChain.setName(newChain.getName()+SYM_CHAIN_ID_SEPARATOR+transformId);
		s.addChain(newChain);
	}

	/**
	 * Returns a list of transformation matrices for the generation of a macromolecular
	 * assembly for the specified assembly Id.
	 *
	 * @param pdbxStructAssembly
	 * @param assemblyIndex
	 * @param pdbxStructAssemblyGen
	 * @param pdbxStructOperList
	 * @return list of transformation matrices to generate macromolecular assembly
	 */
	public List<BiologicalAssemblyTransformation> getBioUnitTransformationList(PdbxStructAssembly pdbxStructAssembly,
																			   int assemblyIndex,
																			   PdbxStructAssemblyGen pdbxStructAssemblyGen,
																			   PdbxStructOperList pdbxStructOperList) {
		init();

		// first we populate the list of all operators from pdbx_struct_oper_list so that we can then
		// get them from getBioUnitTransformationsListUnaryOperators() and getBioUnitTransformationsListBinaryOperators()
		for (int i = 0; i < pdbxStructOperList.getRowCount(); i++) {
			try {
				Matrix4d m = new Matrix4d();
				m.m00 = pdbxStructOperList.getMatrix11().get(i);
				m.m01 = pdbxStructOperList.getMatrix12().get(i);
				m.m02 = pdbxStructOperList.getMatrix13().get(i);

				m.m10 = pdbxStructOperList.getMatrix21().get(i);
				m.m11 = pdbxStructOperList.getMatrix22().get(i);
				m.m12 = pdbxStructOperList.getMatrix23().get(i);

				m.m20 = pdbxStructOperList.getMatrix31().get(i);
				m.m21 = pdbxStructOperList.getMatrix32().get(i);
				m.m22 = pdbxStructOperList.getMatrix33().get(i);

				m.m03 = pdbxStructOperList.getVector1().get(i);
				m.m13 = pdbxStructOperList.getVector2().get(i);
				m.m23 = pdbxStructOperList.getVector3().get(i);

				m.m30 = 0;
				m.m31 = 0;
				m.m32 = 0;
				m.m33 = 1;

				allTransformations.put(pdbxStructOperList.getId().get(i), m);
			} catch (NumberFormatException e) {
				logger.warn("Could not parse a matrix value from pdbx_struct_oper_list for id {}. The operator id will be ignored. Error: {}", pdbxStructOperList.getId().get(i), e.getMessage());
			}
		}

		String assemblyId = pdbxStructAssembly.getId().get(assemblyIndex);
		ArrayList<BiologicalAssemblyTransformation> transformations = getBioUnitTransformationsListUnaryOperators(assemblyId, pdbxStructAssemblyGen);
		transformations.addAll(getBioUnitTransformationsListBinaryOperators(assemblyId, pdbxStructAssemblyGen));
		transformations.trimToSize();
		return transformations;
	}

	private ArrayList<BiologicalAssemblyTransformation> getBioUnitTransformationsListBinaryOperators(String assemblyId, PdbxStructAssemblyGen pdbxStructAssemblyGen) {
		ArrayList<BiologicalAssemblyTransformation> transformations = new ArrayList<>();
		List<OrderedPair<String>> operators = operatorResolver.getBinaryOperators();

		for (int i = 0; i < pdbxStructAssemblyGen.getRowCount(); i++) {
			if (!pdbxStructAssemblyGen.getAssemblyId().get(i).equals(assemblyId)) {
				continue;
			}

			String[] asymIds= pdbxStructAssemblyGen.getAsymIdList().get(i).split(",");
			operatorResolver.parseOperatorExpressionString(pdbxStructAssemblyGen.getOperExpression().get(i));

			// apply binary operators to the specified chains
			// Example 1M4X: generates all products of transformation matrices (1-60)(61-88)
			for (String chainId : asymIds) {
				for (OrderedPair<String> operator : operators) {
					Matrix4d original1 = allTransformations.get(operator.getElement1());
					Matrix4d original2 = allTransformations.get(operator.getElement2());
					if (original1 == null || original2 == null) {
						logger.warn("Could not find matrix operator for operator id {} or {}. Assembly id {} will not contain the composed operator.", operator.getElement1(), operator.getElement2(), assemblyId);
						continue;
					}
					Matrix4d composed = new Matrix4d(original1);
					composed.mul(original2);
					BiologicalAssemblyTransformation transform = new BiologicalAssemblyTransformation();
					transform.setChainId(chainId);
					transform.setId(operator.getElement1() + COMPOSED_OPERATOR_SEPARATOR + operator.getElement2());
					transform.setTransformationMatrix(composed);
					transformations.add(transform);
				}
			}
		}

		return transformations;
	}

	private ArrayList<BiologicalAssemblyTransformation> getBioUnitTransformationsListUnaryOperators(String assemblyId, PdbxStructAssemblyGen pdbxStructAssemblyGen) {
		ArrayList<BiologicalAssemblyTransformation> transformations = new ArrayList<>();

		for (int i = 0; i < pdbxStructAssemblyGen.getRowCount(); i++) {
			if (!pdbxStructAssemblyGen.getAssemblyId().get(i).equals(assemblyId)) {
				continue;
			}

			operatorResolver.parseOperatorExpressionString(pdbxStructAssemblyGen.getOperExpression().get(i));
			List<String> operators = operatorResolver.getUnaryOperators();
			String[] asymIds = pdbxStructAssemblyGen.getAsymIdList().get(i).split(",");

			// apply unary operators to the specified chains
			for (String chainId : asymIds) {
				for (String operator : operators) {
					Matrix4d original = allTransformations.get(operator);
					if (original == null) {
						logger.warn("Could not find matrix operator for operator id {}. Assembly id {} will not contain the operator.", operator, assemblyId);
						continue;
					}
					BiologicalAssemblyTransformation transform = new BiologicalAssemblyTransformation();
					transform.setChainId(chainId);
					transform.setId(operator);
					transform.setTransformationMatrix(original);
					transformations.add(transform);
				}
			}
		}

		return transformations;
	}

	private void init() {
		operatorResolver = new OperatorResolver();
		allTransformations = new HashMap<>();
	}
}
