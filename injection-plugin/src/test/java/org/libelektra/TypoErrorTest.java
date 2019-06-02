package org.libelektra;

import org.junit.Before;
import org.junit.Test;
import org.libelektra.errortypes.TypoError;
import org.libelektra.service.RandomizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class TypoErrorTest extends AbstractErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(TypoErrorTest.class);

    private TypoError typoError;
    private KeySet loadedKeySet;
    private RandomizerService randomizerService;
    private Key presentKey;


    @Before
    public void setUp() throws KDB.KDBException {
        randomizerService = new RandomizerService(21);
        typoError = new TypoError(randomizerService, kdbService);
        loadedKeySet = KeySet.create();
        kdbService.get(loadedKeySet, Key.create(TEST_NAMESPACE));
        presentKey = Key.create(APPLY_NAMESPACE + "/some/value", "abcdef");
        loadedKeySet.append(presentKey);
    }

    @Test
    public void transposition_shouldWork() throws Exception {
        KeySet.printKeySet(loadedKeySet);
        String startString = loadedKeySet.lookup(APPLY_NAMESPACE + "/some/value").getString();
        KeySet returnedSet = typoError.applyTypoError(loadedKeySet, APPLY_NAMESPACE + "/some/value",
                TypoError.Metadata.TYPO_TRANSPOSITION);
        KeySet.printKeySet(loadedKeySet);
        kdbService.set(returnedSet, APPLY_NAMESPACE);
        String newString = returnedSet.lookup(APPLY_NAMESPACE + "/some/value").getString();

        assertThat("Changed the length in transposition",
                newString.length(),
                is(startString.length()));
        assertThat("Strings are still the same after transposition",
                newString,
                not(startString));
    }

    @Test
    public void insertion_shouldWork() throws Exception {

        KeySet.printKeySet(loadedKeySet);
        String startString = loadedKeySet.lookup(APPLY_NAMESPACE + "/some/value").getString();
        KeySet returnedSet = typoError.applyTypoError(loadedKeySet, APPLY_NAMESPACE + "/some/value",
                TypoError.Metadata.TYPO_INSERTION);
        KeySet.printKeySet(loadedKeySet);
        kdbService.set(returnedSet, APPLY_NAMESPACE);
        String newString = returnedSet.lookup(APPLY_NAMESPACE + "/some/value").getString();

        assertThat("Length not changed in insertion",
                newString.length(),
                not(startString.length()));
        assertThat("Strings are still the same after character injection",
                newString,
                not(startString));
    }

    @Test
    public void deletion_shouldWork() throws Exception {

        KeySet.printKeySet(loadedKeySet);
        String startString = loadedKeySet.lookup(APPLY_NAMESPACE + "/some/value").getString();
        KeySet returnedSet = typoError.applyTypoError(loadedKeySet, APPLY_NAMESPACE + "/some/value",
                TypoError.Metadata.TYPO_DELETION);
        KeySet.printKeySet(loadedKeySet);
        kdbService.set(returnedSet, APPLY_NAMESPACE);
        String newString = returnedSet.lookup(APPLY_NAMESPACE + "/some/value").getString();
        assertThat("Length not changed in deletion",
                newString.length(),
                not(startString.length()));
        assertThat("Strings are still the same after character deletion",
                startString,
                not(newString));
    }

    @Test
    public void changeChar_shouldWork() throws Exception {
        KeySet.printKeySet(loadedKeySet);
        String startString = loadedKeySet.lookup(APPLY_NAMESPACE + "/some/value").getString();
        KeySet returnedSet = typoError.applyTypoError(loadedKeySet, APPLY_NAMESPACE + "/some/value",
                TypoError.Metadata.TYPO_CHANGE_CHAR);
        KeySet.printKeySet(loadedKeySet);
        kdbService.set(returnedSet, APPLY_NAMESPACE);
        String newString = returnedSet.lookup(APPLY_NAMESPACE + "/some/value").getString();
        assertThat("Changed the length in change char execution",
                startString.length(),
                is(newString.length()));
        assertThat("Strings are still the same after change char execution",
                startString,
                not(newString));
    }

    @Test
    public void insertSpace_shouldWork() throws Exception {
        KeySet.printKeySet(loadedKeySet);
        String startString = loadedKeySet.lookup(APPLY_NAMESPACE + "/some/value").getString();
        KeySet returnedSet = typoError.applyTypoError(loadedKeySet, APPLY_NAMESPACE + "/some/value",
                TypoError.Metadata.TYPO_SPACE);
        KeySet.printKeySet(loadedKeySet);
        kdbService.set(returnedSet, APPLY_NAMESPACE);
        String newString = returnedSet.lookup(APPLY_NAMESPACE + "/some/value").getString();

        assertThat("Changed the length in change char execution",
                startString.length(),
                not(newString.length()));
        assertThat("Strings are still the same after change char execution",
                startString,
                not(newString));
    }

    @Test
    public void toggleCase_shouldWork() throws Exception {
        KeySet.printKeySet(loadedKeySet);
        String startString = loadedKeySet.lookup(APPLY_NAMESPACE + "/some/value").getString();
        KeySet returnedSet = typoError.applyTypoError(loadedKeySet, APPLY_NAMESPACE + "/some/value",
                TypoError.Metadata.TYPO_TOGGLE);
        KeySet.printKeySet(loadedKeySet);
        kdbService.set(returnedSet, APPLY_NAMESPACE);
        String newString = returnedSet.lookup(APPLY_NAMESPACE + "/some/value").getString();
        assertThat("Strings are still the same after toggling case execution",
                startString,
                not(newString));
    }

}