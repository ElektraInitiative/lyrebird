package org.libelektra.errortypes;

import org.libelektra.InjectionMeta;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.model.InjectionData;
import org.libelektra.model.InjectionDataResult;
import org.libelektra.service.RandomizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static java.util.Objects.isNull;

@Component
public class LimitError extends AbstractErrorType {

    private final static Logger LOG = LoggerFactory.getLogger(LimitError.class);

    @Autowired
    public LimitError(RandomizerService randomizerService) {
        super(randomizerService);
        this.TYPE_ID = 6;
    }

    @Override
    public KeySet doInject(InjectionData injectionData) {
        injectionData.getInjectKey().rewindMeta();
        if (injectionData.getInjectionType().equals(LimitError.Metadata.LIMIT_ERROR_MIN) ||
                injectionData.getInjectionType().equals(LimitError.Metadata.LIMIT_ERROR_MAX)) {
            return applyError(injectionData.getSet(), injectionData.getInjectKey(), injectionData.getInjectPath());
        }
        return injectionData.getSet();
    }

    private KeySet applyError(KeySet set, Key injectKey, String injectPath) {
        LOG.debug("Applying limit error to {}", injectKey.getName());
        String value = set.lookup(injectPath).getString();

        String min = injectKey.getMeta(Metadata.LIMIT_ERROR_MIN.getMetadata()).getString();
        String max = injectKey.getMeta(Metadata.LIMIT_ERROR_MAX.getMetadata()).getString();

        if (isNull(min) && isNull(max)) {
            LOG.warn("Min or max value was not supplied!");
            return set;
        }

        int random = randomizerService.getNextInt(2);
        String newValue;
        InjectionMeta injectionMeta;
        if (isNull(max) || random == 0) {
            newValue = min;
            injectionMeta = Metadata.LIMIT_ERROR_MIN;
        } else {
            injectionMeta = Metadata.LIMIT_ERROR_MAX;
            newValue = max;
        }

        Key newKey = Key.create(injectPath);
        newKey.setString(newValue);
        set.append(newKey);

        this.injectionDataResult = new InjectionDataResult.Builder(true)
                .withOldValue(value)
                .withNewValue(newValue)
                .withKey(injectPath)
                .withInjectionMeta(injectionMeta)
                .build();

        return set;
    }

    @Override
    public int getInjectionInt() {
        return TYPE_ID;
    }

    @Override
    public List<InjectionMeta> getBelongingMetadatas() {
        return Arrays.asList(Metadata.values());
    }

    public static enum Metadata implements InjectionMeta {
        LIMIT_ERROR_MIN("inject/limit/min"),
        LIMIT_ERROR_MAX("inject/limit/max");

        private final String metadata;

        Metadata(String metadata) {
            this.metadata = metadata;
        }

        public String getMetadata() {
            return metadata;
        }

        @Override
        public String getCategory() {
            return "Limit Error";
        }

        public static boolean hasMetadata(String keyMeta) {
            for (Metadata meta : Metadata.values()) {
                if (keyMeta.equals(meta.metadata)) {
                    return true;
                }
            }
            return false;
        }
    }

}
