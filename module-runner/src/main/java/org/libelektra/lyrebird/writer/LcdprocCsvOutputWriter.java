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
    String[] HEADERS = {"ErrorType", "InjectionType", "Key", "Log Message", "Old Value", "New Value", "Error Message"};


    public LcdprocCsvOutputWriter(@Value("${outputpath}") String outputPath) {
        this.outputPath = outputPath;
    }

    public void write(Collection<LogEntry> result) throws IOException {
        FileWriter out = new FileWriter(outputPath + "/lcdproc.csv");
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader(HEADERS))) {
            for (LogEntry logEntry : result) {
                printer.printRecord(
                        logEntry.getInjectionDataResult().getInjectionMeta().getCategory(),
                        logEntry.getInjectionDataResult().getInjectionMeta().getMetadata(),
                        logEntry.getInjectionDataResult().getKey(),
                        logEntry.getLogMessage(),
                        logEntry.getInjectionDataResult().getOldValue(),
                        logEntry.getInjectionDataResult().getNewValue(),
                        logEntry.getErrorLogEntry()
                );
            }
        }
    }
}
