package org.libelektra.errortypes;

import org.libelektra.InjectionMeta;
import org.libelektra.Key;
import org.libelektra.KeySet;

public class InjectionData {
    private KeySet set;
    private Key injectKey;
    private String defaultValue;
    private String injectPath;
    private InjectionMeta injectionType;

    public InjectionData(KeySet set, Key injectKey, String defaultValue, String injectPath, InjectionMeta injectionType) {
        this.set = set;
        this.injectKey = injectKey;
        this.injectPath = injectPath;
        this.injectionType = injectionType;
        this.defaultValue = defaultValue;
    }

    public KeySet getSet() {
        return set;
    }

    public Key getInjectKey() {
        return injectKey;
    }

    public String getInjectPath() {
        return injectPath;
    }

    public InjectionMeta getInjectionType() {
        return injectionType;
    }

    public void setSet(KeySet set) {
        this.set = set;
    }

    public void setInjectKey(Key injectKey) {
        this.injectKey = injectKey;
    }

    public void setInjectPath(String injectPath) {
        this.injectPath = injectPath;
    }

    public void setInjectionType(InjectionMeta injectionType) {
        this.injectionType = injectionType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
