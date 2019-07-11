package org.libelektra.model;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InjectionConfiguration {

    private final boolean withSpecification;
    private final String tmpRunConfig;
    private final String parentPath;
    private final String specPath;
    private final String injectPath;

    public InjectionConfiguration(
            @Value("${injection.with-specification}") boolean withSpecification,
            @Value("${injection.run-config-location}") String tmpRunConfig,
            @Value("${mountpoint.config}") String parentPath,
            @Value("${mountpoint.specification}") String specPath,
            @Value("${mountpoint.inject}") String injectPath) {
        this.withSpecification = withSpecification;
        this.parentPath = parentPath;
        this.specPath = specPath;
        this.tmpRunConfig = tmpRunConfig;
        this.injectPath = injectPath;
    }

    public boolean isWithSpecification() {
        return withSpecification;
    }

    public String getParentPath() {
        return parentPath;
    }

    public String getSpecPath() {
        return specPath;
    }

    public String getTmpRunConfig() {
        return tmpRunConfig;
    }

    public String getInjectPath() {
        return injectPath;
    }
}
