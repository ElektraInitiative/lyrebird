package org.libelektra.lyrebird.runner;

import org.libelektra.lyrebird.errortype.ErrorType;
import org.libelektra.lyrebird.model.LogEntry;
import org.libelektra.KDB;

import java.io.IOException;
import java.util.Set;

public interface ApplicationRunner {

    static final String ELEKTRA_NAMESPACE = "system";

    void start() throws IOException, InterruptedException;

    void stop() throws IOException, InterruptedException;

    void injectInConfiguration() throws KDB.KDBException, InterruptedException;

    void resetConfiguration() throws IOException;

    LogEntry getLogEntry();

    void setErrorTypes(Set<ErrorType> errorTypes);

    void cleanUp() throws IOException;
}
