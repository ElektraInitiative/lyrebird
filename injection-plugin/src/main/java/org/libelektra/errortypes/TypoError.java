package org.libelektra.errortypes;

import org.libelektra.InjectionMeta;
import org.libelektra.KDB;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.libelektra.service.KDBService;
import org.libelektra.service.RandomizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class TypoError extends AbstractErrorType {

    private final static Logger LOG = LoggerFactory.getLogger(TypoError.class);
    private final static char[] availableInsertionCharacters =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
    private final KDBService kdbService;
    public static int TYPE_ID = 4;

    @Autowired
    public TypoError(RandomizerService randomizerService, KDBService kdbService) {
        super(randomizerService);
        this.kdbService = kdbService;
    }

    public KeySet applyTypoError(KeySet set, String injectPath, String defaultValue, InjectionMeta injectionType) throws KDB.KDBException {
        
        if (injectionType.equals(Metadata.TYPO_TRANSPOSITION)) {
            return transposition(set, injectPath, defaultValue);
        } else if (injectionType.equals(Metadata.TYPO_CHANGE_CHAR)) {
            return changeChar(set, injectPath, defaultValue);
        } else if (injectionType.equals(Metadata.TYPO_DELETION)) {
            return deletion(set, injectPath, defaultValue);
        } else if (injectionType.equals(Metadata.TYPO_INSERTION)) {
            return insertion(set, injectPath, defaultValue);
        } else if (injectionType.equals(Metadata.TYPO_SPACE)) {
            return space(set, injectPath, defaultValue);
        } else if (injectionType.equals(Metadata.TYPO_TOGGLE)) {
            return caseToggle(set, injectPath, defaultValue);
        }
        
        return set;
    }

    @Override
    public int getInjectionInt() {
        return TYPE_ID;
    }

    @Override
    public List<InjectionMeta> getBelongingMetadatas() {
        return Arrays.asList(Metadata.values());
    }

    private KeySet transposition(KeySet set, String injectPath, String defaultValue) throws KDB.KDBException {
        LOG.debug("Applying Typo Error [transposition] to {}", injectPath);
        Key keyToChange = getValueOrDefault(set, injectPath, defaultValue);
        String value = keyToChange.getString();
        if (value.length() < 2) {
            LOG.warn("Cannot transpose a single character");
            return set;
        }

        int charPosition = randomizerService.getNextInt(value.length());
        int otherPosition = randomizerService.getNextInt(value.length());

        //search for another position. Single character strings are excluded already
        while (otherPosition == charPosition) {
            otherPosition = randomizerService.getNextInt(value.length());
        }

        StringBuilder sb = new StringBuilder(value);
        char char1 = value.charAt(charPosition);
        char char2 = value.charAt(otherPosition);
        sb.setCharAt(charPosition, char2);
        sb.setCharAt(otherPosition, char1);
        String newValue = sb.toString();
        keyToChange.setString(newValue);

        String message = String.format("Changing [%s ===> %s] on %s",
                value, newValue, keyToChange.getName());
        LOG.debug("{}", message);
        set.append(keyToChange);
        return set;
    }

    private KeySet insertion(KeySet set, String injectPath, String defaultValue) {
        LOG.debug("Applying Typo Error [insertion] to {}", injectPath);
        Key keyToChange = getValueOrDefault(set, injectPath, defaultValue);
        String value = keyToChange.getString();

        char randomChar = availableInsertionCharacters[randomizerService.getNextInt(availableInsertionCharacters.length)];
        int position = randomizerService.getNextInt(value.length()+1);

        String newString = new StringBuilder(value).insert(position, randomChar).toString();

        String message = String.format("Inserted [%s ===> %s] on %s",
                value, newString, keyToChange.getName());
        LOG.debug(message);

        keyToChange.setString(newString);
        set.append(keyToChange);
        return set;
    }

    private KeySet deletion(KeySet set, String injectPath, String defaultValue) {
        LOG.debug("Applying Typo Error [deletion] to {}", injectPath);
        Key keyToChange = getValueOrDefault(set, injectPath, defaultValue);
        String value = keyToChange.getString();

        String newString = new StringBuilder(value)
                .deleteCharAt(randomizerService.getNextInt(value.length()))
                .toString();

        String message = String.format("Deletion [%s ===> %s] on %s",
                value, newString, keyToChange.getName());
        LOG.debug(message);

        keyToChange.setString(newString);
        set.append(keyToChange);
        return set;
    }

    private KeySet changeChar(KeySet set, String injectPath, String defaultValue) {
        LOG.debug("Applying Typo Error [changeChar] to {}", injectPath);
        Key keyToChange = getValueOrDefault(set, injectPath, defaultValue);
        String value = keyToChange.getString();

        char randomChar = availableInsertionCharacters[randomizerService.getNextInt(availableInsertionCharacters.length)];
        int position = randomizerService.getNextInt(value.length());

        StringBuilder sb = new StringBuilder(value);
        sb.setCharAt(position, randomChar);

        String newString = sb.toString();
        String message = String.format("Changing Char [%s ===> %s] on %s",
                value, newString, keyToChange.getName());
        LOG.debug(message);

        keyToChange.setString(newString);
        set.append(keyToChange);
        return set;
    }

    private KeySet space(KeySet set, String injectPath, String defaultValue) {
        LOG.debug("Applying Typo Error [space] to {}", injectPath);
        Key keyToChange = getValueOrDefault(set, injectPath, defaultValue);
        String value = keyToChange.getString();

        // +1 because we want at least one space
        int precedingSpaces = randomizerService.getNextInt(5)+1;
        int trailingSpaces = randomizerService.getNextInt(5)+1;

        StringBuilder sb = new StringBuilder(value);
        for (int i = 0; i < precedingSpaces; i++) {
            sb.insert(0, " ");
        }
        for (int i = 0; i < trailingSpaces; i++) {
            sb.append(" ");
        }

        String newString = sb.toString();
        String message = String.format("Inserting space [%s ===> '%s'] on %s",
                value, newString, keyToChange.getName());
        LOG.debug(message);

        keyToChange.setString(newString);
        set.append(keyToChange);
        return set;
    }

    private KeySet caseToggle(KeySet set, String injectPath, String defaultValue) {
        LOG.debug("Applying Typo Error [caseToggle] to {}", injectPath);
        Key keyToChange = getValueOrDefault(set, injectPath, defaultValue);
        String value = keyToChange.getString();

        if (!value.matches(".*[a-zA-Z]+.*")) {
            LOG.warn("Cannot toggle non-alphabetical characters");
            return set;
        }

        int currentPos = 0;
        char[] valueAsArray = value.toCharArray();
        int iterations = randomizerService.getNextInt(value.length());
        while (true) {
            if (Character.isAlphabetic(valueAsArray[currentPos]) && iterations <= 0) {
                char c = valueAsArray[currentPos];
                if (Character.isUpperCase(c)) {
                    c = Character.toLowerCase(c);
                } else if (Character.isLowerCase(c)) {
                    c = Character.toUpperCase(c);
                }
                valueAsArray[currentPos] = c;
                break;
            }
            if (Character.isAlphabetic(valueAsArray[currentPos])){
                iterations--;
            }
            currentPos = (currentPos + 1) % valueAsArray.length;
        }

        String newString = String.valueOf(valueAsArray);
        keyToChange.setString(newString);

        String message = String.format("Toggle Case [%s ===> %s] on %s",
                value, newString, keyToChange.getName());
        LOG.debug("{}", message);
        set.append(keyToChange);
        return set;
    }

    public static enum Metadata implements InjectionMeta {
        TYPO_TRANSPOSITION("inject/typo/transposition"),
        TYPO_INSERTION("inject/typo/insertion"),
        TYPO_DELETION("inject/typo/deletion"),
        TYPO_CHANGE_CHAR("inject/typo/change/char"),
        TYPO_SPACE("inject/typo/space"),
        TYPO_TOGGLE("inject/typo/case/toggle");

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

    private Key getValueOrDefault(KeySet set, String injectPath, String defaultValue) {
        Key lookup = set.lookup(injectPath);
        if (lookup.isNull()) {
            return Key.create(injectPath, defaultValue);
        }
        return lookup;
    }
}
