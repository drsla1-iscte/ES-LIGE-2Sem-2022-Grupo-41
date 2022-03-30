package org.biojava.nbio.structure.io.cif;

import org.biojava.nbio.structure.Structure;
import org.rcsb.cif.schema.mm.AtomSite;
import org.rcsb.cif.schema.mm.AtomSites;
import org.rcsb.cif.schema.mm.AuditAuthor;
import org.rcsb.cif.schema.mm.Cell;
import org.rcsb.cif.schema.mm.ChemComp;
import org.rcsb.cif.schema.mm.ChemCompBond;
import org.rcsb.cif.schema.mm.DatabasePDBRemark;
import org.rcsb.cif.schema.mm.DatabasePDBRev;
import org.rcsb.cif.schema.mm.DatabasePDBRevRecord;
import org.rcsb.cif.schema.mm.Em3dReconstruction;
import org.rcsb.cif.schema.mm.Entity;
import org.rcsb.cif.schema.mm.EntityPoly;
import org.rcsb.cif.schema.mm.EntityPolySeq;
import org.rcsb.cif.schema.mm.EntitySrcGen;
import org.rcsb.cif.schema.mm.EntitySrcNat;
import org.rcsb.cif.schema.mm.Exptl;
import org.rcsb.cif.schema.mm.PdbxAuditRevisionHistory;
import org.rcsb.cif.schema.mm.PdbxChemCompIdentifier;
import org.rcsb.cif.schema.mm.PdbxDatabaseStatus;
import org.rcsb.cif.schema.mm.PdbxEntityBranchDescriptor;
import org.rcsb.cif.schema.mm.PdbxEntitySrcSyn;
import org.rcsb.cif.schema.mm.PdbxMolecule;
import org.rcsb.cif.schema.mm.PdbxMoleculeFeatures;
import org.rcsb.cif.schema.mm.PdbxNonpolyScheme;
import org.rcsb.cif.schema.mm.PdbxReferenceEntityLink;
import org.rcsb.cif.schema.mm.PdbxReferenceEntityList;
import org.rcsb.cif.schema.mm.PdbxReferenceEntityPolyLink;
import org.rcsb.cif.schema.mm.PdbxStructAssembly;
import org.rcsb.cif.schema.mm.PdbxStructAssemblyGen;
import org.rcsb.cif.schema.mm.PdbxStructModResidue;
import org.rcsb.cif.schema.mm.PdbxStructOperList;
import org.rcsb.cif.schema.mm.Refine;
import org.rcsb.cif.schema.mm.Struct;
import org.rcsb.cif.schema.mm.StructAsym;
import org.rcsb.cif.schema.mm.StructConf;
import org.rcsb.cif.schema.mm.StructConn;
import org.rcsb.cif.schema.mm.StructConnType;
import org.rcsb.cif.schema.mm.StructKeywords;
import org.rcsb.cif.schema.mm.StructNcsOper;
import org.rcsb.cif.schema.mm.StructRef;
import org.rcsb.cif.schema.mm.StructRefSeq;
import org.rcsb.cif.schema.mm.StructRefSeqDif;
import org.rcsb.cif.schema.mm.StructSheetRange;
import org.rcsb.cif.schema.mm.StructSite;
import org.rcsb.cif.schema.mm.StructSiteGen;
import org.rcsb.cif.schema.mm.Symmetry;

/**
 * Defines the categories to consume during CIF parsing.
 * @author Sebastian Bittrich
 * @since 6.0.0
 */
public interface CifStructureConsumer extends CifFileConsumer<Structure> {
    /**
     * Consume a particular Cif category.
     * @param atomSite data
     */
    void consumeAtomSite(AtomSite atomSite);

    /**
     * Consume a particular Cif category.
     * @param atomSites data
     */
    void consumeAtomSites(AtomSites atomSites);

    /**
     * Consume a particular Cif category.
     * @param auditAuthor data
     */
    void consumeAuditAuthor(AuditAuthor auditAuthor);

    /**
     * Consume a particular Cif category.
     * @param cell data
     */
    void consumeCell(Cell cell);

    /**
     * Consume a particular Cif category.
     * @param chemComp data
     */
    void consumeChemComp(ChemComp chemComp);

    /**
     * Consume a particular Cif category.
     * @param chemCompBond data
     */
    void consumeChemCompBond(ChemCompBond chemCompBond);

    /**
     * Consume a particular Cif category.
     * @param databasePDBremark data
     */
    void consumeDatabasePDBRemark(DatabasePDBRemark databasePDBremark);

    /**
     * Consume a particular Cif category.
     * @param databasePDBrev data
     */
    void consumeDatabasePDBRev(DatabasePDBRev databasePDBrev);

    /**
     * Consume a particular Cif category.
     * @param databasePDBrevRecord data
     */
    void consumeDatabasePDBRevRecord(DatabasePDBRevRecord databasePDBrevRecord);

	/**
	 * Consume Electron Microscopy 3D reconstruction data
	 * @param em3dReconstruction
	 */
	void consumeEm3dReconstruction(Em3dReconstruction em3dReconstruction);

	/**
     * Consume a particular Cif category.
     * @param entity data
     */
    void consumeEntity(Entity entity);

    /**
     * Consume a particular Cif category.
     * @param entityPoly data
     */
    void consumeEntityPoly(EntityPoly entityPoly);

    /**
     * Consume a particular Cif category.
     * @param entitySrcGen data
     */
    void consumeEntitySrcGen(EntitySrcGen entitySrcGen);

    /**
     * Consume a particular Cif category.
     * @param entitySrcNat data
     */
    void consumeEntitySrcNat(EntitySrcNat entitySrcNat);

    /**
     * Consume a particular Cif category.
     * @param entitySrcSyn data
     */
    void consumeEntitySrcSyn(PdbxEntitySrcSyn entitySrcSyn);

    /**
     * Consume a particular Cif category.
     * @param entityPolySeq data
     */
    void consumeEntityPolySeq(EntityPolySeq entityPolySeq);

    /**
     * Consume a particular Cif category.
     * @param exptl data
     */
    void consumeExptl(Exptl exptl);

    /**
     * Consume a particular Cif category.
     * @param pdbxAuditRevisionHistory data
     */
    void consumePdbxAuditRevisionHistory(PdbxAuditRevisionHistory pdbxAuditRevisionHistory);

    /**
     * Consume a particular Cif category.
     * @param pdbxChemCompIdentifier data
     */
    void consumePdbxChemCompIdentifier(PdbxChemCompIdentifier pdbxChemCompIdentifier);

    /**
     * Consume a particular Cif category.
     * @param pdbxDatabaseStatus data
     */
    void consumePdbxDatabaseStatus(PdbxDatabaseStatus pdbxDatabaseStatus);

    /**
     * Consume a particular Cif category.
     * @param pdbxEntityBranchDescriptor data
     */
    void consumePdbxEntityBranchDescriptor(PdbxEntityBranchDescriptor pdbxEntityBranchDescriptor);

    /**
     * Consume a particular Cif category.
     * @param pdbxMolecule data
     */
    void consumePdbxMolecule(PdbxMolecule pdbxMolecule);

    /**
     * Consume a particular Cif category.
     * @param pdbxMoleculeFeatures data
     */
    void consumePdbxMoleculeFeatures(PdbxMoleculeFeatures pdbxMoleculeFeatures);

    /**
     * Consume a particular Cif category.
     * @param pdbxNonpolyScheme data
     */
    void consumePdbxNonpolyScheme(PdbxNonpolyScheme pdbxNonpolyScheme);

    /**
     * Consume a particular Cif category.
     * @param pdbxReferenceEntityLink data
     */
    void consumePdbxReferenceEntityLink(PdbxReferenceEntityLink pdbxReferenceEntityLink);

    /**
     * Consume a particular Cif category.
     * @param pdbxReferenceEntityList data
     */
    void consumePdbxReferenceEntityList(PdbxReferenceEntityList pdbxReferenceEntityList);

    /**
     * Consume a particular Cif category.
     * @param pdbxReferenceEntityPolyLink data
     */
    void consumePdbxReferenceEntityPolyLink(PdbxReferenceEntityPolyLink pdbxReferenceEntityPolyLink);

    /**
     * Consume a particular Cif category.
     * @param pdbxStructAssembly data
     */
    void consumePdbxStructAssembly(PdbxStructAssembly pdbxStructAssembly);

    /**
     * Consume a particular Cif category.
     * @param pdbxStructAssemblyGen data
     */
    void consumePdbxStructAssemblyGen(PdbxStructAssemblyGen pdbxStructAssemblyGen);

    /**
     * Consume a particular Cif category.
     * @param pdbxStructModResidue data
     */
    void consumePdbxStructModResidue(PdbxStructModResidue pdbxStructModResidue);

    /**
     * Consume a particular Cif category.
     * @param pdbxStructOperList data
     */
    void consumePdbxStructOperList(PdbxStructOperList pdbxStructOperList);

    /**
     * Consume a particular Cif category.
     * @param refine data
     */
    void consumeRefine(Refine refine);

    /**
     * Consume a particular Cif category.
     * @param struct data
     */
    void consumeStruct(Struct struct);

    /**
     * Consume a particular Cif category.
     * @param structAsym data
     */
    void consumeStructAsym(StructAsym structAsym);

    /**
     * Consume a particular Cif category.
     * @param structConf data
     */
    void consumeStructConf(StructConf structConf);

    /**
     * Consume a particular Cif category.
     * @param structConn data
     */
    void consumeStructConn(StructConn structConn);

    /**
     * Consume a particular Cif category.
     * @param structConnType data
     */
    void consumeStructConnType(StructConnType structConnType);

    /**
     * Consume a particular Cif category.
     * @param structKeywords data
     */
    void consumeStructKeywords(StructKeywords structKeywords);

    /**
     * Consume a particular Cif category.
     * @param structNcsOper data
     */
    void consumeStructNcsOper(StructNcsOper structNcsOper);

    /**
     * Consume a particular Cif category.
     * @param structRef data
     */
    void consumeStructRef(StructRef structRef);

    /**
     * Consume a particular Cif category.
     * @param structRefSeq data
     */
    void consumeStructRefSeq(StructRefSeq structRefSeq);

    /**
     * Consume a particular Cif category.
     * @param structRefSeqDif data
     */
    void consumeStructRefSeqDif(StructRefSeqDif structRefSeqDif);

    /**
     * Consume a particular Cif category.
     * @param structSheetRange data
     */
    void consumeStructSheetRange(StructSheetRange structSheetRange);

    /**
     * Consume a particular Cif category.
     * @param structSite data
     */
    void consumeStructSite(StructSite structSite);

    /**
     * Consume a particular Cif category.
     * @param structSiteGen data
     */
    void consumeStructSiteGen(StructSiteGen structSiteGen);

    /**
     * Consume a particular Cif category.
     * @param symmetry data
     */
    void consumeSymmetry(Symmetry symmetry);
}
