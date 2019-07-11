package org.libelektra.model;

import org.libelektra.InjectionMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InjectionDataResult {

    private final static Logger LOG = LoggerFactory.getLogger(InjectionDataResult.class);

    private boolean wasInjectionSuccessful;
    private String oldValue;
    private String newValue;
    private String key;
    private InjectionMeta injectionMeta;

    private InjectionDataResult() {
    }

    public InjectionDataResult(InjectionDataResult injectionDataResult) {
        this.wasInjectionSuccessful = injectionDataResult.wasInjectionSuccessful;
        this.oldValue = injectionDataResult.oldValue;
        this.newValue = injectionDataResult.newValue;
        this.key = injectionDataResult.key;
        this.injectionMeta = injectionDataResult.injectionMeta;
    }

    public boolean wasInjectionSuccessful() {
        return wasInjectionSuccessful;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getKey() {
        return key;
    }

    public InjectionMeta getInjectionMeta() {
        return injectionMeta;
    }

    @Override
    public String toString() {
        return "InjectionDataResult{" +
                "wasInjectionSuccessful=" + wasInjectionSuccessful +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                ", key='" + key + '\'' +
                ", injectionMeta=" + injectionMeta +
                '}';
    }

    public void logInjection() {
        if (wasInjectionSuccessful) {
            String log = String.format("%15s %-60s on %s", injectionMeta.getCategory(),
                    String.format("[%s ==> “%s“]", oldValue, newValue),
                    key);
            LOG.info(log);
        }
    }

    public static class Builder {
        private boolean wasInjectionSuccessful;
        private String oldValue;
        private String newValue;
        private String key;
        private InjectionMeta injectionMeta;

        public Builder(boolean wasInjectionSuccessful) {
            this.wasInjectionSuccessful = wasInjectionSuccessful;
        }

        public Builder withOldValue(String oldValue) {
            this.oldValue = oldValue;
            return this;
        }

        public Builder withNewValue(String newValue) {
            this.newValue = newValue;
            return this;
        }

        public Builder withKey(String key) {
            this.key = key;
            return this;
        }

        public Builder withInjectionMeta(InjectionMeta meta) {
            this.injectionMeta = meta;
            return this;
        }

        public InjectionDataResult build() {
            InjectionDataResult result = new InjectionDataResult();
            result.wasInjectionSuccessful = wasInjectionSuccessful;
            result.oldValue = oldValue;
            result.newValue = newValue;
            result.key = key;
            result.injectionMeta = injectionMeta;
            return result;
        }
    }
}
