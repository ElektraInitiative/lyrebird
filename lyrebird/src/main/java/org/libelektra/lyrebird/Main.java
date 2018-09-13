package org.libelektra.lyrebird;

import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.Plugin;

import java.util.Iterator;

public class Main {

    static {
        System.setProperty("jna.library.path", "/usr/local/lib");
    }

    static String ROOT = "user";

    public static void main(String[] args) {
        Key key = Key.create(ROOT);
        KeySet set = KeySet.create();
        try (KDB kdb = KDB.open(key)) {
            kdb.get(set, key);
            setUpEnumTst(kdb);
            kdb.get(set, key);

            Plugin plugin = new Plugin(Plugin.AvailableSpecificationPlugins.ENUM.getPluginName(), key);
            int resultCode = plugin.kdbSet(set, key);
            System.out.println("Returned Error Code: " + resultCode);

            KeySet.printKeySet(set);
            Key.printKeyAndMeta(key);

//            KeySet newSet = removeAllKeysStartingWith(set, "system/sw");
//            kdb.set(newSet, key);

//            printKS(newSet);


        } catch (KDB.KDBException e) {
            e.printStackTrace();
        }
    }

    public static KeySet getKeySetBelow(String startKey, KeySet set) {
        KeySet result = KeySet.create();
        for (int i = 0; i < set.length(); i++) {  //Traverse the set
            if (set.at(i).getName().startsWith(startKey)) {
                result.append(set.at(i));
            }
        }
        return result;
    }

    public static KeySet removeAllKeysStartingWith(KeySet set, String startKey) {
        Iterator<Key> iterator = set.iterator();
        while (iterator.hasNext()) {
            Key current = iterator.next();
            if (current.getName().startsWith(startKey)) {
                iterator.remove();
            }
        }
        return set;
    }

    public static void setUpEnumTst(KDB kdb) throws KDB.KDBException {
        Key key = Key.create(ROOT);
        KeySet set = KeySet.create();
        kdb.get(set, key);
//        Key checkEnumMeta = Key.create("check/enum", "a, b, c");
        Key k = Key.create(ROOT+"/enumtest", "d");
        k.setMeta("check/enum", "a, b, c");
        set.append(k);
        kdb.set(set, key);
    }

}
