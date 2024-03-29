package org.libelektra.errortypes;

import org.libelektra.InjectionMeta;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.model.InjectionData;
import org.libelektra.model.InjectionDataResult;
import org.libelektra.service.RandomizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.Objects.nonNull;

@Component
@Profile({"structure", "all"})
public class StructureError extends AbstractErrorType{

    private final static Logger LOG = LoggerFactory.getLogger(StructureError.class);
    private final String parentPath;

    @Autowired
    public StructureError(
            @Value("${mountpoint.config}") String parentPath,
            RandomizerService randomizerService) {
        super(randomizerService);
        this.parentPath = parentPath;
        this.TYPE_ID = 1;
    }

    @Override
    public KeySet doInject(InjectionData injectionData) {
        if (injectionData.getInjectionType().equals(Metadata.SECTION_REMOVE)) {
            return removeSection(injectionData.getSet(), injectionData.getInjectPath());
        } else if (injectionData.getInjectionType().equals(Metadata.SECTION_REALLOCATE)) {
            return reallocateSection(injectionData.getSet(), injectionData.getInjectPath());
        } else if (injectionData.getInjectionType().equals(Metadata.SECTION_DUPLICATE)) {
            return duplicateSection(injectionData.getSet(), injectionData.getInjectPath());
        }
        return injectionData.getSet();
    }

    @Override
    public int getInjectionInt() {
        return TYPE_ID;
    }

    @Override
    public boolean canBeApplied(List<Integer> injectionInt) {
        return true;
    }

    @Override
    public List<InjectionMeta> getBelongingMetadatas() {
        return Arrays.asList(Metadata.values());
    }

    private KeySet removeSection(KeySet set, String path) {
        LOG.debug("Applying Structure Error [removeSection] to {}", path);
        removeSectionNoLogging(set, path);
        this.injectionDataResult = new InjectionDataResult.Builder(true)
                .withOldValue(path)
                .withNewValue("null")
                .withKey(path)
                .withInjectionMeta(Metadata.SECTION_REMOVE)
                .build();
        return set;
    }

    private void removeSectionNoLogging(KeySet set, String path) {
        Iterator<Key> iterator = set.iterator();
        while (iterator.hasNext()) {
            Key current = iterator.next();
            if (current.getName().startsWith(path)) {
                iterator.remove();
            }
        }
    }

    //What a disgusting method ... but libelektra forces me to do it
    private KeySet reallocateSection(KeySet set, String path) {
        LOG.debug("Applying Structure Error [reallocateSection] to {}", path);

        String newRoot = findNewRoot(set, path);
        KeySet extractedKS = extractKeySet(set, path);

        //We only want to substitute until the last '/'
        int slashLocation=path.lastIndexOf("/");
        String toSubstitute=path.substring(0, slashLocation);

        //Change the path of every Key and add it to a new KS
        KeySet extractedKSwithNewRoot = changePathOfKeySet(extractedKS, toSubstitute, newRoot);

        this.injectionDataResult = new InjectionDataResult.Builder(true)
                .withOldValue(path)
                .withNewValue(newRoot)
                .withKey(path)
                .withInjectionMeta(Metadata.SECTION_REALLOCATE)
                .build();
        removeSectionNoLogging(set, path);
        set.append(extractedKSwithNewRoot);

        return set;
    }

    private KeySet duplicateSection(KeySet set, String path) {
        LOG.debug("Applying Structure Error [duplicateSection] to {}", path);

        String newRoot = findNewRoot(set, path);
        KeySet extractedKS = extractKeySet(set, path);

        int slashLocation=path.lastIndexOf("/");
        String toSubstitute=path.substring(0, slashLocation);

        //Change the path of every Key and add it to a new KS
        KeySet extractedKSwithNewRoot = changePathOfKeySet(extractedKS, toSubstitute, newRoot);
        set.append(extractedKSwithNewRoot);
        this.injectionDataResult = new InjectionDataResult.Builder(true)
                .withOldValue(path)
                .withNewValue(newRoot)
                .withKey(path)
                .withInjectionMeta(Metadata.SECTION_DUPLICATE)
                .build();

        return set;
    }

    private KeySet extractKeySet(KeySet set, String path) {
        KeySet safeCopy = set.dup();
        KeySet extractedKS = KeySet.create();
        for (Key current : safeCopy) {
            if (current.getName().startsWith(path)) {
                extractedKS.append(current);
            }
        }
        return extractedKS;
    }

    private String findNewRoot(KeySet set, String path) {
        KeySet filtered = set.dup();
        removeSectionNoLogging(filtered, path); //This should avoid reallocating yourself to yourself or a subkey
        int keySetLength = filtered.length();
        Key relocatorKey = filtered.at(randomizerService.getNextInt(keySetLength));
        String name = relocatorKey.getName();
        name = name.replace(parentPath+"/", "");
        String[] splitString = name.split("/");
        int pathLength = splitString.length+1;
        int random = randomizerService.getNextInt(pathLength);
        String[] newSplitString = IntStream.range(0, random)
                .mapToObj(i -> splitString[i])
                .toArray(String[]::new);
        String newRoot = parentPath;
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

    public static enum Metadata implements InjectionMeta {
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

        @Override
        public String getCategory() {
            return "Structure Error";
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
