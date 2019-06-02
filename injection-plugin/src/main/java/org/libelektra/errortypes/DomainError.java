package org.libelektra.errortypes;

import org.libelektra.InjectionMeta;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Objects.nonNull;
import static org.libelektra.util.RandomizerSingelton.Randomizer;

public class DomainError extends AbstractErrorType {

    private final static Logger LOG = LoggerFactory.getLogger(DomainError.class);


    public KeySet applyDomainError(KeySet set, Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (currentKey.getName().startsWith(Metadata.DOMAIN_ERROR.getMetadata())) {
                return resourceError(set, key);
            }
            currentKey = key.nextMeta();
        }
        return set;
    }

    private KeySet resourceError(KeySet set, Key key) {
        LOG.debug("Applying resource error to {}", key.getName());
        String metadata = Metadata.DOMAIN_ERROR.getMetadata();
        String value = key.getString();
        List<String> allMetaArrayValues = extractMetaDataArray(key, metadata);
        Randomizer randomizer = getRandomizer(key);
        key = removeAffectingMetaArray(key, metadata);
        set.append(key);

        if (allMetaArrayValues.size() == 0) {
            LOG.warn("Cannot apply domain error without provided alternatives");
            return set;
        }

        int randomPick = randomizer.getNextInt(allMetaArrayValues.size());
        String newValue = allMetaArrayValues.get(randomPick);
        key.setString(newValue);
        set.append(key);

        String message = String.format("Domain Error [%s ===> %s] on %s",
                value, newValue, key.getName());
        LOG.debug(message);

        return set;
    }

    public static enum Metadata implements InjectionMeta {
        DOMAIN_ERROR("inject/domain");

        private final String metadata;

        Metadata(String metadata) {
            this.metadata = metadata;
        }

        public String getMetadata() {
            return metadata;
        }

        public static boolean hasMetadata(String keyMeta) {
            for (Metadata meta : Metadata.values()) {
                if (keyMeta.startsWith(meta.metadata)) {
                    return true;
                }
            }
            return false;
        }
    }

}
