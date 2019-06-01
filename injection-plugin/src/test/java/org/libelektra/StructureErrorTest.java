package org.libelektra;

import org.libelektra.errortypes.StructureError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.libelektra.InjectionPlugin.*;
import static org.libelektra.errortypes.StructureError.Metadata.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class StructureErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(StructureErrorTest.class);

    private InjectionPlugin injectionPlugin;
    private KDB kdb;
    private KeySet loadedKeySet;


    @Before
    public void setUp() throws KDB.KDBException {
        injectionPlugin = new InjectionPlugin("user/tests/inject");
        kdb = injectionPlugin.kdb;
        loadedKeySet = KeySet.create();
        kdb.get(loadedKeySet, ROOT_KEY);
    }

    @Test
    public void hasSeedMeta_shouldReturnTrue() {
        Key key1 = Key.create(ROOT_KEY +"/a", "a");
        key1.setMeta(InjectionPlugin.SEED_META, "10");
        assertThat(hasSeedSet(key1), is(true));
    }

    @Test
    public void hasSeedMeta_shouldReturnFalse() {
        Key key1 = Key.create(ROOT_KEY +"/a", "a");
        assertThat(hasSeedSet(key1), is(false));
    }

    @Test
    public void getSeed_shouldReturnCorrectInt() {
        int toTest = 523;
        Key key1 = Key.create(ROOT_KEY +"/a", "a");
        key1.setMeta(InjectionPlugin.SEED_META, String.valueOf(toTest));
        assertThat(getSeedFromMeta(key1), is(toTest));
    }

    @Test
    public void removeSection_shouldWork() throws Exception {
        Key key1 = Key.create(ROOT_KEY +"/a", "a");
        Key key2 = Key.create(ROOT_KEY +"/a/b1", "b1");
        key2.setMeta(SECTION_REMOVE.getMetadata(), "");
        Key key3 = Key.create(ROOT_KEY +"/a/b2", "b2");
        Key key4 = Key.create(ROOT_KEY +"/a/b1/c1", "c1");
        Key key5 = Key.create(ROOT_KEY +"/a/b2/c2", "c2");
        loadedKeySet.append(key1);
        loadedKeySet.append(key2);
        loadedKeySet.append(key3);
        loadedKeySet.append(key4);
        loadedKeySet.append(key5);

        KeySet.printKeySet(loadedKeySet);
        assertThat(loadedKeySet.lookup(ROOT_KEY+"/a/b1/c1").getName(), notNullValue());
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        assertThat(loadedKeySet.lookup(ROOT_KEY+"/a/b1/c1").getName(), nullValue());
        KeySet.printKeySet(loadedKeySet);
    }

    @Test
    public void relocateSection_shouldWork() throws Exception {
        Key key1 = Key.create(ROOT_KEY +"/a", "a");
        Key key2 = Key.create(ROOT_KEY +"/a/b1", "b1");
        key2.setMeta(SECTION_REALLOCATE.getMetadata(), "");
        key2.setMeta(InjectionPlugin.SEED_META, "411");
        Key key3 = Key.create(ROOT_KEY +"/a/b2", "b2");
        Key key4 = Key.create(ROOT_KEY +"/a/b1/c1", "c1");
        Key key5 = Key.create(ROOT_KEY +"/a/b2/c2", "c2");
        loadedKeySet.append(key1);
        loadedKeySet.append(key2);
        loadedKeySet.append(key3);
        loadedKeySet.append(key4);
        loadedKeySet.append(key5);

        //Check correct prerequisites
        assertThat(loadedKeySet.lookup(ROOT_KEY+"/a/b1/c1").getName(), notNullValue());
        assertThat(loadedKeySet.lookup(ROOT_KEY+"/a/b1")
                .getMeta(StructureError.Metadata.SECTION_REALLOCATE.getMetadata()).getName(), notNullValue());

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        KeySet.printKeySet(loadedKeySet);

        //This outcome is specific to the set SEED so if you changed the random number it will most likely break
        assertThat("Old section is still at the same place",
                loadedKeySet.lookup(ROOT_KEY+"/a/b1/c1").getName(),
                nullValue());
        assertThat("Section not correctly relocated",
                loadedKeySet.lookup(ROOT_KEY+"/b1").getName(),
                notNullValue());
        assertThat("After section relocate the metadata has to be removed",
                loadedKeySet.lookup(ROOT_KEY+"/b1")
                .getMeta(StructureError.Metadata.SECTION_REALLOCATE.getMetadata()).getName(),
                nullValue());
    }

    @Test
    public void duplicateSection_shouldWork() throws Exception {
        Key key1 = Key.create(ROOT_KEY +"/a", "a");
        Key key2 = Key.create(ROOT_KEY +"/a/b1", "b1");
        key2.setMeta(SECTION_DUPLICATE.getMetadata(), "");
        key2.setMeta(InjectionPlugin.SEED_META, "411");
        Key key3 = Key.create(ROOT_KEY +"/a/b2", "b2");
        Key key4 = Key.create(ROOT_KEY +"/a/b1/c1", "c1");
        Key key5 = Key.create(ROOT_KEY +"/a/b2/c2", "c2");
        loadedKeySet.append(key1);
        loadedKeySet.append(key2);
        loadedKeySet.append(key3);
        loadedKeySet.append(key4);
        loadedKeySet.append(key5);

        //Check correct prerequisites
        assertThat(loadedKeySet.lookup(ROOT_KEY+"/a/b1/c1").getName(), notNullValue());
        assertThat(loadedKeySet.lookup(ROOT_KEY+"/a/b1")
                .getMeta(StructureError.Metadata.SECTION_DUPLICATE.getMetadata()).getName(), notNullValue());

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        KeySet.printKeySet(loadedKeySet);

        assertThat("Metadata was not removed after execution",
                loadedKeySet.lookup(ROOT_KEY+"/a/b1")
                .getMeta(StructureError.Metadata.SECTION_DUPLICATE.getMetadata()).getName(),
                nullValue());

        //This outcome is specific to the set SEED so if you changed the random number it will most likely break
        assertThat("Metadata was not removed from duplicated section",
                loadedKeySet.lookup(ROOT_KEY+"/b1")
                        .getMeta(StructureError.Metadata.SECTION_DUPLICATE.getMetadata()).getName(),
                nullValue());
        assertThat("Relocation of b1/c1 was not correct",
                loadedKeySet.lookup(ROOT_KEY+"/b1/c1").getName(),
                notNullValue());
        assertThat("Relocation of b1 was not correct",
                loadedKeySet.lookup(ROOT_KEY+"/b1").getName(),
                notNullValue());
        assertThat("Duplicated section should not be removed",
                loadedKeySet.lookup(ROOT_KEY +"/a/b1").getName(),
                notNullValue());

    }



    @After
    public void tearDown() throws KDB.KDBException {
        Util.cleanUp(loadedKeySet, kdb);
    }

}