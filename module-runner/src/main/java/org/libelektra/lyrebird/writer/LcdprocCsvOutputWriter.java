package org.libelektra.lyrebird.writer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.libelektra.lyrebird.model.LogEntry;
import org.libelektra.model.InjectionDataResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

@Component
public class LcdprocCsvOutputWriter {

    private final String outputPath;
    String[] HEADERS = { "ErrorType", "InjectionType", "Key", "Old Value", "New Value", "Log Message", "Error Message", "Core Error Message"};


    public LcdprocCsvOutputWriter(@Value("${outputpath}") String outputPath) {
        this.outputPath = outputPath;
    }

    public void write(Collection<LogEntry> result) throws IOException {
        FileWriter out = new FileWriter(outputPath + "/lcdproc.csv");
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader(HEADERS))) {
        }
    }
}
