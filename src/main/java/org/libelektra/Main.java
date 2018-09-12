package org.libelektra;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import org.libelektra.Elektra;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class Main {

    public static void main(String[] args) throws UnsupportedEncodingException {
        Elektra INSTANCE = Native.loadLibrary("elektra", Elektra.class);

        Key key = Key.create("user/user");
        KeySet set = KeySet.create();
        try (KDB kdb = KDB.open(key)) {
            kdb.get(set, key);
        } catch (KDB.KDBException e) {
            e.printStackTrace();
        }

        KeySet modules = KeySet.create();
        Plugin2 plugin = INSTANCE.elektraPluginOpen("enum", modules.get(),
                set.get(), key.get());
        set.rewind();
        int wut = plugin.kdbSet.invoke(plugin, set.get(), key.get());
        System.out.println(wut);

        printKS(set);


        System.out.println(plugin);

//        test.kdbOpen.invoke(null, null);
//        System.out.println(plugin);

    }

    public static void printKS(KeySet set) {
        System.out.println("*============================*");
        for (int i = 0; i < set.length(); i++) {  //Traverse the set
            String keyAndValue = String.format("%s: %s",
                    set.at(i).getName(),          //Fetch the key's name
                    set.at(i).getString());       //Fetch the key's value
            System.out.println(keyAndValue);
            printMetasOfKey(set.at(i));
        }
        System.out.println("*============================*");
    }

    public static void printMetasOfKey(Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            String keyAndValue = String.format("\tMeta: %s: %s",
                    currentKey.getName(),          //Fetch the key's name
                    currentKey.getString());       //Fetch the key's value
            System.out.println(keyAndValue);
            currentKey = key.nextMeta();
        }
    }
}
