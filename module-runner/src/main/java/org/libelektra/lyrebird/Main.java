package org.libelektra.lyrebird;

import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.Plugin;
import org.libelektra.lyrebird.model.LogEntry;
import org.libelektra.lyrebird.runner.ApplicationRunner;
import org.libelektra.lyrebird.writer.LcdprocCsvOutputWriter;
import org.libelektra.service.KDBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static org.libelektra.service.KDBService.ROOT;

@SpringBootApplication(scanBasePackages = "org.libelektra")
public class Main implements CommandLineRunner {

    private final static Logger LOG = LoggerFactory.getLogger(Main.class);

    private final ApplicationRunner runner;
    private final KDBService kdbService;
    private Collection<LogEntry> allLogs;
    private final int iterations;
    private final LcdprocCsvOutputWriter outputWriter;

    static {
        System.setProperty("jna.library.path", "/usr/local/lib");
    }

    public Main(ApplicationRunner runner,
                KDBService kdbService,
                @Value("${injection.iterations}") int iterations, LcdprocCsvOutputWriter outputWriter) {
        this.runner = runner;
        this.kdbService = kdbService;
        this.iterations = iterations;
        this.outputWriter = outputWriter;
        allLogs = new ArrayList<>();
    }

    @Override
    public void run(String... args) throws Exception {
        for (int i = 0; i < iterations; i++) {
            runner.resetConfiguration();
            boolean successful = runner.injectInConfiguration();
            if (!successful) {
                i--;
                continue;
            }
            CompletableFuture.runAsync(() -> {
                try {
                    runner.start();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            Thread.sleep(300);
            runner.stop();
            allLogs.add(runner.getLogEntry());
        }
        outputWriter.write(allLogs);
        allLogs.stream().filter(entry -> !entry.getLogMessage().startsWith("Error opening")).forEach(entry -> LOG.info(entry.toString()));
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(Main.class, args);
        LOG.info("APPLICATION FINISHED");

//        CassandraRunner.startClusterIfNotUp();
//        ApplicationRunner runner = new CassandraRunner();

//        runner.resetConfiguration();

//        runner.close();
//        runner.start();
//        LOG.info("Sleeping");
//        Thread.sleep(5000);
//        runner.stop();
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
