package org.libelektra.service;

import org.apache.commons.lang3.StringUtils;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.Plugin;
import org.libelektra.model.InjectionConfiguration;
import org.libelektra.model.SpecificationDataResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SpecificationEnforcer {

    private final static Logger LOG = LoggerFactory.getLogger(SpecificationEnforcer.class);

    private final InjectionConfiguration injectionConfiguration;
    private final List<Plugin> allPlugins;
    private Key errorKey = Key.create("user/error");

    @Autowired
    public SpecificationEnforcer(
            InjectionConfiguration injectionConfiguration) {
        this.injectionConfiguration = injectionConfiguration;
        allPlugins = new ArrayList<>();
        Arrays.stream(SpecPlugins.values()).forEach(
                specPlugin -> allPlugins.add(new Plugin(specPlugin.getPluginName(), Key.create(injectionConfiguration.getSpecPath())))
        );
    }


    public SpecificationDataResult checkSpecification(KeySet specification, KeySet configKeys, Key changedKey) {
        Key correspondingSpecKey = specification.lookup(changedKey.getName().replace(injectionConfiguration.getInjectPath(), injectionConfiguration.getSpecPath()).toLowerCase());
        Key correspondingConfigKey = configKeys.lookup(changedKey.getName().replace(injectionConfiguration.getInjectPath(), injectionConfiguration.getParentPath())).dup();
        correspondingConfigKey.copyAllMeta(correspondingSpecKey);
        for (Plugin specPlugin : allPlugins) {
            // Bug in Elektra?
            KeySet tmpKeySet = KeySet.create();
            tmpKeySet.append(correspondingConfigKey);
            int result = specPlugin.kdbSet(tmpKeySet, errorKey);
            if (result < 1) {
                String error = errorKey.getMeta("error/reason").getString();
                List<String> warnings = extractWarnings();
                LOG.info("(Specification caught - {})", specPlugin.getName());
                errorKey.release();
                errorKey = Key.create("user/error");
                return SpecificationDataResult.detectionResult(SpecPlugins.valueOf(specPlugin.getName().toUpperCase()),
                        error, warnings);
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
