package org.libelektra;

import org.libelektra.errortypes.*;
import org.libelektra.service.KDBService;
import org.libelektra.service.RandomizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private final RandomizerService randomizerService;
    private final KDBService kdbService;
    private final String configMountPoint;

    @Autowired
    public InjectionPlugin(StructureError structureError,
                           TypoError typoError,
                           SemanticError semanticError,
                           ResourceError resourceError,
                           DomainError domainError,
                           LimitError limitError,
                           RandomizerService randomizerService,
                           KDBService kdbService,
                           @Value("${config.mountpoint}") String configMountPoint) {
        this.structureError = structureError;
        this.typoError = typoError;
        this.semanticError = semanticError;
        this.resourceError = resourceError;
        this.domainError = domainError;
        this.limitError = limitError;
        this.randomizerService = randomizerService;
        this.kdbService = kdbService;
        this.configMountPoint = configMountPoint;
    }

    public int kdbSet(KeySet keySet, Key injectKey, String path) {
        keySet.rewind();

        String defaultValue = injectKey.getMeta("default").getString();
        InjectionData injectionData = new InjectionData(keySet, injectKey, defaultValue, path,
                null);

        List<AbstractErrorType> allErrors = new ArrayList<>(getAllPossibleInjections(injectKey));
        AbstractErrorType toInject = allErrors.get(randomizerService.getNextInt(allErrors.size()));
        List<InjectionMeta> availableMetadatas = toInject.getBelongingMetadatas();
        InjectionMeta injectionConcrete =
                availableMetadatas.get(randomizerService.getNextInt(availableMetadatas.size()));

        injectionData.setInjectionType(injectionConcrete);

        try {
            keySet = toInject.apply(injectionData);
            kdbService.set(keySet, configMountPoint);
        } catch (KDB.KDBException e) {
            LOG.warn("Could not inject: Type {}, Injection: {}", toInject.getClass().getSimpleName(),
                    injectionConcrete.getMetadata(), e);
        }

        return 0;
    }

    public String getName() {
        return name;
    }

    Collection<AbstractErrorType> getAllPossibleInjections(Key injectKey) {
        Collection<AbstractErrorType> injectionMetaCollection = new ArrayList<>();
        injectionMetaCollection.add(structureError);
        injectionMetaCollection.add(typoError);
        Key types = injectKey.getMeta("types");
        if (types.isNull()) {
            return injectionMetaCollection;
        }
        List<Integer> allTypesAsIntegers = Arrays.stream(types.getString().trim().split(","))
                .map(String::trim)
                .map(Integer::valueOf)
                .collect(Collectors.toList());

        if (allTypesAsIntegers.contains(semanticError.getInjectionInt())) {
            if (hasSemanticMetadata(injectKey)) {
                injectionMetaCollection.add(semanticError);
            } else {
                LOG.warn("No {} metadata found for key {} despite given int", "semantic", injectKey.getName());
            }
        }
        if (allTypesAsIntegers.contains(resourceError.getInjectionInt())) {
            if (hasResourceMetadata(injectKey)) {
                injectionMetaCollection.add(resourceError);
            } else {
                LOG.warn("No {} metadata found for key {} despite given int", "resource", injectKey.getName());
            }
        }
        if (allTypesAsIntegers.contains(domainError.getInjectionInt())) {
            if (hasDomaincMetadata(injectKey)) {
                injectionMetaCollection.add(domainError);
            } else {
                LOG.warn("No {} metadata found for key {} despite given int", "domain", injectKey.getName());
            }
        }
        if (allTypesAsIntegers.contains(limitError.getInjectionInt())) {
            if (hasLimitMetadata(injectKey)) {
                injectionMetaCollection.add(limitError);
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
