package org.libelektra.service;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Component
@Profile("manual")
public class ManualInjectionService {

    private final static Logger LOG = LoggerFactory.getLogger(ManualInjectionService.class);

    private final String specialInjections;

    public ManualInjectionService(
            @Value("${special.injections}") String specialInjections) {
        this.specialInjections = specialInjections;
    }

    public void inject(Path path) throws IOException {
        LOG.info("Injecting file {}", path.getFileName());
        FileUtils.copyFile(new File(path.toUri()), new File(specialInjections));
    }

    public void reset() {
        FileUtils.deleteQuietly(new File(specialInjections));
    }
}
