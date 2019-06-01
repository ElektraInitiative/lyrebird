package org.elektra;

import org.elektra.errortypes.StructureError;
import org.elektra.errortypes.TypoError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.elektra.InjectionPlugin.*;
import static org.elektra.errortypes.TypoError.Metadata.*;
import static org.junit.jupiter.api.Assertions.*;

class TypoErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(TypoErrorTest.class);

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
        assertEquals(loadedKeySet.lookup(testKey.getName()).getString().length(),
                testKey.getString().length(),
                "Changed the length in transposition");
        assertNotEquals(loadedKeySet.lookup(testKey.getName()).getString(),
                testKey.getString(),
                "Strings are still the same after transposition");
        assertNull(loadedKeySet.lookup(testKey.getName())
                        .getMeta(TypoError.Metadata.TYPO_TRANSPOSITION.getMetadata()).getName(),
                "Metadata was not removed");
        KeySet.printKeySet(loadedKeySet);
    }

    @Test
    public void insertion_shouldWork() throws Exception {
        testKey.setMeta(TYPO_INSERTION.getMetadata(), "");
        testKey.setMeta(InjectionPlugin.SEED_META, "411");

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        assertNull(loadedKeySet.lookup(testKey.getName())
                        .getMeta(TypoError.Metadata.TYPO_INSERTION.getMetadata()).getName(),
                "Metadata was not removed");
        assertNotEquals(loadedKeySet.lookup(testKey.getName()).getString(),
                testKey.getString(),
                "Strings are still the same after character injection");
        KeySet.printKeySet(loadedKeySet);
    }

    @Test
    public void deletion_shouldWork() throws Exception {
        testKey.setMeta(TYPO_DELETION.getMetadata(), "");
        testKey.setMeta(InjectionPlugin.SEED_META, "411");

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        assertNull(loadedKeySet.lookup(testKey.getName())
                        .getMeta(TypoError.Metadata.TYPO_DELETION.getMetadata()).getName(),
                "Metadata was not removed");
        assertNotEquals(loadedKeySet.lookup(testKey.getName()).getString(),
                testKey.getString(),
                "Strings are still the same after character deletion");
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
        assertEquals(loadedKeySet.lookup(testKey.getName()).getString().length(),
                testKey.getString().length(),
                "Changed the length in change char execution");
        assertNotEquals(loadedKeySet.lookup(testKey.getName()).getString(),
                testKey.getString(),
                "Strings are still the same after change char execution");
        assertNull(loadedKeySet.lookup(testKey.getName())
                        .getMeta(TypoError.Metadata.TYPO_CHANGE_CHAR.getMetadata()).getName(),
                "Metadata was not removed");
    }

    @Test
    public void insertSpace_shouldWork() throws Exception {
        testKey.setMeta(TYPO_SPACE.getMetadata(), "");
        testKey.setMeta(InjectionPlugin.SEED_META, "411");

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        KeySet.printKeySet(loadedKeySet);

        assertNotEquals(loadedKeySet.lookup(testKey.getName()).getString().length(),
                testKey.getString().length(),
                "Changed the length in change char execution");
        assertNotEquals(loadedKeySet.lookup(testKey.getName()).getString(),
                testKey.getString(),
                "Strings are still the same after change char execution");
        assertNull(loadedKeySet.lookup(testKey.getName())
                        .getMeta(TypoError.Metadata.TYPO_SPACE.getMetadata()).getName(),
                "Metadata was not removed");
    }

    @Test
    public void toggleCase_shouldWork() throws Exception {
        testKey.setMeta(TYPO_TOGGLE.getMetadata(), "");
        testKey.setMeta(InjectionPlugin.SEED_META, "411");

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        KeySet.printKeySet(loadedKeySet);
        assertNotEquals(loadedKeySet.lookup(testKey.getName()).getString(),
                testKey.getString(),
                "Strings are still the same after toggling case execution");
        assertNull(loadedKeySet.lookup(testKey.getName())
                        .getMeta(TypoError.Metadata.TYPO_TOGGLE.getMetadata()).getName(),
                "Metadata was not removed");
    }


    @AfterEach
    public void tearDown() throws KDB.KDBException {
        Util.cleanUp(loadedKeySet, kdb);
    }

}