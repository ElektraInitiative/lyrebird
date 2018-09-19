package org.libelektra.lyrebird.runner;

import org.libelektra.lyrebird.model.LogEntry;
import org.libelektra.lyrebird.errortype.ErrorType;

import java.io.IOException;
import java.util.Set;

public interface ApplicationRunner {

    void start() throws IOException, InterruptedException;

    void stop() throws IOException, InterruptedException;

    void injectInConfiguration();

    void resetConfiguration();

    LogEntry getLogEntry();

    void setErrorTypes(Set<ErrorType> errorTypes);
}
