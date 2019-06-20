package org.libelektra;


import org.junit.Before;
import org.junit.Test;
import org.libelektra.errortypes.InjectionData;
import org.libelektra.errortypes.LimitError;
import org.libelektra.service.KDBService;
import org.libelektra.service.RandomizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.libelektra.InjectionPlugin.ROOT_KEY;
import static org.libelektra.errortypes.LimitError.Metadata.LIMIT_ERROR_MAX;
import static org.libelektra.errortypes.LimitError.Metadata.LIMIT_ERROR_MIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class LimitErrorTest extends AbstractErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(LimitErrorTest.class);

    private KeySet loadedKeySet;

    private LimitError limitError;
    private Key injectKey;


    @Before
    public void setUp() throws KDB.KDBException {
        KDBService kdbService = new KDBService();
        limitError = new LimitError(new RandomizerService(100));
        loadedKeySet = KeySet.create();
        kdbService.get(loadedKeySet, TEST_NAMESPACE);
        loadedKeySet.append(Key.create(APPLY_NAMESPACE, "600"));
        injectKey = Key.create(INJECT_NAMESPACE + "/some/value", "");
    }

    @Test
    public void limitError_shouldWork() throws Exception {
        String minValue = "0";
        String maxValue = "1000";
        injectKey.setMeta(LIMIT_ERROR_MIN.getMetadata(), minValue);
        injectKey.setMeta(LIMIT_ERROR_MAX.getMetadata(), maxValue);

        KeySet.printKeySet(loadedKeySet);
        kdbService.set(loadedKeySet, ROOT_KEY);
        KeySet returnedKeySet = limitError.apply(new InjectionData(loadedKeySet, injectKey, null, APPLY_NAMESPACE
                , LIMIT_ERROR_MAX));
        KeySet.printKeySet(returnedKeySet);

        String newString = returnedKeySet.lookup(APPLY_NAMESPACE).getString();
        assertThat("Neither min nor max value was applied in limit error!",
                newString.equals(minValue) || newString.equals(maxValue),
                is(true));
    }

}