package org.libelektra;

import org.junit.Before;
import org.junit.Test;
import org.libelektra.errortypes.InjectionData;
import org.libelektra.errortypes.StructureError;
import org.libelektra.service.RandomizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.libelektra.errortypes.StructureError.Metadata.SECTION_DUPLICATE;
import static org.libelektra.errortypes.StructureError.Metadata.SECTION_REALLOCATE;

public class StructureErrorTest extends AbstractErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(StructureErrorTest.class);

    private StructureError structureError;
    private KeySet loadedKeySet;
    private RandomizerService randomizerService;


    @Before
    public void setUp() throws KDB.KDBException {
        randomizerService = new RandomizerService(21);
        structureError = new StructureError(APPLY_NAMESPACE, randomizerService);
        loadedKeySet = KeySet.create();
        kdbService.get(loadedKeySet, Key.create(TEST_NAMESPACE));
    }

    @Test
    public void removeSection_shouldWork() throws Exception {
        Key key1 = Key.create(APPLY_NAMESPACE +"/a", "a");
        Key key2 = Key.create(APPLY_NAMESPACE +"/a/b1", "b1");
        Key key3 = Key.create(APPLY_NAMESPACE +"/a/b2", "b2");
        Key key4 = Key.create(APPLY_NAMESPACE +"/a/b1/c1", "c1");
        Key key5 = Key.create(APPLY_NAMESPACE +"/a/b2/c2", "c2");
        loadedKeySet.append(key1);
        loadedKeySet.append(key2);
        loadedKeySet.append(key3);
        loadedKeySet.append(key4);
        loadedKeySet.append(key5);


        KeySet.printKeySet(loadedKeySet);
        assertThat(loadedKeySet.lookup(APPLY_NAMESPACE+"/a/b1/c1").getName(), notNullValue());
        kdbService.set(loadedKeySet, APPLY_NAMESPACE);
        KeySet returnedKeySet = structureError.apply(new InjectionData(loadedKeySet, null, null,APPLY_NAMESPACE +
                "/a/b1", StructureError.Metadata.SECTION_REMOVE));
        kdbService.set(returnedKeySet, APPLY_NAMESPACE);
        assertThat(returnedKeySet.lookup(APPLY_NAMESPACE+"/a/b1/c1").getName(), nullValue());
        KeySet.printKeySet(loadedKeySet);
    }

    @Test
    public void relocateSection_shouldWork() throws Exception {
        Key key1 = Key.create(APPLY_NAMESPACE +"/a", "a");
        Key key2 = Key.create(APPLY_NAMESPACE +"/a/b1", "b1");
        Key key3 = Key.create(APPLY_NAMESPACE +"/a/b2", "b2");
        Key key4 = Key.create(APPLY_NAMESPACE +"/a/b1/c1", "c1");
        Key key5 = Key.create(APPLY_NAMESPACE +"/a/b2/c2", "c2");
        loadedKeySet.append(key1);
        loadedKeySet.append(key2);
        loadedKeySet.append(key3);
        loadedKeySet.append(key4);
        loadedKeySet.append(key5);

        KeySet.printKeySet(loadedKeySet);
        kdbService.set(loadedKeySet, APPLY_NAMESPACE);
        KeySet returnedKeySet = structureError.apply(new InjectionData(loadedKeySet, null, null,APPLY_NAMESPACE +
                "/a/b1", SECTION_REALLOCATE));
        kdbService.set(returnedKeySet, APPLY_NAMESPACE);
        KeySet.printKeySet(loadedKeySet);

        //This outcome is specific to the set SEED so if you changed the random number it will most likely break
        assertThat("Old section is still at the same place",
                returnedKeySet.lookup(APPLY_NAMESPACE+"/a/b1/c1").getName(),
                nullValue());
        assertThat("Section not correctly relocated",
                returnedKeySet.lookup(APPLY_NAMESPACE+"/a/b2/c2/b1/c1").getName(), // This test may file depending on the seed
                notNullValue());
    }

    @Test
    public void duplicateSection_shouldWork() throws Exception {
        Key key1 = Key.create(APPLY_NAMESPACE +"/a", "a");
        Key key2 = Key.create(APPLY_NAMESPACE +"/a/b1", "b1");
        Key key3 = Key.create(APPLY_NAMESPACE +"/a/b2", "b2");
        Key key4 = Key.create(APPLY_NAMESPACE +"/a/b1/c1", "c1");
        Key key5 = Key.create(APPLY_NAMESPACE +"/a/b2/c2", "c2");
        loadedKeySet.append(key1);
        loadedKeySet.append(key2);
        loadedKeySet.append(key3);
        loadedKeySet.append(key4);
        loadedKeySet.append(key5);
        randomizerService.setSeed(12);

        KeySet.printKeySet(loadedKeySet);
        kdbService.set(loadedKeySet, APPLY_NAMESPACE);
        KeySet returnedKeySet = structureError.apply(new InjectionData(loadedKeySet, null, null,APPLY_NAMESPACE + "/a/b1", SECTION_DUPLICATE));
        KeySet.printKeySet(loadedKeySet);

        //This outcome is specific to the set SEED so if you changed the random number it will most likely break
        assertThat("Relocation of b1/c1 was not correct",
                returnedKeySet.lookup(APPLY_NAMESPACE+"/b1/c1").getName(),
                notNullValue());
        assertThat("Relocation of b1 was not correct",
                returnedKeySet.lookup(APPLY_NAMESPACE+"/b1").getName(),
                notNullValue());
        assertThat("Duplicated section should not be removed",
                returnedKeySet.lookup(APPLY_NAMESPACE +"/a/b1").getName(),
                notNullValue());

    }

}