package org.libelektra.errortypes;

import org.libelektra.InjectionMeta;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.service.RandomizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Objects.nonNull;

@Component
public class SemanticError extends AbstractErrorType {

    private final static Logger LOG = LoggerFactory.getLogger(SemanticError.class);
    public static int TYPE_ID = 2;

    @Autowired
    public SemanticError(RandomizerService randomizerService) {
        super(randomizerService);
    }

    public KeySet applySemanticError(KeySet set, Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (currentKey.getName().startsWith(Metadata.SEMANTIC_ERROR.getMetadata())) {
                return semanticError(set, key);
            }
            currentKey = key.nextMeta();
        }
        return set;
    }

    private KeySet semanticError(KeySet set, Key key) {
        LOG.debug("Applying semantic error to {}", key.getName());
        String value = key.getString();
        List<String> allMetaArrayValues = extractMetaDataArray(key, Metadata.SEMANTIC_ERROR.getMetadata());
        key = removeAffectingMetaArray(key, Metadata.SEMANTIC_ERROR.getMetadata());
        set.append(key);

        if (allMetaArrayValues.size() == 0) {
            LOG.warn("Cannot apply semantic error without provided alternatives");
            return set;
        }

        int randomPick = randomizerService.getNextInt(allMetaArrayValues.size());
        String newValue = allMetaArrayValues.get(randomPick);
        key.setString(newValue);
        set.append(key);

        String message = String.format("Semantic Error [%s ===> %s] on %s",
                value, newValue, key.getName());
        LOG.debug(message);

        return set;
    }

    public static enum Metadata implements InjectionMeta {
        SEMANTIC_ERROR("inject/semantic");

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
