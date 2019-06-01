package org.libelektra.lyrebird.runner;

import org.libelektra.lyrebird.model.LogEntry;
import org.libelektra.lyrebird.errortype.ErrorType;

import java.io.IOException;
import java.util.Set;

public interface ApplicationRunner {

    static final String ELEKTRA_NAMESPACE = "system";

    void start() throws IOException, InterruptedException;

    void stop() throws IOException, InterruptedException;

    void injectInConfiguration();

    void resetConfiguration() throws IOException;

    LogEntry getLogEntry();

    void setErrorTypes(Set<ErrorType> errorTypes);

    void cleanUp() throws IOException;
}
