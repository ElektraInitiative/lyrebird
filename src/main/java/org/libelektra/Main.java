package org.libelektra;

import java.io.UnsupportedEncodingException;
import static java.util.Objects.nonNull;

public class Main {

    public static void main(String[] args) throws UnsupportedEncodingException {
        Key key = Key.create("user/user");
        KeySet set = KeySet.create();
        try (KDB kdb = KDB.open(key)) {
            kdb.get(set, key);

            Plugin plugin = new Plugin("network", key);
            int resultCode = plugin.kdbSet(set, key);
            System.out.println("Returned Error Code: "+  resultCode);

            printKS(set);
//            printKeyAndMeta(Plugin.DEFAULT_ERROR_KEY);

            printKeyAndMeta(key);

        } catch (KDB.KDBException e) {
            e.printStackTrace();
        }
    }

    public static void printKS(KeySet set) {
        System.out.println("*======== KeySet Information ========*");
        for (int i = 0; i < set.length(); i++) {  //Traverse the set
            printKeyAndMeta(set.at(i));
        }
        System.out.println("*====================================*");
    }

    public static void printKeyAndMeta(Key key) {
        key.rewindMeta();
        String keyAndValue = String.format("%s: %s",
                key.getName(),          //Fetch the key's name
                key.getString());       //Fetch the key's value
        System.out.println(keyAndValue);
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            String metaKeyAndValue = String.format("\tMeta [%s: %s]",
                    currentKey.getName(),          //Fetch the key's name
                    currentKey.getString());       //Fetch the key's value
            System.out.println(metaKeyAndValue);
            currentKey = key.nextMeta();
        }
    }
}
