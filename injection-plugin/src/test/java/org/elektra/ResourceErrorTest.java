package org.elektra;

import org.elektra.errortypes.ResourceError;
import org.elektra.errortypes.TypoError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.elektra.InjectionPlugin.ROOT_KEY;
import static org.elektra.errortypes.ResourceError.Metadata.RESOURCE_ERROR;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(ResourceErrorTest.class);

    private InjectionPlugin injectionPlugin;
    private KDB kdb;
    private KeySet loadedKeySet;
    private Key testKey;

    List<String> alternativeOptions;


    @BeforeEach
    public void setUp() throws KDB.KDBException {
        injectionPlugin = new InjectionPlugin();
        kdb = injectionPlugin.kdb;
        loadedKeySet = KeySet.create();
        kdb.get(loadedKeySet, ROOT_KEY);
        testKey = Key.create(ROOT_KEY +"/some/value", "/my/valid/path");
        loadedKeySet.append(testKey);
        alternativeOptions = new ArrayList<>();
        alternativeOptions.add("/tmp/myfile.txt");
        alternativeOptions.add("/root/secure.txt");
        alternativeOptions.add("/does/not/exist");
    }

    @Test
    public void resourceError_shouldWork() throws Exception {
        testKey.setMeta(RESOURCE_ERROR.getMetadata()+"/#0", alternativeOptions.get(0));
        testKey.setMeta(RESOURCE_ERROR.getMetadata()+"/#1", alternativeOptions.get(1));
        testKey.setMeta(RESOURCE_ERROR.getMetadata()+"/#2", alternativeOptions.get(2));
        testKey.setMeta(InjectionPlugin.SEED_META, "411");

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        KeySet.printKeySet(loadedKeySet);

        String newString = loadedKeySet.lookup(testKey.getName()).getString();
        assertTrue(alternativeOptions.stream().anyMatch(newString::equals),
                "None of the provided values were picked in resource error!");
        assertNull(loadedKeySet.lookup(testKey.getName())
                        .getMeta(ResourceError.Metadata.RESOURCE_ERROR.getMetadata()+"/#0").getName(),
                "Metadata (#0) was not removed");
        assertNull(loadedKeySet.lookup(testKey.getName())
                        .getMeta(ResourceError.Metadata.RESOURCE_ERROR.getMetadata()+"/#1").getName(),
                "Metadata (#1) was not removed");
        assertNull(loadedKeySet.lookup(testKey.getName())
                        .getMeta(ResourceError.Metadata.RESOURCE_ERROR.getMetadata()+"/#2").getName(),
                "Metadata (#2) was not removed");
    }


    @AfterEach
    public void tearDown() throws KDB.KDBException {
        Util.cleanUp(loadedKeySet, kdb);
    }

}