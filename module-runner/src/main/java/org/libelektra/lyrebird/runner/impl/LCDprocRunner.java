package org.libelektra.lyrebird.runner.impl;

import org.apache.commons.io.FileUtils;
import org.libelektra.*;
import org.libelektra.lyrebird.model.LogEntry;
import org.libelektra.lyrebird.runner.ApplicationRunner;
import org.libelektra.model.InjectionConfiguration;
import org.libelektra.model.InjectionDataResult;
import org.libelektra.model.InjectionResult;
import org.libelektra.model.SpecificationDataResult;
import org.libelektra.service.KDBService;
import org.libelektra.service.RandomizerService;
import org.libelektra.service.SpecificationEnforcer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("lcdproc")
public class LCDprocRunner implements ApplicationRunner {

    private KDBService kdbService;
    private RandomizerService randomizerService;
    private final SpecificationEnforcer specificationEnforcer;

    private final static Logger LOG = LoggerFactory.getLogger(LCDprocRunner.class);

    private Process process;

    private SpecificationDataResult specificationDataResult;
    private LogEntry currentLogEntry;

    public final static String LCDSERVER_RUN_CONFIG = "lcdproc/LCDd-run.ini";
    public final static String LCDSERVER_INJECT_CONFIG = "lcdproc/LCDd-inject.ini";
    public final static String LCDSERVER_SPEC_CONFIG = "lcdproc/LCDd-spec.ini";
    public final static String TEMP_ERROR_CONFIG = "/tmp/lcdd-inject.conf";
    public final static String TEMP_SPEC_CONFIG = "/tmp/lcdd-spec.conf";

    private final InjectionPlugin injectionPlugin;
    private KeySet errorConfigKeySet;
    private KeySet specificationKeySet;
    private final InjectionConfiguration injectionConfiguration;

    @Autowired
    public LCDprocRunner(KDBService kdbService,
                         RandomizerService randomizerService,
                         SpecificationEnforcer specificationEnforcer,
                         InjectionPlugin injectionPlugin,
                         InjectionConfiguration injectionConfiguration
    ) throws KDB.KDBException {
        this.injectionConfiguration = injectionConfiguration;
        this.specificationEnforcer = specificationEnforcer;
        this.injectionPlugin = injectionPlugin;
        this.kdbService = kdbService;
        this.randomizerService = randomizerService;
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            File errorConfigFile = new File(classLoader.getResource(LCDSERVER_INJECT_CONFIG).getFile());
            File specConfigFile = new File(classLoader.getResource(LCDSERVER_SPEC_CONFIG).getFile());
            //TODO: Filter for settings with numbers
            File runConfig = new File(TEMP_ERROR_CONFIG);
            File specConfig = new File(TEMP_SPEC_CONFIG);
            FileUtils.copyFile(errorConfigFile, runConfig);
            FileUtils.copyFile(specConfigFile, specConfig);
            currentLogEntry = new LogEntry();
            Util.executeCommand(String.format("kdb mount %s %s ni", TEMP_ERROR_CONFIG, injectionConfiguration.getInjectPath()));
            Util.executeCommand(String.format("kdb mount %s %s ni", TEMP_SPEC_CONFIG, injectionConfiguration.getSpecPath()));
        } catch (NullPointerException e) {
            LOG.error("Could not find configuration for {}", LCDSERVER_RUN_CONFIG);
        } catch (IOException e) {
            e.printStackTrace();
        }
        kdbService.initKDB();
        errorConfigKeySet = kdbService.getKeySetBelowPath(injectionConfiguration.getInjectPath());
        specificationKeySet = kdbService.getKeySetBelowPath(injectionConfiguration.getSpecPath());
    }


    @Override
    public void start() throws IOException {
//        String[] command = (new String[]{"gnome-terminal", "-e", String.format("LCDd -f -c %s", tmpRunConfig)});
        String[] command = new String[]{"LCDd", "-f", "-c", injectionConfiguration.getTmpRunConfig()};

        process = new ProcessBuilder(command)
                .start();

        handleLogMessage(process);
    }

    @Override
    public void stop() throws IOException, InterruptedException {
        Runtime.getRuntime().exec("killall -s SIGINT LCDd");
        process.waitFor();
        LOG.debug("LCDd exitvalue: {}", process.exitValue());
    }

    @Override
    public InjectionResult injectInConfiguration() throws KDB.KDBException {
        int nextRandom = randomizerService.getNextInt(errorConfigKeySet.length());
        Key injectKey = errorConfigKeySet.at(nextRandom);
        String path = injectKey.getName().replace(injectionConfiguration.getInjectPath(), injectionConfiguration.getParentPath());
        KeySet configKeySet = kdbService.getKeySetBelowPath(injectionConfiguration.getParentPath());
        InjectionDataResult injectionDataResult = injectionPlugin.kdbSet(configKeySet, injectKey, path);
        currentLogEntry.setInjectionDataResult(injectionDataResult);
        this.specificationDataResult = specificationEnforcer.checkSpecification(specificationKeySet,
                configKeySet, injectKey);
        currentLogEntry.setSpecificationDataResult(this.specificationDataResult);
        injectAdditionalContextDependant(configKeySet, injectionDataResult);
        return new InjectionResult(injectionDataResult, specificationDataResult);
    }

    // We want to use the concrete driver to force additional error possibility
    @Override
    public void injectAdditionalContextDependant(KeySet set, InjectionDataResult data) {
        String key = data.getKey();
        if (key == null) {
            return;
        }
        String postInjectPath = key.replace(injectionConfiguration.getParentPath(), "");
        if (postInjectPath.length() <= 1) {
            return;
        }
        String driver = postInjectPath.split("/")[1];
        if (driver.equals("server") || driver.equals("menu")) {
            return;
        }

        set.append(Key.create(injectionConfiguration.getParentPath() + "/server/Driver", driver));

        try {
            kdbService.set(set, injectionConfiguration.getParentPath());
        } catch (KDB.KDBException e) {
            LOG.error("Could not do context dependant injection", e);
        }
    }

    @Override
    public void resetConfiguration() throws IOException {
        try {
            Util.executeCommand(String.format("kdb umount %s", injectionConfiguration.getParentPath()));
            kdbService.close();
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            File initialConfigFile = new File(classLoader.getResource(LCDSERVER_RUN_CONFIG).getFile());
            File runConfig = new File(injectionConfiguration.getTmpRunConfig());
            FileUtils.deleteQuietly(runConfig);
            FileUtils.copyFile(initialConfigFile, runConfig);
            Util.executeCommand(String.format("kdb mount %s %s ini", injectionConfiguration.getTmpRunConfig(), injectionConfiguration.getParentPath()));
            kdbService.initKDB();
            this.currentLogEntry = new LogEntry();
        } catch (NullPointerException e) {
            LOG.error("Could not find configuration for {}", LCDSERVER_RUN_CONFIG);
        }
    }

    private void handleLogMessage(Process process) {
        try {
            List<String> errorMessages = new ArrayList<>();
            List<String> allMessages = new ArrayList<>();
            String errorLine;
            InputStream error = process.getErrorStream();
            InputStreamReader isrerror = new InputStreamReader(error);
            BufferedReader bre = new BufferedReader(isrerror);
            boolean errorStarts = false;
            String startedMessage = "Inc., 51 Franklin Street";
            while ((errorLine = bre.readLine()) != null) {
                allMessages.add(errorLine);
                if (errorStarts) {
                    errorMessages.add(errorLine);
                }
                if (errorLine.startsWith(startedMessage)) {
                    errorStarts=true;
                }
            }

            if (!errorStarts) {
                errorMessages = allMessages;
            }
            errorMessages = errorMessages.stream().filter(str -> !str.isEmpty()).collect(Collectors.toList());
            currentLogEntry.setLogMessage(String.join("\n", errorMessages));
            if (errorMessages.size() > 0) {
                currentLogEntry.setErrorLogEntry(errorMessages.get(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO!
        currentLogEntry.setResultType(LogEntry.RESULT_TYPE.NONE);
    }


    @Override
    public LogEntry getLogEntry() {
        return currentLogEntry;
    }

    @Override
    @PreDestroy
    public void cleanUp() throws IOException {
        Util.executeCommand(String.format("kdb umount %s", injectionConfiguration.getParentPath()));
        Util.executeCommand(String.format("kdb umount %s", injectionConfiguration.getInjectPath()));
        Util.executeCommand(String.format("kdb umount %s", injectionConfiguration.getSpecPath()));
        FileUtils.deleteQuietly(new File(injectionConfiguration.getTmpRunConfig()));
        FileUtils.deleteQuietly(new File(TEMP_ERROR_CONFIG));
        FileUtils.deleteQuietly(new File(TEMP_SPEC_CONFIG));
    }
}
