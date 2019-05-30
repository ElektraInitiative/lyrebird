package org.libelektra.lyrebird.runner.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.lyrebird.Main;
import org.libelektra.lyrebird.errortype.ErrorType;
import org.libelektra.lyrebird.model.LogEntry;
import org.libelektra.lyrebird.runner.ApplicationRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LCDprocRunner implements ApplicationRunner {

    private final static Logger LOG = LoggerFactory.getLogger(LCDprocRunner.class);

    private Set<ErrorType> allowedErrorTypes;
    private Process process;

    private SysLogListener sysLogListener;
    private Tailer tailer;
    private final String LOG_LOCATION = "/var/log/syslog";

    private LogEntry currentLogEntry;


    private final static String LCDSERVER_RUN_CONFIG = "lcdproc/LCDd-run.ini";
    private final static String TEMP_RUN_CONFIG = "/tmp/lcdd-tmp.conf";
    private final static String KDB_LCDPROC_PATH = ELEKTRA_NAMESPACE+"/lcdproc";


    @Override
    public void start() throws IOException, InterruptedException {

        String[] command = (new String[]{"xterm", "-e", String.format("LCDd -f -c %s", TEMP_RUN_CONFIG)});

        process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        sysLogListener = new SysLogListener();
        File file = new File(LOG_LOCATION);
        tailer = Tailer.create(file, sysLogListener);
    }

    @Override
    public void stop() throws IOException, InterruptedException {
        Runtime.getRuntime().exec("killall -s SIGINT LCDd");
        process.waitFor();
        tailer.stop();
        handleLogMessage(sysLogListener.getLogMessages());
        LOG.info("LCDd exitvalue: {}", process.exitValue());
    }

    @Override
    public void injectInConfiguration() {
        Key key = Key.create(KDB_LCDPROC_PATH);
        KeySet set = KeySet.create();
        try (KDB kdb = KDB.open(key)) {
            kdb.get(set, key);
        } catch (KDB.KDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resetConfiguration() throws IOException {
        try {
            Runtime.getRuntime().exec(String.format("kdb umount %s", KDB_LCDPROC_PATH));
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            File initialConfigFile = new File(classLoader.getResource(LCDSERVER_RUN_CONFIG).getFile());
            File runConfig = new File(TEMP_RUN_CONFIG);
            FileUtils.copyFile(initialConfigFile, runConfig);
            Runtime.getRuntime().exec(String.format("kdb mount %s %s ini", TEMP_RUN_CONFIG, KDB_LCDPROC_PATH));
        } catch (NullPointerException e) {
            LOG.error("Could not find configuration for {}", LCDSERVER_RUN_CONFIG);
        }
    }

    private void handleLogMessage(List<String> logMessages) {
        currentLogEntry = new LogEntry();
        currentLogEntry.setLogMessage(String.join("\n", logMessages));

        //TODO!
        currentLogEntry.setResultType(LogEntry.RESULT_TYPE.NONE);
        currentLogEntry.setErrorType("UNDEFINED YET");
        currentLogEntry.setInjectedError("UNDEFINED YET");
    }


    @Override
    public LogEntry getLogEntry() {
        return currentLogEntry;
    }

    @Override
    public void setErrorTypes(Set<ErrorType> errorTypes) {
        this.allowedErrorTypes = errorTypes;
    }
}
