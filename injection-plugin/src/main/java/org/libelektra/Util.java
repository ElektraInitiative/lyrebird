package org.libelektra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import static org.libelektra.InjectionPlugin.ROOT_KEY;

public class Util {

    private final static Logger LOG = LoggerFactory.getLogger(Util.class);

    public static void executeCommand(String command) throws IOException {
        LOG.trace(command);
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(command);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        // read the output from the command
        String s = null;
        while ((s = stdInput.readLine()) != null)
        {
            LOG.debug("stout: {}" ,s);
        }

        // read any errors from the attempted command
        while ((s = stdError.readLine()) != null)
        {
            LOG.debug("stderr: {}" ,s);
        }
    }

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

    public static void printKeySetUnderPath(String path) {
        Key key = Key.create(path);
        KeySet set = KeySet.create();
        try (KDB kdb = KDB.open(key)) {
            kdb.get(set, key);
            KeySet.printKeySet(set);
        } catch (KDB.KDBException e) {
            e.printStackTrace();
        }
    }
}
