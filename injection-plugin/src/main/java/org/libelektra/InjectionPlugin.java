package org.libelektra;

import org.libelektra.errortypes.*;
import org.libelektra.service.KDBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static java.util.Objects.nonNull;

public class InjectionPlugin {

    public static final String SEED_META ="inject/rand/seed";

    private final static Logger LOG = LoggerFactory.getLogger(InjectionPlugin.class);

    private static String name = "injection";
    public static Key ROOT_KEY = Key.create("user/injection/test");

    private final StructureError structureError = new StructureError();
    private final TypoError typoError = new TypoError();
    private final SemanticError semanticError = new SemanticError();
    private final ResourceError resourceError = new ResourceError();
    private final DomainError domainError = new DomainError();
    private final LimitError limitError = new LimitError();

    private final KDBService kdbService;

    public InjectionPlugin(KDBService kdbService) {

        this.kdbService = kdbService;
    }

    public int kdbSet(KeySet keySet, Key injectKey, String path) {
        keySet.rewind();



        if (hasStructureMetadata(injectKey)) {
            keySet = structureError.applyStructureError(keySet, injectKey);
        } else if (hasTypoMetadata(injectKey)) {
            keySet = typoError.applyTypoError(keySet, injectKey);
        } else if (hasSemanticMetadata(injectKey)) {
            keySet = semanticError.applySemanticError(keySet, injectKey);
        } else if (hasResourceMetadata(injectKey)) {
            keySet = resourceError.applyResourceError(keySet, injectKey);
        } else if (hasDomaincMetadata(injectKey)) {
            keySet = domainError.applyDomainError(keySet, injectKey);
        } else if (hasLimitMetadata(injectKey)) {
            keySet = limitError.applyLimitError(keySet, injectKey);
        }


        try {
            kdbService.set(keySet, ROOT_KEY);
        } catch (KDB.KDBException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public String getName() {
        return name;
    }

    Collection<InjectionMeta> getAllPossibleInjections(Key injectKey) {
        Collection<InjectionMeta> injectionMetaCollection = new ArrayList<>();
        injectionMetaCollection.addAll(Arrays.asList(StructureError.Metadata.values()));
        injectionMetaCollection.addAll(Arrays.asList(TypoError.Metadata.values()));
        Key types = injectKey.getMeta("types");
        if (types.isNull()) {
            return injectionMetaCollection;
        }

        if (hasSemanticMetadata(injectKey)) {
            injectionMetaCollection.addAll(Arrays.asList(SemanticError.Metadata.values()));
        }
        if (hasResourceMetadata(injectKey)) {
            injectionMetaCollection.addAll(Arrays.asList(ResourceError.Metadata.values()));
        }
        if (hasDomaincMetadata(injectKey)) {
            injectionMetaCollection.addAll(Arrays.asList(DomainError.Metadata.values()));
        }
        if (hasLimitMetadata(injectKey)) {
            injectionMetaCollection.addAll(Arrays.asList(LimitError.Metadata.values()));
        }
        return injectionMetaCollection;
    }

    private boolean hasLimitMetadata(Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (LimitError.Metadata.hasMetadata(currentKey.getName())) {
                return true;
            }
            currentKey = key.nextMeta();
        }
        return false;
    }

    private boolean hasStructureMetadata(Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (StructureError.Metadata.hasMetadata(currentKey.getName())) {
                return true;
            }
            currentKey = key.nextMeta();
        }
        return false;
    }

    private boolean hasTypoMetadata(Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (TypoError.Metadata.hasMetadata(currentKey.getName())) {
                return true;
            }
            currentKey = key.nextMeta();
        }
        return false;
    }

    private boolean hasResourceMetadata(Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (ResourceError.Metadata.hasMetadata(currentKey.getName())) {
                return true;
            }
            currentKey = key.nextMeta();
        }
        return false;
    }

    private boolean hasSemanticMetadata(Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (SemanticError.Metadata.hasMetadata(currentKey.getName())) {
                return true;
            }
            currentKey = key.nextMeta();
        }
        return false;
    }

    private boolean hasDomaincMetadata(Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (DomainError.Metadata.hasMetadata(currentKey.getName())) {
                return true;
            }
            currentKey = key.nextMeta();
        }
        return false;
    }

    public static boolean hasSeedSet(Key key) {
        return nonNull(key.getMeta(SEED_META).getName());
    }

    public static int getSeedFromMeta(Key key) {
        if (hasSeedSet(key)) {
            return key.getMeta(SEED_META).getInteger();
        }
        return 0;
    }

}
