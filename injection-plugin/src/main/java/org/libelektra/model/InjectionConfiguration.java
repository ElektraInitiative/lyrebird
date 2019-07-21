package org.libelektra.model;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InjectionConfiguration {

    private String tmpRunConfig;
    private String parentPath;
    private String specPath;
    private String injectPath;

    public InjectionConfiguration(
            @Value("${injection.run-config-location}") String tmpRunConfig,
            @Value("${mountpoint.config}") String parentPath,
            @Value("${mountpoint.specification}") String specPath,
            @Value("${mountpoint.inject}") String injectPath) {
        this.parentPath = parentPath;
        this.specPath = specPath;
        this.tmpRunConfig = tmpRunConfig;
        this.injectPath = injectPath;
    }

    public String getParentPath() {
        return parentPath;
    }

    public String getSpecPath() {
        return specPath;
    }

    public String getRunConfig() {
        return tmpRunConfig;
    }

    public String getInjectPath() {
        return injectPath;
    }

    public void setRunConfig(String tmpRunConfig) {
        this.tmpRunConfig = tmpRunConfig;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public void setSpecPath(String specPath) {
        this.specPath = specPath;
    }

    public void setInjectPath(String injectPath) {
        this.injectPath = injectPath;
    }
}
