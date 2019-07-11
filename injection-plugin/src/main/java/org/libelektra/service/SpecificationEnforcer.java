package org.libelektra.service;

import org.apache.commons.lang3.StringUtils;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.Plugin;
import org.libelektra.model.SpecificationDataResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SpecificationEnforcer {

    private final String specPath;
    private final String configPath;
    private final List<Plugin> allPlugins;
    private final Key errorKey = Key.create("user/error");

    public SpecificationEnforcer(
            @Value("${mountpoint.specification}") String specPath,
            @Value("${mountpoint.config}") String configPath) {
        this.specPath = specPath;
        this.configPath = configPath;
        allPlugins = new ArrayList<>();
        Arrays.stream(SpecPlugins.values()).forEach(
                specPlugin -> allPlugins.add(new Plugin(specPlugin.getPluginName(), Key.create(specPath)))
        );
    }


    public SpecificationDataResult checkSpecification(KeySet specification, KeySet configKeys, Key changedKey) {
        Key correspondingSpecKey = specification.lookup(changedKey.getName().replace(configPath, specPath));
        changedKey.copyAllMeta(correspondingSpecKey);
        for (Plugin specPlugin : allPlugins) {
            int result = specPlugin.kdbSet(configKeys, errorKey);
            if (result < 1) {
                return SpecificationDataResult.detectionResult(SpecPlugins.valueOf(specPlugin.getName().toUpperCase()),
                        errorKey.getMeta("error/reason").getString(), extractWarnings());
            }
        }

        return SpecificationDataResult.noDetectionResult();
    }

    private List<String> extractWarnings() {
        List<String> warnings = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String currentWarning = StringUtils.leftPad(String.valueOf(i), 2, '0');
            Key warningReasonKey = errorKey.getMeta(String.format("warnings/#%s/reason", currentWarning));
            if (warningReasonKey.isNull()) {
                return warnings;
            }
            warnings.add(warningReasonKey.getString());
        }
        return warnings;
    }

    public enum SpecPlugins {
        TYPE("type"),
        PATH("path"),
        NETWORK("network"),
        RANGE("range"),
//        REFERENCE("reference"),
//        CONDITIONALS("conditionals"),
        VALIDATION("validation");

        private final String pluginName;

        SpecPlugins(String pluginName) {
            this.pluginName = pluginName;
        }

        public String getPluginName() {
            return pluginName;
        }
    }
}
