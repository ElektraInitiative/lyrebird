package org.libelektra.lyrebird.writer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.libelektra.InjectionMeta;
import org.libelektra.lyrebird.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LcdprocCsvOutputWriter {

    private final static Logger LOG = LoggerFactory.getLogger(LcdprocCsvOutputWriter.class);

    private final String outputPath;

    String[] HEADERS = {"ErrorType", "InjectionType", "Key", "Old Value", "New Value", "Error Message", "SpecMessage"
            , "SpecCaught", "SpecPlugin", "Log Message", "Type"};

    public LcdprocCsvOutputWriter(
            @Value("${outputpath}") String outputPath) {
        this.outputPath = outputPath;
    }

    public void write(Collection<LogEntry> result) throws IOException {
        Files.deleteIfExists(Path.of(outputPath + "/lyrebird.csv"));
        FileWriter out = new FileWriter(outputPath + "/lyrebird.csv");
        Collection<LogEntry> noDuplicates = result.stream()
                .distinct()
                .collect(Collectors.toList());
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader(HEADERS))) {
            for (LogEntry logEntry : noDuplicates) {
                String specificationErrorLogEntry = "";
                if (logEntry.getSpecificationDataResult().hasDetectedError()) {
                    specificationErrorLogEntry = logEntry.getSpecificationDataResult().getErrorMessage();
                    if (specificationErrorLogEntry != null && specificationErrorLogEntry.equals("(null)")) {
                        specificationErrorLogEntry = String.join("\n",
                                logEntry.getSpecificationDataResult().getWarnings());
                    }
                }
                printer.printRecord(
                        logEntry.getInjectionDataResult()
                                .getInjectionMeta()
                                .getCategory(),
                        logEntry.getInjectionDataResult().getInjectionMeta().getMetadata(),
                        logEntry.getInjectionDataResult().getKey(),
                        logEntry.getInjectionDataResult().getOldValue(),
                        logEntry.getInjectionDataResult().getNewValue(),
                        logEntry.getErrorLogEntry(),
                        specificationErrorLogEntry,
                        logEntry.getSpecificationDataResult().hasDetectedError() ? "X" : "",
                        logEntry.getSpecificationDataResult().hasDetectedError() ?
                                logEntry.getSpecificationDataResult().getPluginName() : "",
                        logEntry.getLogMessage(),
                        logEntry.getResultType()
                );
            }
        }
        LOG.info("===== Removed {} duplicates ======", result.size() - noDuplicates.size());
        Map<InjectionMeta, Long> sortedMap = noDuplicates
                .stream()
                .collect(Collectors.groupingBy(LogEntry::getInjectionMeta,
                        Collectors.counting()));
        sortedMap.forEach((k, v) -> LOG.info("{}: {}", k.getMetadata(), v));
        LOG.info("===============");
        Map<String, Long> sortedMap2 = noDuplicates
                .stream()
                .collect(Collectors.groupingBy(l -> l.getInjectionDataResult().getInjectionMeta().getCategory(),
                        Collectors.counting()));
        sortedMap2.forEach((k, v) -> LOG.info("{}: {}", k, v));
    }
}
