package org.libelektra.lyrebird.model;

import org.libelektra.model.InjectionDataResult;
import org.libelektra.model.SpecificationDataResult;

import static org.libelektra.lyrebird.model.LogEntry.RESULT_TYPE.NONE;

public class LogEntry {

    private RESULT_TYPE resultType;
    private String logMessage;
    private String errorLogEntry;
    private InjectionDataResult injectionDataResult;
    private SpecificationDataResult specificationDataResult;

    public LogEntry(RESULT_TYPE resultType,
                    String logMessage,
                    String errorLogEntry,
                    InjectionDataResult injectionDataResult,
                    SpecificationDataResult specificationDataResult) {
        this.resultType = resultType;
        this.logMessage = logMessage;
        this.errorLogEntry = errorLogEntry;
        this.injectionDataResult = injectionDataResult;
        this.specificationDataResult = specificationDataResult;
    }

    public LogEntry() {
        resultType = NONE;
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

    public void setResultType(RESULT_TYPE resultType) {
        this.resultType = resultType;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public SpecificationDataResult getSpecificationDataResult() {
        return specificationDataResult;
    }

    public void setSpecificationDataResult(SpecificationDataResult specificationDataResult) {
        this.specificationDataResult = specificationDataResult;
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

    public static enum RESULT_TYPE {
        SPECIFICATION_CAUGHT,
        ERROR,
        SUCCESS,
        NONE;
    }
}

