package org.elektra;

import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;

import java.util.Iterator;

import static org.elektra.InjectionPlugin.ROOT_KEY;

public class Util {

    static void cleanUp(KeySet loadedKeySet, KDB kdb) throws KDB.KDBException {
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
