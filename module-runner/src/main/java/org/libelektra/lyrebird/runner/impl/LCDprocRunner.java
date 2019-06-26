package org.libelektra.lyrebird.runner.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.Tailer;
import org.libelektra.*;
import org.libelektra.lyrebird.errortype.ErrorType;
import org.libelektra.lyrebird.model.LogEntry;
import org.libelektra.model.InjectionDataResult;
import org.libelektra.service.KDBService;
import org.libelektra.lyrebird.runner.ApplicationRunner;
import org.libelektra.service.RandomizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class LCDprocRunner implements ApplicationRunner {

    private KDBService kdbService;
    private final static Logger LOG = LoggerFactory.getLogger(LCDprocRunner.class);

    private Set<ErrorType> allowedErrorTypes;
    private Process process;
    private RandomizerService randomizerService;
    private final InjectionPlugin injectionPlugin;
    private InjectionDataResult injectionDataResult;

    private SysLogListener sysLogListener;
    private Tailer tailer;
    private final String LOG_LOCATION = "/var/log/syslog";

    private LogEntry currentLogEntry;

    public final static String LCDSERVER_RUN_CONFIG = "lcdproc/LCDd-run.ini";
    public final static String LCDSERVER_INJECT_CONFIG = "lcdproc/LCDd-inject.ini";
    public final static String TEMP_RUN_CONFIG = "/tmp/lcdd-tmp.conf";
    public final static String TEMP_ERROR_CONFIG = "/tmp/lcdd-inject.conf";
    public static String KDB_LCDPROC_PATH;
    public final static String KDB_LCDPROC_INJECT_PATH = ELEKTRA_NAMESPACE+"/inject/lcdproc";

    private KeySet errorConfigKeySet;

    @Autowired
    public LCDprocRunner(KDBService kdbService,
                         RandomizerService randomizerService,
                         InjectionPlugin injectionPlugin,
                         @Value("${config.mountpoint}") String parentPath) throws KDB.KDBException {
        this.injectionPlugin = injectionPlugin;
        KDB_LCDPROC_PATH = parentPath;
        this.kdbService = kdbService;
        this.randomizerService = randomizerService;
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            File errorConfigFile = new File(classLoader.getResource(LCDSERVER_INJECT_CONFIG).getFile());
            File runConfig = new File(TEMP_ERROR_CONFIG);
            FileUtils.copyFile(errorConfigFile, runConfig);
            Util.executeCommand(String.format("kdb mount %s %s ni", TEMP_ERROR_CONFIG, KDB_LCDPROC_INJECT_PATH));
        } catch (NullPointerException e) {
            LOG.error("Could not find configuration for {}", LCDSERVER_RUN_CONFIG);
        } catch (IOException e) {
            e.printStackTrace();
        }
        errorConfigKeySet = kdbService.getKeySetBelowPath(KDB_LCDPROC_INJECT_PATH);
    }


    @Override
    public void start() throws IOException, InterruptedException {

//        String[] command = (new String[]{"gnome-terminal", "-e", String.format("LCDd -f -c %s", TEMP_RUN_CONFIG)});
        String[] command = new String[]{"LCDd", "-f", "-c", TEMP_RUN_CONFIG};

        process = new ProcessBuilder(command)
//                .redirectErrorStream(true)
                .start();

        handleLogMessage(process);

//        sysLogListener = new SysLogListener();
//        File file = new File(LOG_LOCATION);
//        tailer = Tailer.create(file, sysLogListener);
    }

    @Override
    public void stop() throws IOException, InterruptedException {
        Runtime.getRuntime().exec("killall -s SIGINT LCDd");
        process.waitFor();
//        tailer.stop();
//        handleLogMessage(sysLogListener.getLogMessages());
        LOG.info("LCDd exitvalue: {}", process.exitValue());
    }

    @Override
    public boolean injectInConfiguration() throws KDB.KDBException {
        int nextRandom = randomizerService.getNextInt(errorConfigKeySet.length());
        Key injectKey = errorConfigKeySet.at(nextRandom);
        String path = injectKey.getName().replace(KDB_LCDPROC_INJECT_PATH, KDB_LCDPROC_PATH);
        KeySet configKeySet = kdbService.getKeySetBelowPath(KDB_LCDPROC_PATH);
        InjectionDataResult injectionDataResult = injectionPlugin.kdbSet(configKeySet, injectKey, path);
        this.injectionDataResult = injectionDataResult;
        injectAdditionalContextDependant(configKeySet, injectionDataResult);
        return injectionDataResult.isWasInjectionSuccessful();
    }

    // We want to use the concrete driver to force additional error possibility
    @Override
    public void injectAdditionalContextDependant(KeySet set, InjectionDataResult data) {
        String key = data.getKey();
        if (key == null) {
            return;
        }
        String postInjectPath = key.replace(KDB_LCDPROC_PATH, "");
        if (postInjectPath.length() <= 1) {
            return;
        }
        String driver = postInjectPath.split("/")[1];
        if (driver.equals("server")) {
            return;
        }

        set.append(Key.create(KDB_LCDPROC_PATH + "/server/Driver", driver));

        try {
            kdbService.set(set, KDB_LCDPROC_PATH);
        } catch (KDB.KDBException e) {
            LOG.error("Could not do context dependant injection", e);
        }
    }

    @Override
    public void resetConfiguration() throws IOException {
        try {
            Util.executeCommand(String.format("kdb umount %s", KDB_LCDPROC_PATH));
            kdbService.close();
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            File initialConfigFile = new File(classLoader.getResource(LCDSERVER_RUN_CONFIG).getFile());
            File runConfig = new File(TEMP_RUN_CONFIG);
            FileUtils.deleteQuietly(runConfig);
            FileUtils.copyFile(initialConfigFile, runConfig);
            Util.executeCommand(String.format("kdb mount %s %s ini", TEMP_RUN_CONFIG, KDB_LCDPROC_PATH));
            kdbService.initKDB();
            errorConfigKeySet = kdbService.getKeySetBelowPath(KDB_LCDPROC_INJECT_PATH);
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

            currentLogEntry = new LogEntry();
            if (!errorStarts) {
                errorMessages = allMessages;
            }
            errorMessages = errorMessages.stream().filter(str -> !str.isEmpty()).collect(Collectors.toList());
            currentLogEntry.setLogMessage(String.join("\n", errorMessages));
            currentLogEntry.setInjectionDataResult(this.injectionDataResult);
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
    public void setErrorTypes(Set<ErrorType> errorTypes) {
        this.allowedErrorTypes = errorTypes;
    }

    @Override
    @PreDestroy
    public void cleanUp() throws IOException {
        Util.executeCommand(String.format("kdb umount %s", KDB_LCDPROC_PATH));
        Util.executeCommand(String.format("kdb umount %s", KDB_LCDPROC_INJECT_PATH));
    }
}
