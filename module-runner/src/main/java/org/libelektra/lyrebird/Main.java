package org.libelektra.lyrebird;

import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.Plugin;
import org.libelektra.lyrebird.model.LogEntry;
import org.libelektra.lyrebird.runner.ApplicationRunner;
import org.libelektra.lyrebird.writer.LcdprocCsvOutputWriter;
import org.libelektra.model.InjectionResult;
import org.libelektra.service.KDBService;
import org.libelektra.service.ManualInjectionService;
import org.libelektra.service.SpecificationEnforcer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@SpringBootApplication(scanBasePackages = "org.libelektra")
public class Main implements CommandLineRunner {

    private final static Logger LOG = LoggerFactory.getLogger(Main.class);

    private final ApplicationRunner runner;
    private final KDBService kdbService;
    private Collection<LogEntry> allLogs;
    private final LcdprocCsvOutputWriter outputWriter;
    private ManualInjectionService manualInjectionService;


    private int timeout;
    private int iterations;
    private final String specialInjections;

    private Environment env;

    static {
        System.setProperty("jna.library.path", "/usr/local/lib");
    }

    @Autowired
    public Main(ApplicationRunner runner,
                KDBService kdbService,
                @Value("${injection.iterations}") int iterations,
                @Value("${injection.timeout}") int timeout,
                @Value("${special.injections}") String specialInjections,
                Environment env,
                LcdprocCsvOutputWriter outputWriter,
                Optional<ManualInjectionService> manualInjectionService) {
        this.timeout = timeout;
        this.specialInjections = specialInjections;
        this.runner = runner;
        this.kdbService = kdbService;
        this.iterations = iterations;
        this.env = env;
        this.outputWriter = outputWriter;
        allLogs = new ArrayList<>();
        manualInjectionService.ifPresent(service -> {this.manualInjectionService = service;});
    }

    @Override
    public void run(String... args) throws Exception {
        boolean manualInjectionsActive = Arrays.asList(env.getActiveProfiles()).contains("manual");
        if (manualInjectionsActive) {
            runManualInjections();
            return;
        }
        for (int i = 0; i < iterations; i++) {
            runner.resetConfiguration();
            InjectionResult injectionResult = runner.injectInConfiguration();
            if (injectionResult.errorCaughtBySpecification()) {
                // No need to run the program if the specification caught the error
                allLogs.add(runner.getLogEntry());
                continue;
            }
            if (!injectionResult.wasInjectionSuccessful()) {
                // Case toggling a number for example is impossible and yields false as result
                i--;
                continue;
            }
            runProgram(timeout);
            stopProgram();
            allLogs.add(runner.getLogEntry());
        }
        outputWriter.write(allLogs);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(Main.class, args);
        LOG.info("APPLICATION FINISHED");

//        CassandraRunner.startClusterIfNotUp();
//        ApplicationRunner runner = new CassandraRunner();
    }

    private void runManualInjections() throws IOException, InterruptedException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Path customInjections = Paths.get(classLoader.getResource(specialInjections).getPath());
        List<Path> allFiles = Files.walk(customInjections)
                .filter(x -> !Files.isDirectory(x))
                .collect(Collectors.toList());
        for (Path file : allFiles) {
            manualInjectionService.reset();
            manualInjectionService.inject(file);
            runProgram(timeout);
            stopProgram();
            allLogs.add(runner.getLogEntry());
        }
        outputWriter.write(allLogs);
    }

    private void stopProgram() throws IOException, InterruptedException {
        runner.stop();
    }

    private void runProgram(int timeout) throws InterruptedException {
        CompletableFuture.runAsync(() -> {
            try {
                runner.start();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread.sleep(timeout);
    }

    public static void mmain(String[] args) {
        mainRun();
    }

    private static void mainRun() {
        Key key = Key.create("user/enumtest");
        KeySet set = KeySet.create();
        try (KDB kdb = KDB.open(key)) {
            kdb.get(set, key);
            setUpEnumTst(set);

            KeySet.printKeySet(set);
            Plugin plugin = new Plugin(SpecificationEnforcer.SpecPlugins.TYPE.getPluginName(), key);
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

    public static void setUpEnumTst(KeySet set) throws KDB.KDBException {
        Key k = Key.create("user/enumtest/test", "c");
        k.setMeta("type", "enum");
        k.setMeta("check/enum", "#2");
        k.setMeta("check/enum/#0", "a");
        k.setMeta("check/enum/#1", "b");
        k.setMeta("check/enum/#2", "c");
        set.append(k);
    }

    private static void logResult(int returncode) {
        if (returncode < 0) {
            LOG.error("Returned Error Code: {}", returncode);
        } else {
            LOG.info("Returned Error Code: {}", returncode);
        }
    }
}
