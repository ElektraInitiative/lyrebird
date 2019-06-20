package org.libelektra;

import org.libelektra.errortypes.InjectionData;
import org.libelektra.errortypes.ResourceError;
import org.junit.Before;
import org.junit.Test;
import org.libelektra.service.RandomizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.libelektra.errortypes.ResourceError.Metadata.RESOURCE_ERROR;

public class ResourceErrorTest extends AbstractErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(ResourceErrorTest.class);

    private ResourceError resourceError;
    private KeySet loadedKeySet;
    private Key injectKey;

    List<String> alternativeOptions;

    @Before
    public void setUp() throws KDB.KDBException {
        resourceError = new ResourceError(new RandomizerService(100));
        loadedKeySet = KeySet.create();
        kdbService.get(loadedKeySet, Key.create(TEST_NAMESPACE));
        injectKey = Key.create(INJECT_NAMESPACE + "/some/value", "myResource");
        loadedKeySet.append(injectKey);
        alternativeOptions = new ArrayList<>();
        alternativeOptions.add("/tmp/myfile.txt");
        alternativeOptions.add("/root/secure.txt");
        alternativeOptions.add("/does/not/exist");
    }

    @Test
    public void resourceError_shouldWork() throws Exception {
        injectKey.setMeta(RESOURCE_ERROR.getMetadata() + "/#0", alternativeOptions.get(0));
        injectKey.setMeta(RESOURCE_ERROR.getMetadata() + "/#1", alternativeOptions.get(1));
        injectKey.setMeta(RESOURCE_ERROR.getMetadata() + "/#2", alternativeOptions.get(2));

        KeySet.printKeySet(loadedKeySet);
        KeySet returnedSet = resourceError.apply(new InjectionData(loadedKeySet, injectKey,
                null, APPLY_NAMESPACE, RESOURCE_ERROR));
        KeySet.printKeySet(returnedSet);

        String newString = returnedSet.lookup(APPLY_NAMESPACE ).getString();
        assertThat("None of the provided values were picked in resource error!",
                alternativeOptions.stream().anyMatch(newString::equals),
                is(true));
    }

}