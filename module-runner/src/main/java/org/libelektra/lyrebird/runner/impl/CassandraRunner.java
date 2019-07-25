package org.libelektra.lyrebird.runner.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("cassandra")
public class CassandraRunner implements ApplicationRunner {

    private final static Logger LOG = LoggerFactory.getLogger(CassandraRunner.class);

    private static final String USER = System.getProperty("user.name");
    private static final String CASSANDRA_VERSION = "3.11.4";
    private static final int CASSANDRA_NODES = 3;
    private static final String CLUSTER_NAME = "LyreBirdCluster";
    private static final String TEST_NODE = "node1";
    private static final String LOG_LOCATION =
            String.format("/home/%s/.ccm/%s/%s/logs/system.log", USER, CLUSTER_NAME, TEST_NODE);

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
    public final static String NODE1_RUN_CONFIG = String.format("/home/%s/.ccm/%s/%s/conf/cassandra.yaml", USER
            , CLUSTER_NAME, TEST_NODE);
    public final static String TEMP_SPEC_CONFIG = "/tmp/cassandra-spec.conf";
    public final static String TEMP_ERROR_CONFIG = "/tmp/lcdd-inject.conf";

    private Tailer tailer;
    private LogListenerService tailerListener;
    private final File initConfig;

    @Autowired
    public CassandraRunner(KDBService kdbService,
                           RandomizerService randomizerService,
                           SpecificationEnforcer specificationEnforcer,
                           InjectionPlugin injectionPlugin,
                           InjectionConfiguration injectionConfiguration
    ) throws KDB.KDBException, InterruptedException {
        this.injectionConfiguration = injectionConfiguration;
        this.specificationEnforcer = specificationEnforcer;
        this.injectionPlugin = injectionPlugin;
        this.kdbService = kdbService;
        this.randomizerService = randomizerService;
        initConfig = Util.getResourceFile("cassandra/cassandra_ccm.yaml");
        try {
            startClusterIfNotUp();
            File errorConfigFile = Util.getResourceFile(CASSANDRA_INJECT_CONFIG);
            File specConfigFile = Util.getResourceFile(CASSANDRA_SPEC_CONFIG);
            //TODO: Filter for settings with numbers
            FileUtils.copyFile(initConfig, new File(injectionConfiguration.getRunConfig()));
            File specConfig = new File(TEMP_SPEC_CONFIG);
            FileUtils.copyFile(errorConfigFile, new File(TEMP_ERROR_CONFIG));
            FileUtils.copyFile(specConfigFile, specConfig);
            currentLogEntry = new LogEntry();
            Util.executeCommand(String.format("kdb mount %s %s ni", TEMP_ERROR_CONFIG,
                    injectionConfiguration.getInjectPath()));
            Util.executeCommand(String.format("kdb mount %s %s ni", TEMP_SPEC_CONFIG,
                    injectionConfiguration.getSpecPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        kdbService.initKDB();
        errorConfigKeySet = kdbService.getKeySetBelowPath(injectionConfiguration.getInjectPath());
        specificationKeySet = kdbService.getKeySetBelowPath(injectionConfiguration.getSpecPath());
    }

    @Override
    public void start() throws IOException {
        LOG.debug("Starting {}", TEST_NODE);
        File file = new File(LOG_LOCATION);
        tailerListener = new LogListenerService();
        tailer = Tailer.create(file, tailerListener);
        File startScript = Util.getResourceFile("cassandra/startNode.sh");
        ProcessBuilder bash = new ProcessBuilder();
        bash.command("bash", startScript.toString(), TEST_NODE);
        Process p = bash
                .redirectErrorStream(true)
                .start();
        logProcess(p);

//        String[] command = new String[]{"ccm", TEST_NODE, "start"};
//        LOG.debug("Starting {}", TEST_NODE);
//        File file = new File(LOG_LOCATION);
//        tailerListener = new LogListenerService();
//        tailer = Tailer.create(file, tailerListener);
//        Process process = new ProcessBuilder(command)
//                .redirectErrorStream(true)
//                .start();
//        logProcess(process);
        //TODO: log process error stream additionally
    }

    @Override
    public void stop() throws IOException, InterruptedException {
        stopTestNode();
        tailer.stop();
        handleLogs(tailerListener.getLogsAndReset());
    }

    @Override
    public InjectionResult injectInConfiguration() throws KDB.KDBException {
        int nextRandom = randomizerService.getNextInt(errorConfigKeySet.length());
        Key injectKey = errorConfigKeySet.at(nextRandom);
        String path = injectKey.getName().replace(injectionConfiguration.getInjectPath(),
                injectionConfiguration.getParentPath());
        KeySet configKeySet = kdbService.getKeySetBelowPath(injectionConfiguration.getParentPath());
        InjectionDataResult injectionDataResult = injectionPlugin.kdbSet(configKeySet, injectKey, path);
        currentLogEntry.setInjectionDataResult(injectionDataResult);
        this.specificationDataResult = specificationEnforcer.checkSpecification(specificationKeySet,
                configKeySet, injectKey);
        currentLogEntry.setSpecificationDataResult(this.specificationDataResult);
        injectAdditionalContextDependant(configKeySet, injectionDataResult);
        return new InjectionResult(injectionDataResult, specificationDataResult);
    }

    @Override
    public void resetConfiguration() throws IOException {
        try {
            //Clear logging for new run
            File file = new File(LOG_LOCATION);
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.print("");
            }
            Util.executeCommand(String.format("kdb umount %s", injectionConfiguration.getParentPath()));
            kdbService.close();
            File runConfig = new File(injectionConfiguration.getRunConfig());
            FileUtils.deleteQuietly(runConfig);
            FileUtils.copyFile(initConfig, runConfig);
            Util.executeCommand(String.format("kdb mount %s %s yamlcpp", injectionConfiguration.getRunConfig(), injectionConfiguration.getParentPath()));
            kdbService.initKDB();
            this.currentLogEntry = new LogEntry();
        } catch (NullPointerException e) {
            LOG.error("Could not find configuration for {}", CASSANDRA_RUN_CONFIG);
        }
    }

    @Override
    public LogEntry getLogEntry() {
        return currentLogEntry;
    }

    private void handleLogs(List<String> logs) {
        List<String> errorLogs = logs.stream().filter(str -> str.contains("ERROR")).collect(Collectors.toList());
        if (errorLogs.size() > 0) {
            currentLogEntry.setResultType(LogEntry.RESULT_TYPE.ERROR);
        }
        currentLogEntry.setLogMessage(String.join("\n", logs));
        currentLogEntry.setErrorLogEntry(String.join("\n", errorLogs));

        //TODO!
//        currentLogEntry.setErrorType("UNDEFINED YET");
    }

    @Override
    public void cleanUp() throws IOException {
        LOG.info("Cleaning up");
        Util.executeCommand(String.format("kdb umount %s", injectionConfiguration.getParentPath()));
        Util.executeCommand(String.format("kdb umount %s", injectionConfiguration.getInjectPath()));
        Util.executeCommand(String.format("kdb umount %s", injectionConfiguration.getSpecPath()));

        //Set old config file and delete saved copy
        FileUtils.copyFile(initConfig, new File(injectionConfiguration.getRunConfig()));

        FileUtils.deleteQuietly(new File(TEMP_SPEC_CONFIG));
        FileUtils.deleteQuietly(new File(TEMP_ERROR_CONFIG));
    }

    private void startClusterIfNotUp() throws IOException, InterruptedException {
        File startScript = Util.getResourceFile("cassandra/startCluster.sh");
        ProcessBuilder bash = new ProcessBuilder();
        bash.command("bash", startScript.toString(), CLUSTER_NAME, CASSANDRA_VERSION, String.valueOf(CASSANDRA_NODES));
        Process p = bash
                .redirectErrorStream(true)
                .start();
        logProcess(p);

        //Stop node1
        stopTestNode();
    }

    private void stopTestNode() throws IOException, InterruptedException {
        //Stop Node
        String[] command = new String[]{"ccm", TEST_NODE, "stop"};
        LOG.debug("Stopping {}", TEST_NODE);
        Process stopProcess = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        stopProcess.waitFor();

        //Assert that node really shutdown
        String[] isUpCommand = new String[]{"ccm", "status"};
        Process process = new ProcessBuilder(isUpCommand)
                .start();

        List<String> output = new ArrayList<>();
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
        }

        //Check if node is not running
        boolean isDown = output.stream().anyMatch(str -> str.contains(TEST_NODE + ": DOWN"));
        if (!isDown) {
            LOG.error("Received: {}", output);
            throw new RuntimeException(String.format("Node %s is still running which should not be allowed",
                    TEST_NODE));
        }
    }

    private void logProcess(Process process) throws IOException {
        String output = IOUtils.toString(process.getInputStream(), Charset.defaultCharset());
        String errorOutput = IOUtils.toString(process.getErrorStream(), Charset.defaultCharset());
        if (!output.isEmpty()) {
            Arrays.stream(output.split("\n"))
                    .filter(a -> !a.isEmpty())
                    .forEach(LOG::info);
        }
        if (!errorOutput.isEmpty()) {
            Arrays.stream(errorOutput.split("\n"))
                    .filter(a -> !a.isEmpty())
                    .forEach(LOG::info);
        }
    }
}
