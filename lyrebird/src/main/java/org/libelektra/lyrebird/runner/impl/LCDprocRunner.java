package org.libelektra.lyrebird.runner.impl;

import org.libelektra.lyrebird.Main;
import org.libelektra.lyrebird.errortype.ErrorType;
import org.libelektra.lyrebird.model.LogEntry;
import org.libelektra.lyrebird.runner.ApplicationRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LCDprocRunner implements ApplicationRunner {

    private final static Logger LOG = LoggerFactory.getLogger(LCDprocRunner.class);

    private Set<ErrorType> allowedErrorTypes;
    private Process process;


    @Override
    public void start() throws IOException, InterruptedException {

//        String[] command = (new String[]{"/bin/bash", "-c", "LCDd"});
        String[] command = (new String[]{"xterm", "-e", "su -c 'LCDd -s 1' wespe"});
        process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            Thread.sleep(5000);
//            Instant waitTime = Instant.now().plusSeconds(5);
//            while ((line = reader.readLine()) != null && Instant.now().isBefore(waitTime)) {
//                LOG.info("Waiting");
//                LOG.info(line); // Your superior logging approach here
//            }
        }
    }

    @Override
    public void stop() throws IOException, InterruptedException {
        Runtime.getRuntime().exec("killall -s SIGTERM LCDd");
        process.destroy();
        process.waitFor();
        LOG.info("LCDd exitvalue: {}",process.exitValue());
    }

    @Override
    public void injectInConfiguration() {

    }

    @Override
    public void resetConfiguration() {

    }

    @Override
    public LogEntry getLogEntry() {
        return null;
    }

    @Override
    public void setErrorTypes(Set<ErrorType> errorTypes) {
        this.allowedErrorTypes = errorTypes;
    }
}
