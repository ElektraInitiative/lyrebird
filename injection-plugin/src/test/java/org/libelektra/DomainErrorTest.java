package org.libelektra;

import org.junit.Before;
import org.junit.Test;
import org.libelektra.errortypes.DomainError;
import org.libelektra.model.InjectionData;
import org.libelektra.service.RandomizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.libelektra.errortypes.DomainError.Metadata.DOMAIN_ERROR;

public class DomainErrorTest extends AbstractErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(DomainErrorTest.class);

    private DomainError domainError;
    private KeySet loadedKeySet;
    private Key injectKey;

    List<String> alternativeOptions;

    @Before
    public void setUp() throws KDB.KDBException {
        domainError = new DomainError(new RandomizerService(100));
        loadedKeySet = KeySet.create();
        kdbService.get(loadedKeySet, Key.create(TEST_NAMESPACE));
        injectKey = Key.create(INJECT_NAMESPACE + "/some/value", "validDomain");
        loadedKeySet.append(injectKey);
        alternativeOptions = new ArrayList<>();
        alternativeOptions.add("myDomain1");
        alternativeOptions.add("myDomain2");
        alternativeOptions.add("myDomain3");
    }

    @Test
    public void domainError_shouldWork() throws Exception {
        injectKey.setMeta(DOMAIN_ERROR.getMetadata() + "/#0", alternativeOptions.get(0));
        injectKey.setMeta(DOMAIN_ERROR.getMetadata() + "/#1", alternativeOptions.get(1));
        injectKey.setMeta(DOMAIN_ERROR.getMetadata() + "/#2", alternativeOptions.get(2));

        KeySet.printKeySet(loadedKeySet);
        KeySet returnedSet = domainError.apply(new InjectionData(loadedKeySet, injectKey,
                null, APPLY_NAMESPACE + "/domain", DOMAIN_ERROR));
        KeySet.printKeySet(loadedKeySet);
        kdbService.set(returnedSet, APPLY_NAMESPACE);

        String newString = returnedSet.lookup(APPLY_NAMESPACE + "/domain").getString();
        assertThat("None of the provided values were picked in domain error",
                alternativeOptions.stream().anyMatch(newString::equals),
                is(true));
    }

}