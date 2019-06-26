package org.libelektra.lyrebird.runner.impl;

import org.apache.commons.io.input.Tailer;
import org.libelektra.lyrebird.errortype.ErrorType;
import org.libelektra.lyrebird.model.LogEntry;
import org.libelektra.lyrebird.runner.ApplicationRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CassandraRunner implements ApplicationRunner {

    private final static Logger LOG = LoggerFactory.getLogger(CassandraRunner.class);

    private Set<ErrorType> allowedErrorTypes;

    private static final String USER = "wespe";
    private static final String CASSANDRA_VERSION = "3.11.2";
    private static final int CASSANDRA_NODES = 3;
    private static final String CLUSTER_NAME = "MyCluster";
    private static final String TEST_NODE = "node1";
    private static final String LOG_LOCATION =
            String.format("/home/%s/.ccm/%s/node1/logs/system.log", USER, CLUSTER_NAME);
    private static final String CONF_LOCATION =
            String.format("/home/%s/.ccm/%s/node1/conf/cassandra.yaml", USER, CLUSTER_NAME);


    private Tailer tailer;
    private LogListener tailerListener;
    private LogEntry currentLogEntry;

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
        tailerListener = new LogListener();
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
    public boolean injectInConfiguration() {
        return true;
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
    public void setErrorTypes(Set<ErrorType> errorTypes) {
        this.allowedErrorTypes = errorTypes;
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
