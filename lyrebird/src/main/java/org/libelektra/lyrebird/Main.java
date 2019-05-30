package org.libelektra.lyrebird;

import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.Plugin;
import org.libelektra.lyrebird.runner.ApplicationRunner;
import org.libelektra.lyrebird.runner.impl.CassandraRunner;
import org.libelektra.lyrebird.runner.impl.LCDprocRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

public class Main {

    private final static Logger LOG = LoggerFactory.getLogger(Main.class);

    static {
        System.setProperty("jna.library.path", "/usr/local/lib");
    }

    static String ROOT = "system";

    public static void main(String[] args) throws IOException, InterruptedException {


//        CassandraRunner.startClusterIfNotUp();
//        ApplicationRunner runner = new CassandraRunner();
        ApplicationRunner runner = new LCDprocRunner();
        runner.resetConfiguration();
        runner.start();
//        LOG.info("Sleeping");
        Thread.sleep(5000);
        runner.stop();
//        LOG.info("{}", runner.getLogEntry());
//        mainRun();
    }

    private static void mainRun() {
        Key key = Key.create(ROOT);
        KeySet set = KeySet.create();
        try (KDB kdb = KDB.open(key)) {
            kdb.get(set, key);
            setUpEnumTst(kdb);
            kdb.get(set, key);

            Plugin plugin = new Plugin(Plugin.SpecPlugins.ENUM.getPluginName(), key);
            int resultCode = plugin.kdbSet(set, key);
            logResult(resultCode);

            KeySet.printKeySet(set);
            Key.printKeyAndMeta(key);
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

    private static void logResult(int returncode) {
        if (returncode < 0) {
            LOG.error("Returned Error Code: {}", returncode);
        } else {
            LOG.info("Returned Error Code: {}", returncode);
        }
    }

}
