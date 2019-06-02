package org.libelektra.lyrebird.model;

public class LogEntry {

    private RESULT_TYPE resultType;
    private String errorType;
    private String injectedError;
    private String logMessage;
    private String errorLogEntry;

    public LogEntry(RESULT_TYPE resultType, String errorType,
                    String injectedError, String logMessage,
                    String errorLogEntry) {
        this.resultType = resultType;
        this.errorType = errorType;
        this.injectedError = injectedError;
        this.logMessage = logMessage;
        this.errorLogEntry = errorLogEntry;
    }

    public LogEntry() {
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

    public String getErrorType() {
        return errorType;
    }

    public String getInjectedError() {
        return injectedError;
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

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public void setInjectedError(String injectedError) {
        this.injectedError = injectedError;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "resultType=" + resultType +
                ", errorType='" + errorType + '\'' +
                ", injectedError='" + injectedError + '\'' +
                ",\nlogMessage='" + logMessage + '\'' +
                ",\nerrorMessage='" + errorLogEntry + '\'' +
                '}';
    }
}

