package org.elektra.errortypes;

import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.util.RandomizerSingelton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.stream.IntStream;

import static java.util.Objects.nonNull;
import static org.elektra.InjectionPlugin.*;
import static org.libelektra.util.RandomizerSingelton.Randomizer;

public class TypoError {

    private final static Logger LOG = LoggerFactory.getLogger(TypoError.class);

    public KeySet applyStructureError(KeySet set, Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (currentKey.getName().equals(Metadata.TYPO_TRANSPOSITION.getMetadata())) {
                return transposition(set, key);
            } else if (currentKey.getName().equals(Metadata.TYPO_CHANGE_CHAR.getMetadata())) {
                return changeChar(set, key);
            } else if (currentKey.getName().equals(Metadata.TYPO_DELETION.getMetadata())) {
                return deletion(set, key);
            } else if (currentKey.getName().equals(Metadata.TYPO_INSERTION.getMetadata())) {
                return insertion(set, key);
            } else if (currentKey.getName().equals(Metadata.TYPO_SPACE.getMetadata())) {
                return space(set, key);
            } else if (currentKey.getName().equals(Metadata.TYPO_TOGGLE.getMetadata())) {
                return caseToggle(set, key);
            }
            currentKey = key.nextMeta();
        }
        return set;
    }

    private KeySet transposition(KeySet set, Key startKey) {
        LOG.debug("Applying Typo Error [transposition] to {}", startKey.getName());
        return set;
    }

    private KeySet insertion(KeySet set, Key startKey) {
        LOG.debug("Applying Typo Error [insertion] to {}", startKey.getName());
        return set;
    }

    private KeySet deletion(KeySet set, Key startKey) {
        LOG.debug("Applying Typo Error [deletion] to {}", startKey.getName());
        return set;
    }

    private KeySet changeChar(KeySet set, Key startKey) {
        LOG.debug("Applying Typo Error [changeChar] to {}", startKey.getName());
        return set;
    }

    private KeySet space(KeySet set, Key startKey) {
        LOG.debug("Applying Typo Error [space] to {}", startKey.getName());
        return set;
    }

    private KeySet caseToggle(KeySet set, Key startKey) {
        LOG.debug("Applying Typo Error [caseToggle] to {}", startKey.getName());
        return set;
    }

    public static enum Metadata {
        TYPO_TRANSPOSITION("inject/typo/transposition"),
        TYPO_INSERTION("inject/typo/insertion"),
        TYPO_DELETION("inject/typo/deletion"),
        TYPO_CHANGE_CHAR("inject/typo/change/char"),
        TYPO_SPACE("inject/typo/space"),
        TYPO_TOGGLE("inject/typo/case/toggle");

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
