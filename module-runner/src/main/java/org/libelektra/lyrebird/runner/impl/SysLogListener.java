package org.libelektra.lyrebird.runner.impl;

import org.apache.commons.io.input.TailerListenerAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SysLogListener extends TailerListenerAdapter {

    private LocalDateTime currentDate;
    private List<String> logMessages;

    public SysLogListener() {
        super();
        currentDate = LocalDateTime.now();
        logMessages = new ArrayList<>();
    }

    @Override
    public void handle(String line) {
        super.handle(line);
        String subStringDate = line.substring(0, 15);
        subStringDate = subStringDate.replaceAll("\\s+", " ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MMM d HH:mm:ss")
                .withLocale(Locale.US);
        LocalDateTime syslogTime = LocalDateTime.parse("2018 "+subStringDate, formatter)
                .plusSeconds(2); //Needed or isAfter() will return false

        //Filter out only messages which contain LCDd and are current
        if (line.contains("LCDd") && syslogTime.isAfter(currentDate)) {
            logMessages.add(line);
        }
    }

    public List<String> getLogMessages() {
        return logMessages;
    }
}
