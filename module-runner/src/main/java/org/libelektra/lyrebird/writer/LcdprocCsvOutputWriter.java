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

    String[] HEADERS = {"ErrorType", "InjectionType", "Key", "SpecCaught", "Old Value", "New Value", "Error Message", "SpecMessage", "Log Message", "Type"};

    public LcdprocCsvOutputWriter(
            @Value("${outputpath}") String outputPath) {
        this.outputPath = outputPath;
    }

    public void write(Collection<LogEntry> result) throws IOException {
        FileWriter out = new FileWriter(outputPath + "/lyrebird.csv");
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader(HEADERS))) {
            for (LogEntry logEntry : result) {
                String specificationErrorLogEntry = "";
                if (logEntry.getSpecificationDataResult().hasDetectedError()) {
                    specificationErrorLogEntry = logEntry.getSpecificationDataResult().getErrorMessage();
                    if (specificationErrorLogEntry!=null && specificationErrorLogEntry.equals("(null)")) {
                        specificationErrorLogEntry = String.join("\n", logEntry.getSpecificationDataResult().getWarnings());
                    }
                }
                printer.printRecord(
                        logEntry.getInjectionDataResult()
                                .getInjectionMeta()
                                .getCategory(),
                        logEntry.getInjectionDataResult().getInjectionMeta().getMetadata(),
                        logEntry.getInjectionDataResult().getKey(),
                        logEntry.getSpecificationDataResult().hasDetectedError() ? "X" : "",
                        logEntry.getInjectionDataResult().getOldValue(),
                        logEntry.getInjectionDataResult().getNewValue(),
                        logEntry.getErrorLogEntry(),
                        specificationErrorLogEntry,
                        logEntry.getLogMessage(),
                        logEntry.getResultType()
                );
            }
        }
    }
}
