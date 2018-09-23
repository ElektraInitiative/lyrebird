package org.elektra;

import org.elektra.errortypes.StructureError;
import org.elektra.errortypes.TypoError;
import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static java.util.Objects.nonNull;

public class InjectionPlugin {

    public static final String SEED_META ="inject/rand/seed";

    private final static Logger LOG = LoggerFactory.getLogger(InjectionPlugin.class);

    private static String name = "injection";
    public static Key ROOT_KEY = Key.create("user/injection/test");
    public KDB kdb;

    private final StructureError structureError = new StructureError();
    private final TypoError typoError = new TypoError();

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
            } else if (hasTypoMetadata(current)) {
                keySet = typoError.applyTypoError(keySet, current);
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

    private boolean hasTypoMetadata(Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (TypoError.Metadata.hasMetadata(currentKey.getName())) {
                return true;
            }
            currentKey = key.nextMeta();
        }
        return false;
    }



    public static boolean hasSeedSet(Key key) {
        return nonNull(key.getMeta(SEED_META).getName());
    }

    public static int getSeedFromMeta(Key key) {
        if (hasSeedSet(key)) {
            return key.getMeta(SEED_META).getInteger();
        }
        return 0;
    }

}
