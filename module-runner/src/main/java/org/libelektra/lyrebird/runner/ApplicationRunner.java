package org.libelektra.lyrebird.runner;

import org.libelektra.KDB;
import org.libelektra.KeySet;
import org.libelektra.lyrebird.model.LogEntry;
import org.libelektra.model.InjectionDataResult;
import org.libelektra.model.InjectionResult;

import java.io.IOException;

public interface ApplicationRunner {

    static final String ELEKTRA_NAMESPACE = "system";

    void start() throws IOException, InterruptedException;

    void stop() throws IOException, InterruptedException;

    InjectionResult injectInConfiguration() throws KDB.KDBException, InterruptedException;

    default void injectAdditionalContextDependant(KeySet set, InjectionDataResult data) { }

    void resetConfiguration() throws IOException;

    LogEntry getLogEntry();

    void cleanUp() throws IOException;
}
