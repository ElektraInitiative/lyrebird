package org.elektra.errortypes;

import org.elektra.InjectionPlugin;
import org.libelektra.Key;
import org.libelektra.util.RandomizerSingelton;

import static org.elektra.InjectionPlugin.getSeedFromMeta;
import static org.elektra.InjectionPlugin.hasSeedSet;

public abstract class AbstractErrorType {

    protected Key removeAffectingMeta(Key key, String metadata) {
        key = key.removeMetaIfPresent(metadata);
        key = key.removeMetaIfPresent(InjectionPlugin.SEED_META);
        return key;
    }

    protected RandomizerSingelton.Randomizer getRandomizer(Key key) {
        RandomizerSingelton.Randomizer randomizer = RandomizerSingelton.getInstance();
        if (hasSeedSet(key)) {
            randomizer.setSeed(getSeedFromMeta(key));
        }
        return randomizer;
    }
}
