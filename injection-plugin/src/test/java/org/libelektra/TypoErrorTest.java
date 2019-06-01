package org.libelektra;

import org.libelektra.errortypes.TypoError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.libelektra.InjectionPlugin.ROOT_KEY;
import static org.libelektra.errortypes.TypoError.Metadata.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class TypoErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(TypoErrorTest.class);

    private InjectionPlugin injectionPlugin;
    private KDB kdb;
    private KeySet loadedKeySet;
    private Key testKey;


    @Before
    public void setUp() throws KDB.KDBException {
        injectionPlugin = new InjectionPlugin("user/tests/inject");
        kdb = injectionPlugin.kdb;
        loadedKeySet = KeySet.create();
        kdb.get(loadedKeySet, ROOT_KEY);
        testKey = Key.create(ROOT_KEY +"/some/value", "abcdef");
        loadedKeySet.append(testKey);
    }

    @Test
    public void transposition_shouldWork() throws Exception {
        testKey.setMeta(TYPO_TRANSPOSITION.getMetadata(), "");
        testKey.setMeta(InjectionPlugin.SEED_META, "411");

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        assertThat("Changed the length in transposition",
                loadedKeySet.lookup(testKey.getName()).getString().length(),
                is(testKey.getString().length()));
        assertThat("Strings are still the same after transposition",
                loadedKeySet.lookup(testKey.getName()).getString(),
                not(testKey.getString()));
        assertThat("Metadata was not removed",
                loadedKeySet.lookup(testKey.getName())
                        .getMeta(TypoError.Metadata.TYPO_TRANSPOSITION.getMetadata()).getName(),
                nullValue());
        KeySet.printKeySet(loadedKeySet);
    }

    @Test
    public void insertion_shouldWork() throws Exception {
        testKey.setMeta(TYPO_INSERTION.getMetadata(), "");
        testKey.setMeta(InjectionPlugin.SEED_META, "411");

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        assertThat("Metadata was not removed",
                loadedKeySet.lookup(testKey.getName()).getMeta(TypoError.Metadata.TYPO_INSERTION.getMetadata()).getName(),
                nullValue());
        assertThat("Strings are still the same after character injection",
                loadedKeySet.lookup(testKey.getName()).getString(),
                not(testKey.getString()));
        KeySet.printKeySet(loadedKeySet);
    }

    @Test
    public void deletion_shouldWork() throws Exception {
        testKey.setMeta(TYPO_DELETION.getMetadata(), "");
        testKey.setMeta(InjectionPlugin.SEED_META, "411");

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        assertThat("Metadata was not removed",
                loadedKeySet.lookup(testKey.getName())
                        .getMeta(TypoError.Metadata.TYPO_DELETION.getMetadata()).getName(),
                nullValue());
        assertThat("Strings are still the same after character deletion",
                loadedKeySet.lookup(testKey.getName()).getString(),
                not(testKey.getString()));
        KeySet.printKeySet(loadedKeySet);
    }

    @Test
    public void changeChar_shouldWork() throws Exception {
        testKey.setMeta(TYPO_CHANGE_CHAR.getMetadata(), "");
        testKey.setMeta(InjectionPlugin.SEED_META, "411");

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        KeySet.printKeySet(loadedKeySet);
        assertThat("Changed the length in change char execution",
                loadedKeySet.lookup(testKey.getName()).getString().length(),
                is(testKey.getString().length()));
        assertThat("Strings are still the same after change char execution",
                loadedKeySet.lookup(testKey.getName()).getString(),
                not(testKey.getString()));
        assertThat("Metadata was not removed",
                loadedKeySet.lookup(testKey.getName())
                        .getMeta(TypoError.Metadata.TYPO_CHANGE_CHAR.getMetadata()).getName()
                , nullValue());
    }

    @Test
    public void insertSpace_shouldWork() throws Exception {
        testKey.setMeta(TYPO_SPACE.getMetadata(), "");
        testKey.setMeta(InjectionPlugin.SEED_META, "411");

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        KeySet.printKeySet(loadedKeySet);

        assertThat("Changed the length in change char execution",
                loadedKeySet.lookup(testKey.getName()).getString().length(),
                not(testKey.getString().length()));
        assertThat("Strings are still the same after change char execution",
                loadedKeySet.lookup(testKey.getName()).getString(),
                not(testKey.getString()));
        assertThat("Metadata was not removed",
                loadedKeySet.lookup(testKey.getName())
                        .getMeta(TypoError.Metadata.TYPO_SPACE.getMetadata()).getName(),
                nullValue());
    }

    @Test
    public void toggleCase_shouldWork() throws Exception {
        testKey.setMeta(TYPO_TOGGLE.getMetadata(), "");
        testKey.setMeta(InjectionPlugin.SEED_META, "411");

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        KeySet.printKeySet(loadedKeySet);
        assertThat("Strings are still the same after toggling case execution",
                loadedKeySet.lookup(testKey.getName()).getString(),
                not(testKey.getString()));
        assertThat("Metadata was not removed",
                loadedKeySet.lookup(testKey.getName())
                        .getMeta(TypoError.Metadata.TYPO_TOGGLE.getMetadata()).getName(),
                nullValue());
    }


    @After
    public void tearDown() throws KDB.KDBException {
        Util.cleanUp(loadedKeySet, kdb);
    }

}