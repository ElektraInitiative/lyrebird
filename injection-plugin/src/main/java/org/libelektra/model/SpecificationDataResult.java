package org.libelektra.model;

import org.libelektra.service.SpecificationEnforcer;

import java.util.List;

public class SpecificationDataResult {

    private String errorMessage;
    private SpecificationEnforcer.SpecPlugins plugin;
    private List<String> warnings;
    private boolean hasDetectedError;

    private SpecificationDataResult() {

    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public SpecificationEnforcer.SpecPlugins getPlugin() {
        return plugin;
    }

    public boolean hasDetectedError() {
        return hasDetectedError;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public static SpecificationDataResult noDetectionResult() {
        SpecificationDataResult result = new SpecificationDataResult();
        result.hasDetectedError = false;
        return result;
    }

    public static SpecificationDataResult detectionResult(SpecificationEnforcer.SpecPlugins plugin,
                                                          String errorMessage,
                                                          List<String> warnings) {
        SpecificationDataResult result = new SpecificationDataResult();
        result.hasDetectedError = true;
        result.plugin = plugin;
        result.errorMessage = errorMessage;
        result.warnings = warnings;
        return result;
    }

}
