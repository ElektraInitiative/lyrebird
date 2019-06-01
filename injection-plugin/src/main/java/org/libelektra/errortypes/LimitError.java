package org.libelektra.errortypes;

import org.libelektra.Key;
import org.libelektra.KeySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.libelektra.util.RandomizerSingelton.Randomizer;

public class LimitError extends AbstractErrorType {

    private final static Logger LOG = LoggerFactory.getLogger(LimitError.class);


    public KeySet applyLimitError(KeySet set, Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (currentKey.getName().startsWith(Metadata.LIMIT_ERROR_MAX.getMetadata()) ||
                    currentKey.getName().startsWith(Metadata.LIMIT_ERROR_MIN.getMetadata())) {
                return applyError(set, key);
            }
            currentKey = key.nextMeta();
        }
        return set;
    }

    private KeySet applyError(KeySet set, Key key) {
        LOG.debug("Applying limit error to {}", key.getName());
        String value = key.getString();

        String min = key.getMeta(Metadata.LIMIT_ERROR_MIN.getMetadata()).getString();
        String max = key.getMeta(Metadata.LIMIT_ERROR_MAX.getMetadata()).getString();

        if (isNull(min) && isNull(max)) {
            LOG.warn("Min or max value was not supplied!");
            return set;
        }

        Randomizer randomizer = getRandomizer(key);
        key = removeAffectingMeta(key, Metadata.LIMIT_ERROR_MIN.getMetadata(), Metadata.LIMIT_ERROR_MAX.getMetadata());
        set.append(key);

        int random = randomizer.getNextInt(2);
        String newValue;
        if (isNull(max) || random == 0) {
            newValue = min;
        } else {
            newValue = max;
        }

        key.setString(newValue);
        set.append(key);


        String message = String.format("Limit Error [%s ===> %s] on %s",
                value, newValue, key.getName());
        LOG.debug(message);

        return set;
    }

    public static enum Metadata {
        LIMIT_ERROR_MIN("inject/limit/min"),
        LIMIT_ERROR_MAX("inject/limit/max");

        private final String metadata;

        Metadata(String metadata) {
            this.metadata = metadata;
        }

        public String getMetadata() {
            return metadata;
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
