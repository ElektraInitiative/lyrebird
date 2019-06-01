package org.elektra;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.elektra.InjectionPlugin.ROOT_KEY;
import static org.elektra.errortypes.LimitError.Metadata.LIMIT_ERROR_MAX;
import static org.elektra.errortypes.LimitError.Metadata.LIMIT_ERROR_MIN;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimitErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(LimitErrorTest.class);

    private InjectionPlugin injectionPlugin;
    private KDB kdb;
    private KeySet loadedKeySet;
    private Key testKey;


    @BeforeEach
    public void setUp() throws KDB.KDBException {
        injectionPlugin = new InjectionPlugin("user/tests/inject");
        kdb = injectionPlugin.kdb;
        loadedKeySet = KeySet.create();
        kdb.get(loadedKeySet, ROOT_KEY);
        testKey = Key.create(ROOT_KEY +"/some/value", "350");
        loadedKeySet.append(testKey);
    }

    @Test
    public void limitError_shouldWork() throws Exception {
        String minValue = "0";
        String maxValue = "1000";
        testKey.setMeta(LIMIT_ERROR_MIN.getMetadata(), minValue);
        testKey.setMeta(LIMIT_ERROR_MAX.getMetadata(), maxValue);
        testKey.setMeta(InjectionPlugin.SEED_META, "411");

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        KeySet.printKeySet(loadedKeySet);

        String newString = loadedKeySet.lookup(testKey.getName()).getString();
        assertTrue(newString.equals(minValue) || newString.equals(maxValue),
                "Neither min nor max value was applied in limit error!");
        assertNull(loadedKeySet.lookup(testKey.getName())
                        .getMeta(LIMIT_ERROR_MIN.getMetadata()).getName(),
                "Metadata MIN was not removed");
        assertNull(loadedKeySet.lookup(testKey.getName())
                        .getMeta(LIMIT_ERROR_MAX.getMetadata()).getName(),
                "Metadata MAX was not removed");
    }


    @AfterEach
    public void tearDown() throws KDB.KDBException {
        Util.cleanUp(loadedKeySet, kdb);
    }

}