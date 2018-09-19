package org.libelektra.lyrebird.model;

public class LogEntry {

    private final RESULT_TYPE resultType;
    private final String errorType;
    private final String injectedError;
    private final String logMessage;

    public LogEntry(RESULT_TYPE resultType, String errorType, String injectedError, String logMessage) {
        this.resultType = resultType;
        this.errorType = errorType;
        this.injectedError = injectedError;
        this.logMessage = logMessage;
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

    @Override
    public String toString() {
        return "LogEntry{" +
                "resultType=" + resultType +
                ", errorType='" + errorType + '\'' +
                ", injectedError='" + injectedError + '\'' +
                ", logMessage='" + logMessage + '\'' +
                '}';
    }
}

