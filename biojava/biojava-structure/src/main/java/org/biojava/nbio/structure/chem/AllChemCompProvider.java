package org.biojava.nbio.structure.chem;

import org.biojava.nbio.structure.align.util.UserConfiguration;
import org.biojava.nbio.structure.io.LocalPDBDirectory;
import org.biojava.nbio.structure.io.cif.ChemCompConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A ChemComp provider that downloads and caches the components.cif file from the wwPDB site. It then loads all chemical
 * components at startup and keeps them in memory. This provider is not used as a default since it is slower at startup
 * and requires more memory than the {@link DownloadChemCompProvider} that is used by default.
 *
 * @author Andreas Prlic
 */
public class AllChemCompProvider implements ChemCompProvider, Runnable {
    private static final Logger logger = LoggerFactory.getLogger(AllChemCompProvider.class);
    public static final String COMPONENTS_FILE_LOCATION = "pub/pdb/data/monomers/components.cif.gz";
    private static String path;
    private static String serverName;

    // there will be only one copy of the dictionary across all instances
    // to reduce memory impact
    static ChemicalComponentDictionary dict;

    // flags to make sure there is only one thread running that is loading the dictionary
    static AtomicBoolean loading = new AtomicBoolean(false);
    static AtomicBoolean isInitialized = new AtomicBoolean(false);

    public AllChemCompProvider() {
        if (loading.get()) {
            logger.warn("other thread is already loading all chemcomps, no need to init twice");
            return;
        }
        if (isInitialized.get()) {
            return;
        }

        loading.set(true);

        Thread t = new Thread(this);
        t.start();
    }

    /**
     * make sure all paths are initialized correctly
     */
    private static void initPath() {
        if (path == null) {
            UserConfiguration config = new UserConfiguration();
            path = config.getCacheFilePath();
        }
    }

    private static void initServerName() {
        if (serverName == null) {
            serverName = LocalPDBDirectory.getServerName();
        }
    }

    private void ensureFileExists() {
        String fileName = getLocalFileName();
        File f = new File(fileName);

        if (!f.exists()) {
            try {
                downloadFile();
            } catch (IOException e) {
                logger.error("Caught IOException", e);
            }
        }
    }

    /**
     * Downloads the components.cif.gz file from the wwPDB site.
     */
    public static void downloadFile() throws IOException {
        initPath();
        initServerName();
        String localName = getLocalFileName();
        String u = serverName + "/" + COMPONENTS_FILE_LOCATION;
        downloadFileFromRemote(new URL(u), new File(localName));
    }

    private static  void downloadFileFromRemote(URL remoteURL, File localFile) throws IOException {
        logger.info("Downloading {} to: {}", remoteURL, localFile);
        FileOutputStream out = new FileOutputStream(localFile);

        InputStream in = remoteURL.openStream();
        byte[] buf = new byte[4 * 1024]; // 4K buffer
        int bytesRead;
        while ((bytesRead = in.read(buf)) != -1) {
            out.write(buf, 0, bytesRead);
        }
        in.close();
        out.close();
    }

    private static String getLocalFileName(){
        File dir = new File(path, DownloadChemCompProvider.CHEM_COMP_CACHE_DIRECTORY);

        if (!dir.exists()) {
            logger.info("Creating directory {}", dir.toString());
            dir.mkdir();
        }

        return new File(dir, "components.cif.gz").toString();
    }

    /**
     * Load all {@link ChemComp} definitions into memory.
     */
    private void loadAllChemComps() throws IOException {
        String fileName = getLocalFileName();
        logger.debug("Loading {}", fileName);
        dict = ChemCompConverter.fromPath(Paths.get(fileName));
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public ChemComp getChemComp(String recordName) {
        while (loading.get()) {
            // another thread is still initializing the definitions
            try {
                // wait half a second
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error("Interrepted thread while waiting: {}", e.getMessage());
                //e.printStackTrace();
            }
        }

        return dict.getChemComp(recordName);
    }

    /**
     * Do the actual loading of the dictionary in a thread.
     */
    @Override
    public void run() {
        long timeS = System.currentTimeMillis();
        initPath();
        ensureFileExists();

        try {
            loadAllChemComps();
            long timeE = System.currentTimeMillis();
            logger.debug("Time to init chem comp dictionary: {} sec.", (timeE - timeS) / 1000);
        } catch (IOException e) {
            logger.error("Could not load chemical components definition file {}. Error: {}", getLocalFileName(), e.getMessage());
        } finally {
            loading.set(false);
            isInitialized.set(true);
        }
    }
}

