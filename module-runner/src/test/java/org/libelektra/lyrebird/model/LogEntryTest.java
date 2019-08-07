package org.libelektra.lyrebird.model;

import org.junit.Test;
import org.libelektra.errortypes.DomainError;
import org.libelektra.model.InjectionDataResult;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LogEntryTest {

    @Test
    public void equalsTest() {

        InjectionDataResult injectionDataResult1 = new InjectionDataResult.Builder(true)
                .withKey("a")
                .withOldValue("b")
                .withNewValue("c")
                .withInjectionMeta(DomainError.Metadata.DOMAIN_ERROR)
                .build();

        InjectionDataResult injectionDataResult2 = new InjectionDataResult.Builder(false)
                .withKey("a")
                .withOldValue("b")
                .withNewValue("c")
                .withInjectionMeta(DomainError.Metadata.DOMAIN_ERROR)
                .build();

        LogEntry logEntry1 = new LogEntry();
        logEntry1.setInjectionDataResult(injectionDataResult1);
        logEntry1.setLogMessage("asdfasdf");
        LogEntry logEntry2 = new LogEntry();
        logEntry2.setInjectionDataResult(injectionDataResult2);
        logEntry2.setLogMessage("as2345dfasdf");

        assertThat(logEntry1.equals(logEntry2), is(true));

        List<LogEntry> list = List.of(logEntry1, logEntry2).stream()
                .distinct()
                .collect(Collectors.toList());

        assertThat(list.size(), is(1));
    }

}