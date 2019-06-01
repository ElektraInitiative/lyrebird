package org.elektra;

import org.elektra.errortypes.SemanticError;
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
import static org.elektra.errortypes.SemanticError.Metadata.*;
import static org.junit.jupiter.api.Assertions.*;

class SemanticErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(SemanticErrorTest.class);

    private InjectionPlugin injectionPlugin;
    private KDB kdb;
    private KeySet loadedKeySet;
    private Key testKey;

    List<String> alternativeOptions;


    @BeforeEach
    public void setUp() throws KDB.KDBException {
        injectionPlugin = new InjectionPlugin("user/tests/inject");
        kdb = injectionPlugin.kdb;
        loadedKeySet = KeySet.create();
        kdb.get(loadedKeySet, ROOT_KEY);
        testKey = Key.create(ROOT_KEY +"/some/value", "abcdef");
        loadedKeySet.append(testKey);
        alternativeOptions = new ArrayList<>();
        alternativeOptions.add("one");
        alternativeOptions.add("two");
        alternativeOptions.add("three");
    }

    @Test
    public void semanticError_shouldWork() throws Exception {
        testKey.setMeta(SEMANTIC_ERROR.getMetadata()+"/#0", alternativeOptions.get(0));
        testKey.setMeta(SEMANTIC_ERROR.getMetadata()+"/#1", alternativeOptions.get(1));
        testKey.setMeta(SEMANTIC_ERROR.getMetadata()+"/#2", alternativeOptions.get(2));
        testKey.setMeta(InjectionPlugin.SEED_META, "411");

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        KeySet.printKeySet(loadedKeySet);

        String newString = loadedKeySet.lookup(testKey.getName()).getString();
        assertTrue(alternativeOptions.stream().anyMatch(newString::equals),
                "None of the provided values were picked in semantic error!");
        assertNull(loadedKeySet.lookup(testKey.getName())
                        .getMeta(SemanticError.Metadata.SEMANTIC_ERROR.getMetadata()+"/#0").getName(),
                "Metadata (#0) was not removed");
        assertNull(loadedKeySet.lookup(testKey.getName())
                        .getMeta(SemanticError.Metadata.SEMANTIC_ERROR.getMetadata()+"/#1").getName(),
                "Metadata (#1) was not removed");
        assertNull(loadedKeySet.lookup(testKey.getName())
                        .getMeta(SemanticError.Metadata.SEMANTIC_ERROR.getMetadata()+"/#2").getName(),
                "Metadata (#2) was not removed");
    }


    @AfterEach
    public void tearDown() throws KDB.KDBException {
        Util.cleanUp(loadedKeySet, kdb);
    }

}