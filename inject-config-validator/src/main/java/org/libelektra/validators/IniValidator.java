package org.libelektra.validators;

import org.ini4j.Profile;
import org.ini4j.Wini;
import org.libelektra.InjectionMeta;
import org.libelektra.errortypes.AbstractErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class IniValidator {

    private final static Logger LOG = LoggerFactory.getLogger(IniValidator.class);
    private final Collection<AbstractErrorType> allErrors;

    @Autowired
    public IniValidator(Collection<AbstractErrorType> allErrors) {
        this.allErrors = allErrors;
    }

    @PostConstruct
    public void doTheThing() throws IOException {
        purgeNewlines("LCDd-inject.ini", "sanitized.ini");
        checkAllTypesSet("sanitized.ini");
    }

    public void checkAllTypesSet(String inputFile) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File errorConfigFile = new File(classLoader.getResource(inputFile).getFile());

        Wini ini = new Wini(errorConfigFile);
        for (Map.Entry<String, Profile.Section> entry : ini.entrySet()) {
            String typesInConfig = ini.get(entry.getKey(), "types");
            if (typesInConfig == null) {
                continue;
            }
            List<Integer> typesList = Arrays.stream(typesInConfig.split(","))
                    .map(String::trim)
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());

            for (Integer type : typesList) {
                List<InjectionMeta> metas =
                        allErrors.stream()
                            .filter(errType -> errType.getInjectionInt()==type)
                            .flatMap(errTyp -> errTyp.getBelongingMetadatas().stream())
                        .collect(Collectors.toList());
                boolean gotCorrectMeta = entry.getValue().keySet().stream()
                        .anyMatch(sectionEntry -> metas.stream().anyMatch(meta -> sectionEntry.startsWith(meta.getMetadata())));
                if (!gotCorrectMeta) {
                    LOG.error("Section {} does not contain metadata of type {}", entry.getKey(), type);
                }
            }
        }
    }

    public void purgeNewlines(String inputFile, String outputFile) throws IOException {
//        "/home/wespe/extern/repository/lyrebird/inject-config-validator/src/main/resources/sanitized.ini"
        ClassLoader classLoader = IniValidator.class.getClassLoader();
        String path = classLoader.getResource("").getPath();
        BufferedWriter writer = new BufferedWriter(new FileWriter(String.format("%s/%s", path, outputFile)));
        File errorConfigFile = new File(classLoader.getResource(inputFile).getFile());
        List<String> allLines = Files.readAllLines(Paths.get(errorConfigFile.getAbsolutePath()));
        StringBuilder sb = new StringBuilder();
        boolean inDescription = false;
        for (String line : allLines) {
            if (!line.startsWith("description") && !inDescription) {
                sb.append(line);
                sb.append("\n");
            } else {
                sb.append(line);
                inDescription = true;
            }
            if (inDescription && line.isEmpty()) {
                sb.append("\n\n");
                inDescription = false;
            }
        }
        writer.write(sb.toString());
        writer.close();
    }
}
