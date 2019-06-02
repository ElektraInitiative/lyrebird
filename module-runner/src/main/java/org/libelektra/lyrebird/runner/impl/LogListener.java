package org.libelektra.lyrebird.runner.impl;

import org.apache.commons.io.input.TailerListenerAdapter;

import java.util.ArrayList;
import java.util.List;

public class LogListener extends TailerListenerAdapter {

    private List<String> logs;

    public LogListener() {
        logs = new ArrayList<>();
    }

    @Override
    public void handle(String line) {
        super.handle(line);
        logs.add(line);
    }

    public List<String> getLogsAndReset() {
        List<String> tmpLogs = logs;
        logs = new ArrayList<>();
        return tmpLogs;
    }

}
