package org.libelektra.errortypes;

import org.libelektra.InjectionMeta;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.service.RandomizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static java.util.Objects.nonNull;

@Component
public class ResourceError extends AbstractErrorType {

    private final static Logger LOG = LoggerFactory.getLogger(ResourceError.class);

    public static int TYPE_ID = 3;

    @Override
    public int getInjectionInt() {
        return TYPE_ID;
    }

    @Override
    public List<InjectionMeta> getBelongingMetadatas() {
        return Arrays.asList(Metadata.values());
    }

    @Autowired
    public ResourceError(RandomizerService randomizerService) {
        super(randomizerService);
    }

    public KeySet applyResourceError(KeySet set, Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (currentKey.getName().startsWith(Metadata.RESOURCE_ERROR.getMetadata())) {
                return resourceError(set, key);
            }
            currentKey = key.nextMeta();
        }
        return set;
    }

    private KeySet resourceError(KeySet set, Key key) {
        LOG.debug("Applying resource error to {}", key.getName());
        String metadata = Metadata.RESOURCE_ERROR.getMetadata();
        String value = key.getString();
        List<String> allMetaArrayValues = extractMetaDataArray(key, metadata);
        key = removeAffectingMetaArray(key, metadata);
        set.append(key);

        if (allMetaArrayValues.size() == 0) {
            LOG.warn("Cannot apply resource error without provided alternatives");
            return set;
        }

        int randomPick = randomizerService.getNextInt(allMetaArrayValues.size());
        String newValue = allMetaArrayValues.get(randomPick);
        key.setString(newValue);
        set.append(key);

        String message = String.format("Resource Error [%s ===> %s] on %s",
                value, newValue, key.getName());
        LOG.debug(message);

        return set;
    }

    public static enum Metadata implements InjectionMeta {
        RESOURCE_ERROR("inject/resource");

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
