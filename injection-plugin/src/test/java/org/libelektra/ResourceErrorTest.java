package org.libelektra;

import org.libelektra.errortypes.ResourceError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.libelektra.service.KDBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.libelektra.InjectionPlugin.ROOT_KEY;
import static org.libelektra.errortypes.ResourceError.Metadata.RESOURCE_ERROR;

public class ResourceErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(ResourceErrorTest.class);

    private InjectionPlugin injectionPlugin;
    private KDB kdb;
    private KeySet loadedKeySet;
    private Key testKey;

    List<String> alternativeOptions;

    @Before
    public void setUp() throws KDB.KDBException {
        KDBService kdbService = new KDBService();
        injectionPlugin = new InjectionPlugin(kdbService);
        kdb = kdbService.getInstance();
        loadedKeySet = KeySet.create();
        kdb.get(loadedKeySet, ROOT_KEY);
        testKey = Key.create(ROOT_KEY + "/some/value", "/my/valid/path");
        loadedKeySet.append(testKey);
        alternativeOptions = new ArrayList<>();
        alternativeOptions.add("/tmp/myfile.txt");
        alternativeOptions.add("/root/secure.txt");
        alternativeOptions.add("/does/not/exist");
    }

    @Test
    public void resourceError_shouldWork() throws Exception {
        testKey.setMeta(RESOURCE_ERROR.getMetadata() + "/#0", alternativeOptions.get(0));
        testKey.setMeta(RESOURCE_ERROR.getMetadata() + "/#1", alternativeOptions.get(1));
        testKey.setMeta(RESOURCE_ERROR.getMetadata() + "/#2", alternativeOptions.get(2));
        testKey.setMeta(InjectionPlugin.SEED_META, "411");

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY, "user/tests/injectplugin");
        KeySet.printKeySet(loadedKeySet);

        String newString = loadedKeySet.lookup(testKey.getName()).getString();
        assertThat("None of the provided values were picked in resource error!",
                alternativeOptions.stream().anyMatch(newString::equals),
                is(true));
        assertThat("Metadata (#0) was not removed",
                loadedKeySet.lookup(testKey.getName())
                        .getMeta(ResourceError.Metadata.RESOURCE_ERROR.getMetadata() + "/#0").getName(),
                nullValue());
        assertThat("Metadata (#1) was not removed",
                loadedKeySet.lookup(testKey.getName())
                        .getMeta(ResourceError.Metadata.RESOURCE_ERROR.getMetadata() + "/#1").getName(),
                nullValue());
        assertThat("Metadata (#2) was not removed",
                loadedKeySet.lookup(testKey.getName())
                        .getMeta(ResourceError.Metadata.RESOURCE_ERROR.getMetadata() + "/#2").getName(),
                nullValue());
    }

    @After
    public void tearDown() throws KDB.KDBException {
        Util.cleanUp(loadedKeySet, kdb);
    }

}