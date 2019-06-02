package org.libelektra;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.libelektra.service.KDBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.libelektra.InjectionPlugin.ROOT_KEY;
import static org.libelektra.errortypes.LimitError.Metadata.LIMIT_ERROR_MAX;
import static org.libelektra.errortypes.LimitError.Metadata.LIMIT_ERROR_MIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class LimitErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(LimitErrorTest.class);

    private InjectionPlugin injectionPlugin;
    private KDB kdb;
    private KeySet loadedKeySet;
    private Key testKey;


    @Before
    public void setUp() throws KDB.KDBException {
        KDBService kdbService = new KDBService();
        injectionPlugin = new InjectionPlugin(kdbService);
        kdb = kdbService.getInstance();
        loadedKeySet = KeySet.create();
        kdb.get(loadedKeySet, ROOT_KEY);
        testKey = Key.create(ROOT_KEY + "/some/value", "350");
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
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY, "user/tests/injectplugin");
        KeySet.printKeySet(loadedKeySet);

        String newString = loadedKeySet.lookup(testKey.getName()).getString();
        assertThat("Neither min nor max value was applied in limit error!",
                newString.equals(minValue) || newString.equals(maxValue),
                is(true));
        assertThat("Metadata MIN was not removed",
                loadedKeySet.lookup(testKey.getName())
                        .getMeta(LIMIT_ERROR_MIN.getMetadata()).getName(),
                nullValue());
        assertThat("Metadata MAX was not removed",
                loadedKeySet.lookup(testKey.getName())
                        .getMeta(LIMIT_ERROR_MAX.getMetadata()).getName(),
                nullValue());
    }


    @After
    public void tearDown() throws KDB.KDBException {
        Util.cleanUp(loadedKeySet, kdb);
    }

}