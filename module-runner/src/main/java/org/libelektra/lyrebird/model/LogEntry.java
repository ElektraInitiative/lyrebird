package org.libelektra.lyrebird.model;

import org.libelektra.model.InjectionDataResult;

public class LogEntry {

    private RESULT_TYPE resultType;
    private String logMessage;
    private String errorLogEntry;
    private InjectionDataResult injectionDataResult;

    public LogEntry(RESULT_TYPE resultType,
                    String logMessage,
                    String errorLogEntry,
                    InjectionDataResult injectionDataResult) {
        this.resultType = resultType;
        this.logMessage = logMessage;
        this.errorLogEntry = errorLogEntry;
        this.injectionDataResult = injectionDataResult;
    }

    public LogEntry() {
    }

    public void setInjectionDataResult(InjectionDataResult injectionDataResult) {
        this.injectionDataResult = new InjectionDataResult(injectionDataResult);
    }

    public InjectionDataResult getInjectionDataResult() {
        return injectionDataResult;
    }

    public void setErrorLogEntry(String errorLogEntry) {
        this.errorLogEntry = errorLogEntry;
    }

    public String getErrorLogEntry() {
        return errorLogEntry;
    }

    public RESULT_TYPE getResultType() {
        return resultType;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public static enum RESULT_TYPE {
        ERROR,
        SUCCESS,
        NONE;
    }

    public void setResultType(RESULT_TYPE resultType) {
        this.resultType = resultType;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "resultType=" + resultType +
                ", logMessage='" + logMessage + '\'' +
                ", errorLogEntry='" + errorLogEntry + '\'' +
                ", injectionDataResult=" + injectionDataResult +
                '}';
    }
}

