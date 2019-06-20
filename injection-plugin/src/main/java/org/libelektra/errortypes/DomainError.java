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

@Component
public class DomainError extends AbstractErrorType {

    private final static Logger LOG = LoggerFactory.getLogger(DomainError.class);

    public static int TYPE_ID = 5;

    @Autowired
    public DomainError(RandomizerService randomizerService) {
        super(randomizerService);
    }

    public KeySet applyDomainError(KeySet set, Key injectKey, String injectPath, InjectionMeta injectionType) {
        injectKey.rewindMeta();
        if (injectionType.equals(Metadata.DOMAIN_ERROR)) {
            return domainError(set, injectKey, injectPath);
        }
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

    private KeySet domainError(KeySet set, Key injectKey, String injectPath) {
        LOG.debug("Applying domain error to {}", injectPath);
        String metadata = Metadata.DOMAIN_ERROR.getMetadata();
        String value = injectKey.getString();
        List<String> allMetaArrayValues = extractMetaDataArray(injectKey, metadata);

        if (allMetaArrayValues.size() == 0) {
            LOG.warn("Cannot apply domain error without provided alternatives");
            return set;
        }

        int randomPick = randomizerService.getNextInt(allMetaArrayValues.size());
        String newValue = allMetaArrayValues.get(randomPick);
        Key newKey = Key.create(injectPath);
        newKey.setString(newValue);
        set.append(newKey);

        String message = String.format("Domain Error [%s ===> %s] on %s",
                value, newValue, injectPath);
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
