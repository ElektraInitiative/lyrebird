package org.libelektra.lyrebird.runner;

import org.libelektra.KeySet;
import org.libelektra.lyrebird.errortype.ErrorType;
import org.libelektra.lyrebird.model.LogEntry;
import org.libelektra.KDB;
import org.libelektra.model.InjectionDataResult;

import java.io.IOException;
import java.util.Set;

public interface ApplicationRunner {

    static final String ELEKTRA_NAMESPACE = "system";

    void start() throws IOException, InterruptedException;

    void stop() throws IOException, InterruptedException;

    boolean injectInConfiguration() throws KDB.KDBException, InterruptedException;

    default void injectAdditionalContextDependant(KeySet set, InjectionDataResult data) { }

    void resetConfiguration() throws IOException;

    LogEntry getLogEntry();

    void setErrorTypes(Set<ErrorType> errorTypes);

    void cleanUp() throws IOException;
}
