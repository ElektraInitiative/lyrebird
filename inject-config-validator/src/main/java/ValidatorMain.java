import org.ini4j.Ini;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ValidatorMain {

//    public static void main(String[] args) throws IOException {
//        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
//        File errorConfigFile = new File(classLoader.getResource("LCDd-inject.ini").getFile());
//
//
//
//        Ini ini = new Ini(errorConfigFile);
//        ini.forEach((head, section) -> System.out.println(head));
//
//
//    }

    // Purges all newlines from comments
    public static void main(String[] args) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("/home/wespe/extern/repository/lyrebird/inject-config-validator/src/main/resources/sanitized.ini"));
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File errorConfigFile = new File(classLoader.getResource("LCDd-inject.ini").getFile());
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
