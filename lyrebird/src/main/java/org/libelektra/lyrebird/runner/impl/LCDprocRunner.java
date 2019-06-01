package org.libelektra.lyrebird.runner.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.Tailer;
import org.libelektra.Util;
import org.libelektra.lyrebird.errortype.ErrorType;
import org.libelektra.lyrebird.model.LogEntry;
import org.libelektra.service.KDBService;
import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.lyrebird.runner.ApplicationRunner;
import org.libelektra.util.RandomizerSingelton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
public class LCDprocRunner implements ApplicationRunner {

    private KDBService kdbService;
    private KDB kdb;
    private final static Logger LOG = LoggerFactory.getLogger(LCDprocRunner.class);

    private Set<ErrorType> allowedErrorTypes;
    private Process process;

    private SysLogListener sysLogListener;
    private Tailer tailer;
    private final String LOG_LOCATION = "/var/log/syslog";

    private LogEntry currentLogEntry;

    public final static String LCDSERVER_RUN_CONFIG = "lcdproc/LCDd-run.ini";
    public final static String LCDSERVER_INJECT_CONFIG = "lcdproc/LCDd-inject.ini";
    public final static String TEMP_RUN_CONFIG = "/tmp/lcdd-tmp.conf";
    public final static String TEMP_ERROR_CONFIG = "/tmp/lcdd-inject.conf";
    public final static String KDB_LCDPROC_PATH = ELEKTRA_NAMESPACE+"/lcdproc";
    public final static String KDB_LCDPROC_INJECT_PATH = ELEKTRA_NAMESPACE+"/inject/lcdproc";

    private KeySet errorConfigKeySet;

    @Autowired
    public LCDprocRunner(KDBService kdbService) throws KDB.KDBException {
        this.kdbService = kdbService;
        kdb = kdbService.getInstance();
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            File errorConfigFile = new File(classLoader.getResource(LCDSERVER_INJECT_CONFIG).getFile());
            File runConfig = new File(TEMP_ERROR_CONFIG);
            FileUtils.copyFile(errorConfigFile, runConfig);
            Util.executeCommand(String.format("kdb mount %s %s ni", TEMP_ERROR_CONFIG, KDB_LCDPROC_INJECT_PATH));
        } catch (NullPointerException e) {
            LOG.error("Could not find configuration for {}", LCDSERVER_RUN_CONFIG);
        } catch (IOException e) {
            e.printStackTrace();
        }
        errorConfigKeySet = kdbService.getKeySetBelowPath(KDB_LCDPROC_INJECT_PATH);
    }


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
    public void injectInConfiguration() throws KDB.KDBException {
        int nextRandom = RandomizerSingelton.getInstance().getNextInt(errorConfigKeySet.length());
        LOG.debug("nextRandom: {}", nextRandom);
        Key at = errorConfigKeySet.at(nextRandom);
        String keyToInject = at.getName().replace(KDB_LCDPROC_INJECT_PATH, KDB_LCDPROC_PATH);
        LOG.info(keyToInject);
        KeySet configKeySet = kdbService.getKeySetBelowPath(KDB_LCDPROC_PATH);
        KeySet.printKeySet(configKeySet);

        LOG.debug("Printing found Key");
        Key lookup = configKeySet.lookup(keyToInject);
        if (lookup.getName() == null) {
            LOG.debug("Did not find key {} in configuration!", keyToInject);
        }
        Key.printKeyAndMeta(lookup);

    }

    @Override
    public void resetConfiguration() throws IOException {
        try {
            Util.executeCommand(String.format("kdb umount %s", KDB_LCDPROC_PATH));
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            File initialConfigFile = new File(classLoader.getResource(LCDSERVER_RUN_CONFIG).getFile());
            File runConfig = new File(TEMP_RUN_CONFIG);
            FileUtils.copyFile(initialConfigFile, runConfig);
            Util.executeCommand(String.format("kdb mount %s %s ini", TEMP_RUN_CONFIG, KDB_LCDPROC_PATH));
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

    @Override
    public void cleanUp() throws IOException {
        Util.executeCommand(String.format("kdb umount %s", KDB_LCDPROC_PATH));
        Util.executeCommand(String.format("kdb umount %s", KDB_LCDPROC_INJECT_PATH));
    }
}
