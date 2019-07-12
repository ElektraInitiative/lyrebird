package org.libelektra.lyrebird.writer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.libelektra.lyrebird.model.LogEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

@Component
public class LcdprocCsvOutputWriter {

    private final String outputPath;
    private final boolean withSpecification;

    String[] HEADERS = {"ErrorType", "InjectionType", "Key", "Log Message", "Old Value", "New Value", "Error Message", "Type"};


    public LcdprocCsvOutputWriter(
            @Value("${injection.with-specification}") boolean withSpecification,
            @Value("${outputpath}") String outputPath) {
        this.outputPath = outputPath;
        this.withSpecification = withSpecification;
    }

    public void write(Collection<LogEntry> result) throws IOException {
        FileWriter out = new FileWriter(outputPath + "/lcdproc.csv");
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader(HEADERS))) {
            for (LogEntry logEntry : result) {
                String errorLogEntry = logEntry.getErrorLogEntry();
                if (withSpecification && errorLogEntry!=null && errorLogEntry.equals("(null)")) {
                    errorLogEntry = String.join("\n", logEntry.getSpecificationDataResult().getWarnings());
                }
                printer.printRecord(
                        logEntry.getInjectionDataResult()
                                .getInjectionMeta()
                                .getCategory(),
                        logEntry.getInjectionDataResult().getInjectionMeta().getMetadata(),
                        logEntry.getInjectionDataResult().getKey(),
                        logEntry.getLogMessage(),
                        logEntry.getInjectionDataResult().getOldValue(),
                        logEntry.getInjectionDataResult().getNewValue(),
                        errorLogEntry,
                        logEntry.getResultType()
                );
            }
        }
    }
}
