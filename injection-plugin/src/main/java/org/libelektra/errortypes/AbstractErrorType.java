package org.libelektra.errortypes;

import org.libelektra.InjectionMeta;
import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.model.InjectionData;
import org.libelektra.model.InjectionDataResult;
import org.libelektra.service.RandomizerService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

public abstract class AbstractErrorType {

    protected RandomizerService randomizerService;
    protected InjectionDataResult injectionDataResult;
    protected int TYPE_ID;

    public abstract int getInjectionInt();

    public boolean canBeApplied(List<Integer> injectionInts) {
        return injectionInts.contains(TYPE_ID);
    }

    public abstract List<InjectionMeta> getBelongingMetadatas();

    public KeySet apply(InjectionData injectionData) throws KDB.KDBException {
        KeySet result = doInject(injectionData);
        if (injectionDataResult != null) {
            injectionDataResult.logInjection();
        }
        return result;
    }

    public InjectionDataResult getInjectionDataResult() {
        if (injectionDataResult == null) {
            return new InjectionDataResult.Builder(false).build();
        }
        return injectionDataResult;
    }

    abstract KeySet doInject(InjectionData injectionData) throws KDB.KDBException;

    public AbstractErrorType(RandomizerService randomizerService) {
        this.randomizerService = randomizerService;
    }

    protected List<String> extractMetaDataArray(Key key, String startsWith) {
        List<String> allMetaArrayValues = new ArrayList<>();
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (currentKey.getName().startsWith(startsWith)) {
                allMetaArrayValues.add(currentKey.getString());
            }
            currentKey = key.nextMeta();
        }
        return allMetaArrayValues;
    }
}
