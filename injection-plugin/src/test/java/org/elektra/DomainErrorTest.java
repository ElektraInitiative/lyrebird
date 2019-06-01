package org.elektra;

import org.elektra.errortypes.DomainError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.elektra.InjectionPlugin.ROOT_KEY;
import static org.elektra.errortypes.DomainError.Metadata.DOMAIN_ERROR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class DomainErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(DomainErrorTest.class);

    private InjectionPlugin injectionPlugin;
    private KDB kdb;
    private KeySet loadedKeySet;
    private Key testKey;

    List<String> alternativeOptions;


    @Before
    public void setUp() throws KDB.KDBException {
        injectionPlugin = new InjectionPlugin("user/tests/inject");
        kdb = injectionPlugin.kdb;
        loadedKeySet = KeySet.create();
        kdb.get(loadedKeySet, ROOT_KEY);
        testKey = Key.create(ROOT_KEY + "/some/value", "myDomain1");
        loadedKeySet.append(testKey);
        alternativeOptions = new ArrayList<>();
        alternativeOptions.add("myDomain1");
        alternativeOptions.add("myDomain2");
        alternativeOptions.add("myDomain3");
    }

    @Test
    public void domainError_shouldWork() throws Exception {
        testKey.setMeta(DOMAIN_ERROR.getMetadata() + "/#0", alternativeOptions.get(0));
        testKey.setMeta(DOMAIN_ERROR.getMetadata() + "/#1", alternativeOptions.get(1));
        testKey.setMeta(DOMAIN_ERROR.getMetadata() + "/#2", alternativeOptions.get(2));
        testKey.setMeta(InjectionPlugin.SEED_META, "411");

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        KeySet.printKeySet(loadedKeySet);

        String newString = loadedKeySet.lookup(testKey.getName()).getString();
        assertThat("None of the provided values were picked in domain error!",
                alternativeOptions.stream().anyMatch(newString::equals),
                is(true));
        assertThat("Metadata (#0) was not removed",
                loadedKeySet.lookup(testKey.getName())
                        .getMeta(DomainError.Metadata.DOMAIN_ERROR.getMetadata() + "/#0").getName(),
                nullValue());
        assertThat("Metadata (#1) was not removed",
                loadedKeySet.lookup(testKey.getName())
                        .getMeta(DomainError.Metadata.DOMAIN_ERROR.getMetadata() + "/#1").getName(),
                nullValue());
        assertThat("Metadata (#2) was not removed",
                loadedKeySet.lookup(testKey.getName())
                        .getMeta(DomainError.Metadata.DOMAIN_ERROR.getMetadata() + "/#2").getName(),
                nullValue());
    }


    @After
    public void tearDown() throws KDB.KDBException {
        Util.cleanUp(loadedKeySet, kdb);
    }

}