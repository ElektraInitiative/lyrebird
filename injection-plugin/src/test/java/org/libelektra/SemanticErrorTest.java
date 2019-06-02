package org.libelektra;

import org.libelektra.errortypes.SemanticError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.libelektra.service.KDBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.libelektra.InjectionPlugin.ROOT_KEY;
import static org.libelektra.errortypes.SemanticError.Metadata.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNull;

public class SemanticErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(SemanticErrorTest.class);

    private InjectionPlugin injectionPlugin;
    private KDB kdb;
    private KeySet loadedKeySet;
    private Key testKey;

    List<String> alternativeOptions;


//    @Before
//    public void setUp() throws KDB.KDBException {
//        KDBService kdbService = new KDBService();
//        injectionPlugin = new InjectionPlugin(structureError, typoError, semanticError, resourceError, domainError, limitError, kdbService);
//        kdb = kdbService.getInstance();
//        loadedKeySet = KeySet.create();
//        kdb.get(loadedKeySet, ROOT_KEY);
//        testKey = Key.create(ROOT_KEY + "/some/value", "abcdef");
//        loadedKeySet.append(testKey);
//        alternativeOptions = new ArrayList<>();
//        alternativeOptions.add("one");
//        alternativeOptions.add("two");
//        alternativeOptions.add("three");
//    }
//
//    @Test
//    public void semanticError_shouldWork() throws Exception {
//        testKey.setMeta(SEMANTIC_ERROR.getMetadata() + "/#0", alternativeOptions.get(0));
//        testKey.setMeta(SEMANTIC_ERROR.getMetadata() + "/#1", alternativeOptions.get(1));
//        testKey.setMeta(SEMANTIC_ERROR.getMetadata() + "/#2", alternativeOptions.get(2));
//        testKey.setMeta(InjectionPlugin.SEED_META, "411");
//
//        KeySet.printKeySet(loadedKeySet);
//        kdb.set(loadedKeySet, ROOT_KEY);
//        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY, "user/tests/injectplugin");
//        KeySet.printKeySet(loadedKeySet);
//
//        String newString = loadedKeySet.lookup(testKey.getName()).getString();
//        assertThat("None of the provided values were picked in semantic error!",
//                alternativeOptions.stream().anyMatch(newString::equals),
//                is(true));
//        assertThat("Metadata (#0) was not removed",
//                loadedKeySet.lookup(testKey.getName())
//                        .getMeta(SemanticError.Metadata.SEMANTIC_ERROR.getMetadata() + "/#0").getName(),
//                nullValue());
//        assertThat("Metadata (#1) was not removed",
//                loadedKeySet.lookup(testKey.getName())
//                        .getMeta(SemanticError.Metadata.SEMANTIC_ERROR.getMetadata() + "/#1").getName(),
//                nullValue());
//        assertThat("Metadata (#2) was not removed",
//                loadedKeySet.lookup(testKey.getName())
//                        .getMeta(SemanticError.Metadata.SEMANTIC_ERROR.getMetadata() + "/#2").getName(),
//                nullValue());
//    }
//
//
//    @After
//    public void tearDown() throws KDB.KDBException {
//        Util.cleanUp(loadedKeySet, kdb);
//    }

}