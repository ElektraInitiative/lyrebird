package org.libelektra.lyrebird;

import com.sun.net.httpserver.HttpServer;
import org.libelektra.KeySet;
import org.libelektra.lyrebird.model.LogEntry;
import org.libelektra.lyrebird.runner.ApplicationRunner;
import org.libelektra.lyrebird.writer.LcdprocCsvOutputWriter;
import org.libelektra.model.InjectionResult;
import org.libelektra.service.ManualInjectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@SpringBootApplication(scanBasePackages = "org.libelektra")
public class Main implements CommandLineRunner {

    private final static Logger LOG = LoggerFactory.getLogger(Main.class);

    private final ApplicationRunner runner;
    private Collection<LogEntry> allLogs;
    private final LcdprocCsvOutputWriter outputWriter;
    private ManualInjectionService manualInjectionService;


    private int timeout;
    private int iterations;
    private final String specialInjections;

    private Environment env;

    static {
        System.setProperty("jna.library.path", "/usr/local/lib");
    }

    @Autowired
    public Main(ApplicationRunner runner,
                @Value("${injection.iterations}") int iterations,
                @Value("${injection.timeout}") int timeout,
                @Value("${special.injections}") String specialInjections,
                Environment env,
                LcdprocCsvOutputWriter outputWriter,
                Optional<ManualInjectionService> manualInjectionService) {
        this.timeout = timeout;
        this.specialInjections = specialInjections;
        this.runner = runner;
        this.iterations = iterations;
        this.env = env;
        this.outputWriter = outputWriter;
        allLogs = new ArrayList<>();
        manualInjectionService.ifPresent(service -> this.manualInjectionService = service);
    }

    @Override
    public void run(String... args) throws Exception {
        int port = 8080;
        LOG.info("Starting server on port {} to occupy port", port);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(null); // creates a default executor
        server.start();
        boolean manualInjectionsActive = Arrays.asList(env.getActiveProfiles()).contains("manual");
        if (manualInjectionsActive) {
            runManualInjections();
            return;
        }
        for (int i = 0; i < iterations; i++) {
            runner.resetConfiguration();
            InjectionResult injectionResult = runner.injectInConfiguration();
            if (!injectionResult.wasInjectionSuccessful()) {
                // Case toggling a number for example is impossible and yields false as result
                i--;
                continue;
            }
            runProgram(timeout);
            stopProgram();
            allLogs.add(runner.getLogEntry());
        }
        outputWriter.write(allLogs);
        LOG.info("Stopping server on port {}", port);
        server.stop(0);
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);

//        CassandraRunner.startClusterIfNotUp();
//        ApplicationRunner runner = new CassandraRunner();
    }

    private void runManualInjections() throws IOException, InterruptedException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Path customInjections = Paths.get(classLoader.getResource(specialInjections).getPath());
        List<Path> allFiles = Files.walk(customInjections)
                .filter(x -> !Files.isDirectory(x))
                .collect(Collectors.toList());
        for (Path file : allFiles) {
            manualInjectionService.reset();
            manualInjectionService.inject(file);
            runProgram(timeout);
            stopProgram();
            allLogs.add(runner.getLogEntry());
        }
        outputWriter.write(allLogs);
    }

    private void stopProgram() throws IOException, InterruptedException {
        runner.stop();
    }

    private void runProgram(int timeout) throws InterruptedException {
        CompletableFuture.runAsync(() -> {
            try {
                runner.start();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread.sleep(timeout);
    }

    public static KeySet getKeySetBelow(String startKey, KeySet set) {
        KeySet result = KeySet.create();
        for (int i = 0; i < set.length(); i++) {  //Traverse the set
            if (set.at(i).getName().startsWith(startKey)) {
                result.append(set.at(i));
            }
        }
        return result;
    }
}
