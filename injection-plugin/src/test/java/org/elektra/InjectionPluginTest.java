package org.elektra;

import org.elektra.errortypes.StructureError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static org.elektra.InjectionPlugin.ROOT_KEY;
import static org.elektra.errortypes.StructureError.Metadata.SECTION_REMOVE;
import static org.junit.jupiter.api.Assertions.*;

class InjectionPluginTest {

    private final static Logger LOG = LoggerFactory.getLogger(InjectionPluginTest.class);

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

        assertNotNull(loadedKeySet.lookup("user/a/b1/c1").getName());
        kdb.set(loadedKeySet, ROOT_KEY);
        injectionPlugin.kdbSet(loadedKeySet, ROOT_KEY);
        assertNull(loadedKeySet.lookup("user/a/b1/c1").getName());
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