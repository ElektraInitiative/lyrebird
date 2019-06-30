package org.libelektra;

import org.libelektra.errortypes.*;
import org.libelektra.model.InjectionData;
import org.libelektra.model.InjectionDataResult;
import org.libelektra.service.KDBService;
import org.libelektra.service.RandomizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Component
public class InjectionPlugin {

    private final static Logger LOG = LoggerFactory.getLogger(InjectionPlugin.class);

    private static String name = "injection";
    public static Key ROOT_KEY = Key.create("user/injection/test");


    private final RandomizerService randomizerService;
    private final KDBService kdbService;
    private final String configMountPoint;
    private List<AbstractErrorType> errorTypes;

    @Autowired
    public InjectionPlugin(List<AbstractErrorType> errorTypes,
                           RandomizerService randomizerService,
                           KDBService kdbService,
                           @Value("${config.mountpoint}") String configMountPoint) {
        this.errorTypes = errorTypes;
        this.randomizerService = randomizerService;
        this.kdbService = kdbService;
        this.configMountPoint = configMountPoint;
    }

    public InjectionDataResult kdbSet(KeySet keySet, Key injectKey, String path) {
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

        return toInject.getInjectionDataResult();
    }

    public String getName() {
        return name;
    }

    Collection<AbstractErrorType> getAllPossibleInjections(Key injectKey) {
        Collection<AbstractErrorType> injectionMetaCollection = new ArrayList<>();
        Key types = injectKey.getMeta("types");
        List<Integer> allTypesAsIntegers = new ArrayList<>();
        if (!types.isNull()) {
            allTypesAsIntegers = Arrays.stream(types.getString().trim().split(","))
                    .map(String::trim)
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
        }

        List<Integer> finalAllTypesAsIntegers = allTypesAsIntegers;
        errorTypes.stream()
                .filter(errorType -> errorType.canBeApplied(finalAllTypesAsIntegers))
                .forEach(injectionMetaCollection::add);

        return injectionMetaCollection;
    }

}
