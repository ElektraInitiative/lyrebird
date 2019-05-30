package org.libelektra.lyrebird.runner.impl;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
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


    @Override
    public void start() throws IOException, InterruptedException {

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File filee = new File(classLoader.getResource(LCDSERVER_RUN_CONFIG).getFile());

//        String[] command = (new String[]{"/bin/bash", "-c", String.format("'LCDd -f -c %s'",  filee.getAbsolutePath())});
        String[] command = (new String[]{"xterm", "-e", String.format("LCDd -f -c %s",  filee.getAbsolutePath())});
        System.out.println(Arrays.toString(command));
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
        process.destroy();
        process.waitFor();
        tailer.stop();
        handleLogMessage(sysLogListener.getLogMessages());
        LOG.info("LCDd exitvalue: {}",process.exitValue());
    }

    @Override
    public void injectInConfiguration() {

    }

    @Override
    public void resetConfiguration() {

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
