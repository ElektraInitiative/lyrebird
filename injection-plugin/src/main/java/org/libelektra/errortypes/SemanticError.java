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
public class SemanticError extends AbstractErrorType {

    private final static Logger LOG = LoggerFactory.getLogger(SemanticError.class);
    public static int TYPE_ID = 2;

    @Autowired
    public SemanticError(RandomizerService randomizerService) {
        super(randomizerService);
    }

    public KeySet applySemanticError(InjectionData injectionData) {
        injectionData.getInjectKey().rewindMeta();
        if (injectionData.getInjectionType().equals(Metadata.SEMANTIC_ERROR)) {
            return semanticError(injectionData.getSet(), injectionData.getInjectKey(), injectionData.getInjectPath());
        }
        return injectionData.getSet();
    }

    private KeySet semanticError(KeySet set, Key key, String injectPath) {
        LOG.debug("Applying semantic error to {}", key.getName());
        String value = set.lookup(injectPath).getString();
        List<String> allMetaArrayValues = extractMetaDataArray(key, Metadata.SEMANTIC_ERROR.getMetadata());

        if (allMetaArrayValues.size() == 0) {
            LOG.warn("Cannot apply semantic error without provided alternatives");
            return set;
        }

        int randomPick = randomizerService.getNextInt(allMetaArrayValues.size());
        String newValue = allMetaArrayValues.get(randomPick);
        Key newKey = Key.create(injectPath);
        newKey.setString(newValue);
        set.append(newKey);

        String message = String.format("Semantic Error [%s ===> %s] on %s",
                value, newValue, injectPath);
        LOG.debug(message);

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
