package org.libelektra;

import org.libelektra.errortypes.*;
import org.libelektra.service.KDBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Component
public class InjectionPlugin {

    private final static Logger LOG = LoggerFactory.getLogger(InjectionPlugin.class);

    private static String name = "injection";
    public static Key ROOT_KEY = Key.create("user/injection/test");

    private final StructureError structureError;
    private final TypoError typoError;
    private final SemanticError semanticError;
    private final ResourceError resourceError;
    private final DomainError domainError;
    private final LimitError limitError;

    private final KDBService kdbService;

    @Autowired
    public InjectionPlugin(StructureError structureError,
                           TypoError typoError,
                           SemanticError semanticError,
                           ResourceError resourceError,
                           DomainError domainError,
                           LimitError limitError,
                           KDBService kdbService) {
        this.structureError = structureError;
        this.typoError = typoError;
        this.semanticError = semanticError;
        this.resourceError = resourceError;
        this.domainError = domainError;
        this.limitError = limitError;

        this.kdbService = kdbService;
    }

    public int kdbSet(KeySet keySet, Key injectKey, String path) {
        keySet.rewind();

        InjectionData injectionData = new InjectionData(keySet, injectKey, null, path,
                SemanticError.Metadata.SEMANTIC_ERROR);

        try {
            if (hasStructureMetadata(injectKey)) {
                injectionData.setInjectionType(StructureError.Metadata.SECTION_REMOVE);
                keySet = structureError.apply(injectionData);
            } else if (hasTypoMetadata(injectKey)) {
                String defaultValue = injectKey.getMeta("default").getString();
                TypoError.Metadata injectionType = TypoError.Metadata.TYPO_CHANGE_CHAR;
                injectionData.setDefaultValue(defaultValue);
                injectionData.setInjectionType(injectionType);
                keySet = typoError.apply(injectionData);
            } else if (hasSemanticMetadata(injectKey)) {
                keySet = semanticError.applySemanticError(injectionData);
            } else if (hasResourceMetadata(injectKey)) {
                injectionData.setInjectionType(ResourceError.Metadata.RESOURCE_ERROR);
                keySet = resourceError.apply(injectionData);
            } else if (hasDomaincMetadata(injectKey)) {
                injectionData.setInjectionType(DomainError.Metadata.DOMAIN_ERROR);
                keySet = domainError.apply(injectionData);
            } else if (hasLimitMetadata(injectKey)) {
                injectionData.setInjectionType(LimitError.Metadata.LIMIT_ERROR_MIN);
                keySet = limitError.apply(injectionData);
            }

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
        List<Integer> allTypesAsIntegers = Arrays.stream(types.getString().trim().split(","))
                .map(String::trim)
                .map(Integer::valueOf)
                .collect(Collectors.toList());

        if (allTypesAsIntegers.contains(SemanticError.TYPE_ID)) {
            if (hasSemanticMetadata(injectKey)) {
                injectionMetaCollection.addAll(Arrays.asList(SemanticError.Metadata.values()));
            } else {
                LOG.warn("No {} metadata found for key {} despite given int", "semantic", injectKey.getName());
            }
        }
        if (allTypesAsIntegers.contains(ResourceError.TYPE_ID)) {
            if (hasResourceMetadata(injectKey)) {
                injectionMetaCollection.addAll(Arrays.asList(ResourceError.Metadata.values()));
            } else {
                LOG.warn("No {} metadata found for key {} despite given int", "resource", injectKey.getName());
            }
        }
        if (allTypesAsIntegers.contains(DomainError.TYPE_ID)) {
            if (hasDomaincMetadata(injectKey)) {
                injectionMetaCollection.addAll(Arrays.asList(DomainError.Metadata.values()));
            } else {
                LOG.warn("No {} metadata found for key {} despite given int", "domain", injectKey.getName());
            }
        }
        if (allTypesAsIntegers.contains(LimitError.TYPE_ID)) {
            if (hasLimitMetadata(injectKey)) {
                injectionMetaCollection.addAll(Arrays.asList(LimitError.Metadata.values()));
            } else {
                LOG.warn("No {} metadata found for key {} despite given int", "limit", injectKey.getName());
            }
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

}
