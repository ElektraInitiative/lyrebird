package org.elektra.errortypes;

import org.elektra.InjectionPlugin;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static java.util.Objects.nonNull;

public class StructureError {

    private final static Logger LOG = LoggerFactory.getLogger(StructureError.class);

    public KeySet applyStructureError(KeySet set, Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (currentKey.getName().equals(Metadata.SECTION_REMOVE.getMetadata())) {
                return removeSection(set, key);
            }
            currentKey = key.nextMeta();
        }
        return set;
    }

    private KeySet removeSection(KeySet set, Key startKey) {
        LOG.debug("Applying Structure Error to {}", startKey.getName());
        Iterator<Key> iterator = set.iterator();
        while (iterator.hasNext()) {
            Key current = iterator.next();
            if (current.getName().startsWith(startKey.getName())) {
                iterator.remove();
            }
        }
        return set;
    }

    public static enum Metadata {
        SECTION_REMOVE("inject/structure/sectionRemove");

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
