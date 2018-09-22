package org.elektra;

import org.elektra.errortypes.StructureError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static org.elektra.InjectionPlugin.ROOT_KEY;
import static org.elektra.InjectionPlugin.getSeedFromMeta;
import static org.elektra.InjectionPlugin.hasSeedSet;
import static org.elektra.errortypes.StructureError.Metadata.SECTION_DUPLICATE;
import static org.elektra.errortypes.StructureError.Metadata.SECTION_REALLOCATE;
import static org.elektra.errortypes.StructureError.Metadata.SECTION_REMOVE;
import static org.junit.jupiter.api.Assertions.*;

class StructureErrorTest {

    private final static Logger LOG = LoggerFactory.getLogger(StructureErrorTest.class);

    private InjectionPlugin injectionPlugin;
    private KDB kdb;
    private KeySet loadedKeySet;


    @BeforeEach
    public void setUp() throws KDB.KDBException {
        injectionPlugin = new InjectionPlugin();
        kdb = injectionPlugin.kdb;
        loadedKeySet = KeySet.create();
        kdb.get(loadedKeySet, ROOT_KEY);
    }

    @Test
    public void hasSeedMeta_shouldReturnTrue() {
        Key key1 = Key.create(ROOT_KEY +"/a", "a");
        key1.setMeta(InjectionPlugin.SEED_META, "10");
        assertTrue(hasSeedSet(key1));
    }

    @Test
    public void hasSeedMeta_shouldReturnFalse() {
        Key key1 = Key.create(ROOT_KEY +"/a", "a");
        assertFalse(hasSeedSet(key1));
    }

    @Test
    public void getSeed_shouldReturnCorrectInt() {
        int toTest = 523;
        Key key1 = Key.create(ROOT_KEY +"/a", "a");
        key1.setMeta(InjectionPlugin.SEED_META, String.valueOf(toTest));
        assertEquals(getSeedFromMeta(key1), toTest);
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
        assertNotNull(loadedKeySet.lookup(ROOT_KEY+"/a/b1/c1").getName());
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        assertNull(loadedKeySet.lookup(ROOT_KEY+"/a/b1/c1").getName());
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
        assertNotNull(loadedKeySet.lookup(ROOT_KEY+"/a/b1/c1").getName());
        assertNotNull(loadedKeySet.lookup(ROOT_KEY+"/a/b1")
                .getMeta(StructureError.Metadata.SECTION_REALLOCATE.getMetadata()).getName());

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        KeySet.printKeySet(loadedKeySet);

        //This outcome is specific to the set SEED so if you changed the random number it will most likely break
        assertNull(loadedKeySet.lookup(ROOT_KEY+"/a/b1/c1").getName(),
                "Old section is still at the same place");
        assertNotNull(loadedKeySet.lookup(ROOT_KEY+"/b1").getName(),
                "Section not correctly relocated");
        assertNull(loadedKeySet.lookup(ROOT_KEY+"/b1")
                .getMeta(StructureError.Metadata.SECTION_REALLOCATE.getMetadata()).getName(),
                "After section relocate the metadata has to be removed");
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
        assertNotNull(loadedKeySet.lookup(ROOT_KEY+"/a/b1/c1").getName());
        assertNotNull(loadedKeySet.lookup(ROOT_KEY+"/a/b1")
                .getMeta(StructureError.Metadata.SECTION_DUPLICATE.getMetadata()).getName());

        KeySet.printKeySet(loadedKeySet);
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        KeySet.printKeySet(loadedKeySet);

        assertNull(loadedKeySet.lookup(ROOT_KEY+"/a/b1")
                .getMeta(StructureError.Metadata.SECTION_DUPLICATE.getMetadata()).getName(),
                "Metadata was not removed after execution");

        //This outcome is specific to the set SEED so if you changed the random number it will most likely break
        assertNull(loadedKeySet.lookup(ROOT_KEY+"/b1")
                        .getMeta(StructureError.Metadata.SECTION_DUPLICATE.getMetadata()).getName(),
                "Metadata was not removed from duplicated section");
        assertNotNull(loadedKeySet.lookup(ROOT_KEY+"/b1/c1").getName(),
                "Relocation of b1/c1 was not correct");
        assertNotNull(loadedKeySet.lookup(ROOT_KEY+"/b1").getName(),
                "Relocation of b1 was not correct");
        assertNotNull(loadedKeySet.lookup(ROOT_KEY +"/a/b1").getName(),
                "Duplicated section should not be removed");

    }



    @AfterEach
    public void tearDown() throws KDB.KDBException {
        loadedKeySet.rewind();
        Iterator<Key> iterator = loadedKeySet.iterator();
        while (iterator.hasNext()) {
            Key current = iterator.next();
            if (current.getName().startsWith(ROOT_KEY.getName())) {
                iterator.remove();
            }
        }
        kdb.set(loadedKeySet, ROOT_KEY);
        kdb.close(ROOT_KEY);
    }

}