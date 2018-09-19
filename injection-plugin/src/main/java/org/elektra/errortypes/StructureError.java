package org.elektra.errortypes;

import org.elektra.InjectionPlugin;
import org.libelektra.Key;
import org.libelektra.KeySet;

import static org.elektra.InjectionPlugin.ROOT_KEY;
import static org.elektra.InjectionPlugin.getSeedFromMeta;
import static org.libelektra.util.RandomizerSingelton.Randomizer;

import org.libelektra.util.RandomizerSingelton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;

import static java.util.Objects.nonNull;
import static org.elektra.InjectionPlugin.hasSeedSet;

public class StructureError {

    private final static Logger LOG = LoggerFactory.getLogger(StructureError.class);

    public KeySet applyStructureError(KeySet set, Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (currentKey.getName().equals(Metadata.SECTION_REMOVE.getMetadata())) {
                return removeSection(set, key);
            } else if (currentKey.getName().equals(Metadata.SECTION_REALLOCATE.getMetadata())) {
                return reallocateSection(set, key);
            }
            currentKey = key.nextMeta();
        }
        return set;
    }

    private KeySet removeSection(KeySet set, Key startKey) {
        LOG.debug("Applying Structure Error [removeSection] to {}", startKey.getName());
        Iterator<Key> iterator = set.iterator();
        while (iterator.hasNext()) {
            Key current = iterator.next();
            if (current.getName().startsWith(startKey.getName())) {
                iterator.remove();
            }
        }
        return set;
    }

    //What a disgusting method ... but elektra forces me to do it
    private KeySet reallocateSection(KeySet set, Key key) {
        LOG.debug("Applying Structure Error [reallocateSection] to {}", key.getName());

        //Remove the metadata to prohibit multiple reallocation
        key = key.removeMetaIfPresent(Metadata.SECTION_REALLOCATE.getMetadata());
        set.append(key);

        Randomizer randomizer = RandomizerSingelton.getInstance();
        if (hasSeedSet(key)) {
            randomizer.setSeed(getSeedFromMeta(key));
        }
        String newRoot = findNewRoot(set, key, randomizer);
        KeySet extractedKS = extractKeySet(set, key);

        //We only want to substitute until the last '/'
        int slashLocation=key.getName().lastIndexOf("/");
        String toSubstitute=key.getName().substring(0, slashLocation);

        //Change the path of every Key and add it to a new KS
        KeySet extractedKSwithNewRoot = KeySet.create();
        for (Key current : extractedKS) {
            String newPath = current.getName().replace(toSubstitute, newRoot);
            Key tmpKey = Key.create(newPath, current.getString());

            //Copy all metadata to new Key
            key.rewindMeta();
            Key currentMetaKey = current.currentMeta();
            while (nonNull(currentMetaKey.getName())) {
                tmpKey.setMeta(currentMetaKey.getName(), currentMetaKey.getString());
                currentMetaKey = current.nextMeta();
            }

            extractedKSwithNewRoot.append(tmpKey);
        }

        String message = String.format("Reallocate \n %s ===> %s", key.getName(), newRoot);
        LOG.debug(message);
        removeSection(set, key);
        set.append(extractedKSwithNewRoot);

        return set;
    }

    private KeySet extractKeySet(KeySet set, Key key) {
        KeySet safeCopy = set.dup();
        KeySet extractedKS = KeySet.create();
        for (Key current : safeCopy) {
            if (current.getName().startsWith(key.getName())) {
                extractedKS.append(current);
            }
        }
        return extractedKS;
    }

    private String findNewRoot(KeySet set, Key key, Randomizer randomizer) {
        KeySet filtered = set.dup();
        removeSection(filtered, key);   //This should avoid reallocating yourself to yourself or a subkey
        int keySetLength = filtered.length();
        Key relocatorKey = filtered.at(randomizer.getNextInt(keySetLength));
        String name = relocatorKey.getName();
        name = name.replace(ROOT_KEY.getName()+"/", "");
        String[] splitString = name.split("/");
        int pathLength = splitString.length+1;
        int random = randomizer.getNextInt(pathLength);
        String[] newSplitString = IntStream.range(0, random)
                .mapToObj(i -> splitString[i])
                .toArray(String[]::new);
        String newRoot = ROOT_KEY.getName();
        if (newSplitString.length > 0) {
            newRoot+= "/" + String.join("/", newSplitString);
        }
        return newRoot;
    }

    public static enum Metadata {
        SECTION_REMOVE("inject/structure/sectionRemove"),
        SECTION_REALLOCATE("inject/structure/sectionReallocate");

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
