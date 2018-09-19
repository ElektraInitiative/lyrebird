package org.elektra;

import org.elektra.errortypes.StructureError;
import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static java.util.Objects.nonNull;

public class InjectionPlugin {

    private final static Logger LOG = LoggerFactory.getLogger(InjectionPlugin.class);

    private static String name = "injection";
    public static Key ROOT_KEY = Key.create("user");
    public KDB kdb;

    private final StructureError structureError = new StructureError();

    public InjectionPlugin() {
       kdb = KDB.open(ROOT_KEY);
    }

    public InjectionPlugin(String pluginName, Key errorKey) {
    }

    public InjectionPlugin(String pluginName, KeySet modules, KeySet config, Key errorKey) {

    }

    public KeySet getConfig() {
        return KeySet.create();
    }

    public int kdbOpen(Key errorKey) {
        return 0;
    }

    public int kdbClose(Key errorKey) {
        return 0;
    }

    public int kdbSet(KeySet keySet, Key errorKey) {
        keySet.rewind();
        Iterator<Key> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            Key current = iterator.next();
            if (hasStructureMetadata(current)) {
                keySet = structureError.applyStructureError(keySet, current);
            }
        }

        try {
            kdb.set(keySet, ROOT_KEY);
        } catch (KDB.KDBException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int kdbGet(KeySet keySet, Key errorKey) {
        keySet.rewind();
        return 0;
    }

    public int kdbError(KeySet keySet, Key errorKey) {
        keySet.rewind();
        return 0;
    }

    public String getName() {
        return name;
    }

    private boolean hasStructureMetadata(Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (StructureError.Metadata.hasMetadata(currentKey.getName())) {
                return true;
            }
            currentKey = key.nextMeta();
        }
        return false;
    }

}
