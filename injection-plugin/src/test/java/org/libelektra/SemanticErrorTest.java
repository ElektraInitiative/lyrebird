package org.libelektra;

import org.junit.Before;
import org.junit.Test;
import org.libelektra.errortypes.InjectionData;
import org.libelektra.errortypes.SemanticError;
import org.libelektra.service.RandomizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.libelektra.errortypes.SemanticError.Metadata.SEMANTIC_ERROR;

public class SemanticErrorTest extends AbstractErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(SemanticErrorTest.class);

    private SemanticError semanticError;
    private KeySet loadedKeySet;
    private Key injectKey;

    List<String> alternativeOptions;


    @Before
    public void setUp() throws KDB.KDBException {
        semanticError = new SemanticError(new RandomizerService(100));
        loadedKeySet = KeySet.create();
        kdbService.get(loadedKeySet, Key.create(TEST_NAMESPACE));
        injectKey = Key.create(INJECT_NAMESPACE + "/some/value", "");
        loadedKeySet.append(injectKey);
        alternativeOptions = new ArrayList<>();
        alternativeOptions.add("one");
        alternativeOptions.add("two");
        alternativeOptions.add("three");
    }

    @Test
    public void semanticError_shouldWork() throws Exception {
        injectKey.setMeta(SEMANTIC_ERROR.getMetadata() + "/#0", alternativeOptions.get(0));
        injectKey.setMeta(SEMANTIC_ERROR.getMetadata() + "/#1", alternativeOptions.get(1));
        injectKey.setMeta(SEMANTIC_ERROR.getMetadata() + "/#2", alternativeOptions.get(2));

        KeySet.printKeySet(loadedKeySet);
        KeySet returnedSet = semanticError.apply(new InjectionData(loadedKeySet, injectKey,
                null, APPLY_NAMESPACE, SEMANTIC_ERROR));
        KeySet.printKeySet(loadedKeySet);
        kdbService.set(returnedSet, APPLY_NAMESPACE);

        String newString = returnedSet.lookup(APPLY_NAMESPACE).getString();
        assertThat("None of the provided values were picked in semantic error!",
                alternativeOptions.stream().anyMatch(newString::equals),
                is(true));
    }

}