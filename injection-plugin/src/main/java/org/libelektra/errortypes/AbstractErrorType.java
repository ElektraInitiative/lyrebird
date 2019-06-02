package org.libelektra.errortypes;

import org.libelektra.Key;
import org.libelektra.service.RandomizerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;

public abstract class AbstractErrorType {

    protected RandomizerService randomizerService;

    public AbstractErrorType(RandomizerService randomizerService) {
        this.randomizerService = randomizerService;
    }

    protected Key removeAffectingMeta(Key key, String... metadata) {
        for (String singleMetadata : metadata) {
            key = key.removeMetaIfPresent(singleMetadata);
        }
        return key;
    }

    protected Key removeAffectingMetaArray(Key key, String metadata) {
        int currentArrayValue = 0;
        while (Objects.nonNull(
                key.getMeta(metadata + "/#" + String.valueOf(currentArrayValue)).getName())) {
            key = key.removeMetaIfPresent(metadata+"/#"+String.valueOf(currentArrayValue));
            currentArrayValue++;
        }
        return key;
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
