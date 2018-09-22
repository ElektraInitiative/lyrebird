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
            } else if (currentKey.getName().equals(Metadata.SECTION_DUPLICATE.getMetadata())) {
                return duplicateSection(set, key);
            }
            currentKey = key.nextMeta();
        }
        return set;
    }

    private KeySet removeSection(KeySet set, Key startKey) {
        LOG.debug("Applying Structure Error [removeSection] to {}", startKey.getName());
        removeSectionNoLogging(set, startKey);
        LOG.debug("Removing: {} ===> REMOVE", startKey.getName());
        return set;
    }

    private void removeSectionNoLogging(KeySet set, Key startKey) {
        Iterator<Key> iterator = set.iterator();
        while (iterator.hasNext()) {
            Key current = iterator.next();
            if (current.getName().startsWith(startKey.getName())) {
                iterator.remove();
            }
        }
    }

    //What a disgusting method ... but elektra forces me to do it
    private KeySet reallocateSection(KeySet set, Key key) {
        LOG.debug("Applying Structure Error [reallocateSection] to {}", key.getName());

        //Remove the metadata to prohibit multiple reallocation
        key = key.removeMetaIfPresent(Metadata.SECTION_REALLOCATE.getMetadata());
        key = key.removeMetaIfPresent(InjectionPlugin.SEED_META);
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
        KeySet extractedKSwithNewRoot = changePathOfKeySet(extractedKS, toSubstitute, newRoot);

        String message = String.format("Reallocate %s ===> %s", key.getName(), newRoot);
        LOG.debug(message);
        removeSectionNoLogging(set, key);
        set.append(extractedKSwithNewRoot);

        return set;
    }

    private KeySet duplicateSection(KeySet set, Key key) {
        LOG.debug("Applying Structure Error [duplicateSection] to {}", key.getName());

        //Remove the metadata to prohibit multiple reallocation
        key = key.removeMetaIfPresent(Metadata.SECTION_REALLOCATE.getMetadata());
        key = key.removeMetaIfPresent(InjectionPlugin.SEED_META);
        set.append(key);

        Randomizer randomizer = RandomizerSingelton.getInstance();
        if (hasSeedSet(key)) {
            randomizer.setSeed(getSeedFromMeta(key));
        }
        String newRoot = findNewRoot(set, key, randomizer);
        KeySet extractedKS = extractKeySet(set, key);

        int slashLocation=key.getName().lastIndexOf("/");
        String toSubstitute=key.getName().substring(0, slashLocation);

        //Change the path of every Key and add it to a new KS
        KeySet extractedKSwithNewRoot = changePathOfKeySet(extractedKS, toSubstitute, newRoot);
        set.append(extractedKSwithNewRoot);

        String message = String.format("Duplicate %s ===> %s", key.getName(), newRoot);
        LOG.debug(message);

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
        removeSectionNoLogging(filtered, key);   //This should avoid reallocating yourself to yourself or a subkey
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

    private KeySet changePathOfKeySet(KeySet set, String oldPathPart, String newPathPart) {
        KeySet extractedKSwithNewRoot = KeySet.create();
        for (Key current : set) {
            String newPath = current.getName().replace(oldPathPart, newPathPart);
            Key tmpKey = Key.create(newPath, current.getString());

            //Copy all metadata to new Key
            current.rewindMeta();
            Key currentMetaKey = current.currentMeta();
            while (nonNull(currentMetaKey.getName())) {
                tmpKey.setMeta(currentMetaKey.getName(), currentMetaKey.getString());
                currentMetaKey = current.nextMeta();
            }

            extractedKSwithNewRoot.append(tmpKey);
        }
        return extractedKSwithNewRoot;
    }

    public static enum Metadata {
        SECTION_REMOVE("inject/structure/section/remove"),
        SECTION_REALLOCATE("inject/structure/section/reallocate"),
        SECTION_DUPLICATE("inject/structure/section/duplicate");

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
