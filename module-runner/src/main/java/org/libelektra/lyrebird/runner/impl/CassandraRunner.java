package org.libelektra.lyrebird.runner.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.Tailer;
import org.libelektra.*;
import org.libelektra.lyrebird.model.LogEntry;
import org.libelektra.lyrebird.runner.ApplicationRunner;
import org.libelektra.lyrebird.service.LogListenerService;
import org.libelektra.model.InjectionConfiguration;
import org.libelektra.model.InjectionDataResult;
import org.libelektra.model.InjectionResult;
import org.libelektra.model.SpecificationDataResult;
import org.libelektra.service.KDBService;
import org.libelektra.service.RandomizerService;
import org.libelektra.service.SpecificationEnforcer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.libelektra.lyrebird.model.LogEntry.RESULT_TYPE.SPECIFICATION_CAUGHT;

@Component
@Profile("cassandra")
public class CassandraRunner implements ApplicationRunner {

    private final static Logger LOG = LoggerFactory.getLogger(CassandraRunner.class);

    private static final String USER = "wespe";
    private static final String CASSANDRA_VERSION = "3.11.4";
    private static final int CASSANDRA_NODES = 3;
    private static final String CLUSTER_NAME = "LyreBirdCluster";
    private static final String TEST_NODE = "node1";
    private static final String LOG_LOCATION =
            String.format("/home/%s/.ccm/%s/node1/logs/system.log", USER, CLUSTER_NAME);

    private KDBService kdbService;
    private RandomizerService randomizerService;
    private KeySet errorConfigKeySet;
    private KeySet specificationKeySet;
    private LogEntry currentLogEntry;
    private final InjectionConfiguration injectionConfiguration;
    private final InjectionPlugin injectionPlugin;
    private final SpecificationEnforcer specificationEnforcer;
    private SpecificationDataResult specificationDataResult;

    public final static String CASSANDRA_RUN_CONFIG = "cassandra/cassandra.yaml";
    public final static String CASSANDRA_INJECT_CONFIG = "cassandra/cassandra-inject.ini";
    public final static String CASSANDRA_SPEC_CONFIG = "cassandra/cassandra-spec.ini";
    public final static String TEMP_ERROR_CONFIG = String.format("/home/%s/.ccm/%s/node1/conf/cassandra.yaml", USER, CLUSTER_NAME);
    public final static String TEMP_SPEC_CONFIG = "/tmp/cassandra-spec.conf";

    private Tailer tailer;
    private LogListenerService tailerListener;

    @Autowired
    public CassandraRunner(KDBService kdbService,
                         RandomizerService randomizerService,
                         SpecificationEnforcer specificationEnforcer,
                         InjectionPlugin injectionPlugin,
                         InjectionConfiguration injectionConfiguration
    ) throws KDB.KDBException {
        this.injectionConfiguration = injectionConfiguration;
        this.specificationEnforcer = specificationEnforcer;
        this.injectionPlugin = injectionPlugin;
        this.kdbService = kdbService;
        this.randomizerService = randomizerService;
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            File errorConfigFile = new File(classLoader.getResource(CASSANDRA_INJECT_CONFIG).getFile());
            File specConfigFile = new File(classLoader.getResource(CASSANDRA_SPEC_CONFIG).getFile());
            //TODO: Filter for settings with numbers
            File runConfig = new File(TEMP_ERROR_CONFIG);
            File specConfig = new File(TEMP_SPEC_CONFIG);
            FileUtils.copyFile(errorConfigFile, runConfig);
            FileUtils.copyFile(specConfigFile, specConfig);
            currentLogEntry = new LogEntry();
            Util.executeCommand(String.format("kdb mount %s %s ni", TEMP_ERROR_CONFIG, injectionConfiguration.getInjectPath()));
            Util.executeCommand(String.format("kdb mount %s %s ni", TEMP_SPEC_CONFIG, injectionConfiguration.getSpecPath()));
        } catch (NullPointerException e) {
            LOG.error("Could not find configuration for {}", CASSANDRA_RUN_CONFIG);
        } catch (IOException e) {
            e.printStackTrace();
        }
        kdbService.initKDB();
        errorConfigKeySet = kdbService.getKeySetBelowPath(injectionConfiguration.getInjectPath());
        specificationKeySet = kdbService.getKeySetBelowPath(injectionConfiguration.getSpecPath());
    }

    public static void startClusterIfNotUp() throws IOException, InterruptedException {

        String[] isUpCommand = new String[]{"su", USER, "-c", "ccm status"};
        Process process = new ProcessBuilder(isUpCommand)
                .redirectErrorStream(true)
                .start();

        List<String> output = new ArrayList<>();
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
        }

        boolean isUp = output.stream().anyMatch(str -> str.equals("node2: UP")); //Node 1 can be down as it is our testnode
        if (isUp) {
            LOG.info("Cassandra Cluster is already up and working");
        } else {
            LOG.info("Starting Cassandra Cluster with name {} as user {}", CLUSTER_NAME, USER);
            String ccmStartCommand = String.format("ccm create %s -v %s -n %d -s",
                    CLUSTER_NAME, CASSANDRA_VERSION, CASSANDRA_NODES);
            String[] command = new String[]{"su", USER, "-c", ccmStartCommand};
            Process p = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            int result = p.waitFor();
            LOG.info("Started cluster. Process return [{}]", result);
        }

        //Stop node1
        stopTestNode();
        LOG.debug("Stopping {} to start testing", TEST_NODE);
    }

    @Override
    public void start() throws IOException, InterruptedException {
        String ccmStartCommand = String.format("ccm %s start", TEST_NODE);
        String[] command = new String[]{"su", USER, "-c", ccmStartCommand};
        LOG.debug("Starting {}", TEST_NODE);
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        tailerListener = new LogListenerService();
        File file = new File(LOG_LOCATION);
        tailer = Tailer.create(file, tailerListener);
        if (process.waitFor(10, TimeUnit.SECONDS)) {
            LOG.error("Process did not stop!");
        }
    }

    @Override
    public void stop() throws IOException, InterruptedException {
        stopTestNode();
        tailer.stop();
        handleLogs(tailerListener.getLogsAndReset());

        //Clear logging for new run
        File file = new File(LOG_LOCATION);
        try(PrintWriter writer = new PrintWriter(file)) {
            writer.print("");
        }
    }

    @Override
    public InjectionResult injectInConfiguration() throws KDB.KDBException {
        int nextRandom = randomizerService.getNextInt(errorConfigKeySet.length());
        Key injectKey = errorConfigKeySet.at(nextRandom);
        String path = injectKey.getName().replace(injectionConfiguration.getInjectPath(), injectionConfiguration.getParentPath());
        KeySet configKeySet = kdbService.getKeySetBelowPath(injectionConfiguration.getParentPath());
        InjectionDataResult injectionDataResult = injectionPlugin.kdbSet(configKeySet, injectKey, path);
        currentLogEntry.setInjectionDataResult(injectionDataResult);
        if (injectionConfiguration.isWithSpecification()) {
            this.specificationDataResult = specificationEnforcer.checkSpecification(specificationKeySet,
                    configKeySet, injectKey);
            currentLogEntry.setSpecificationDataResult(this.specificationDataResult);
            if (this.specificationDataResult.hasDetectedError()) {
                currentLogEntry.setErrorLogEntry(this.specificationDataResult.getErrorMessage());
                currentLogEntry.setResultType(SPECIFICATION_CAUGHT);
            }
        }
        injectAdditionalContextDependant(configKeySet, injectionDataResult);
        return new InjectionResult(injectionDataResult, specificationDataResult);
    }

    @Override
    public void resetConfiguration() {

    }

    @Override
    public LogEntry getLogEntry() {
        return currentLogEntry;
    }

    private void handleLogs(List<String> logs) {
        currentLogEntry = new LogEntry();
        List<String> errorLogs = logs.stream().filter(str -> str.contains("ERROR")).collect(Collectors.toList());
        if (errorLogs.size() > 0) {
            currentLogEntry.setResultType(LogEntry.RESULT_TYPE.ERROR);
        }
        currentLogEntry.setLogMessage(String.join("\n", logs));
        currentLogEntry.setErrorLogEntry(String.join("\n", errorLogs));

        //TODO!
//        currentLogEntry.setErrorType("UNDEFINED YET");
//        currentLogEntry.setInjectedError("UNDEFINED YET");
    }

    @Override
    public void cleanUp() throws IOException {

    }

    private static void stopTestNode() throws IOException, InterruptedException {
        //Stop Node
        List<String> command = new LinkedList<>();
        command.add("su");
        command.add(USER);
        command.add("-c");
        command.add(String.format("ccm %s stop", TEST_NODE));
        LOG.debug("Stopping {}", TEST_NODE);
        Process stopProcess = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        stopProcess.waitFor();

        //Assert that node really shutdown
        String[] isUpCommand = new String[]{"su", USER, "-c", "ccm status"};
        Process process = new ProcessBuilder(isUpCommand)
                .redirectErrorStream(true)
                .start();

        List<String> output = new ArrayList<>();
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
        }

        //Check if node is not running
        boolean isDown = output.stream().anyMatch(str -> str.equals(TEST_NODE + ": DOWN"));
        if (!isDown) {
            LOG.error("Received: {}", output);
            throw new RuntimeException(String.format("Node %s is still running which should not be allowed", TEST_NODE));
        }
    }
}
