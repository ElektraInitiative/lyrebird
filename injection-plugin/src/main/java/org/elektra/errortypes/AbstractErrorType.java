package org.elektra.errortypes;

import org.elektra.InjectionPlugin;
import org.libelektra.Key;
import org.libelektra.util.RandomizerSingelton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static org.elektra.InjectionPlugin.getSeedFromMeta;
import static org.elektra.InjectionPlugin.hasSeedSet;

public abstract class AbstractErrorType {

    protected Key removeAffectingMeta(Key key, String... metadata) {
        for (String singleMetadata : metadata) {
            key = key.removeMetaIfPresent(singleMetadata);
        }
        key = key.removeMetaIfPresent(InjectionPlugin.SEED_META);
        return key;
    }

    protected Key removeAffectingMetaArray(Key key, String metadata) {
        int currentArrayValue = 0;
        while (Objects.nonNull(
                key.getMeta(metadata + "/#" + String.valueOf(currentArrayValue)).getName())) {
            key = key.removeMetaIfPresent(metadata+"/#"+String.valueOf(currentArrayValue));
            currentArrayValue++;
        }
        key = key.removeMetaIfPresent(InjectionPlugin.SEED_META);
        return key;
    }

    protected RandomizerSingelton.Randomizer getRandomizer() {
        return RandomizerSingelton.getInstance();
    }

    protected RandomizerSingelton.Randomizer getRandomizer(Key key) {
        RandomizerSingelton.Randomizer randomizer = RandomizerSingelton.getInstance();
        if (hasSeedSet(key)) {
            randomizer.setSeed(getSeedFromMeta(key));
        }
        return randomizer;
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
