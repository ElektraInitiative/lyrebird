package org.libelektra.errortypes;

import org.libelektra.InjectionMeta;
import org.libelektra.Key;
import org.libelektra.KeySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.nonNull;
import static org.libelektra.util.RandomizerSingelton.Randomizer;

public class TypoError extends AbstractErrorType {

    private final static Logger LOG = LoggerFactory.getLogger(TypoError.class);
    private final static char[] availableInsertionCharacters =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();

    public KeySet applyTypoError(KeySet set, Key key) {
        key.rewindMeta();
        Key currentKey = key.currentMeta();
        while (nonNull(currentKey.getName())) {
            if (currentKey.getName().equals(Metadata.TYPO_TRANSPOSITION.getMetadata())) {
                return transposition(set, key);
            } else if (currentKey.getName().equals(Metadata.TYPO_CHANGE_CHAR.getMetadata())) {
                return changeChar(set, key);
            } else if (currentKey.getName().equals(Metadata.TYPO_DELETION.getMetadata())) {
                return deletion(set, key);
            } else if (currentKey.getName().equals(Metadata.TYPO_INSERTION.getMetadata())) {
                return insertion(set, key);
            } else if (currentKey.getName().equals(Metadata.TYPO_SPACE.getMetadata())) {
                return space(set, key);
            } else if (currentKey.getName().equals(Metadata.TYPO_TOGGLE.getMetadata())) {
                return caseToggle(set, key);
            }
            currentKey = key.nextMeta();
        }
        return set;
    }

    private KeySet transposition(KeySet set, Key key) {
        LOG.debug("Applying Typo Error [transposition] to {}", key.getName());
        String value = key.getString();
        if (value.length() < 2) {
            LOG.warn("Cannot transpose a single character");
            return set;
        }

        Randomizer randomizer = getRandomizer(key);
        key = removeAffectingMeta(key, Metadata.TYPO_TRANSPOSITION.getMetadata());
        set.append(key);

        int charPosition = randomizer.getNextInt(value.length());
        int otherPosition = randomizer.getNextInt(value.length());

        //search for another position. Single character strings are excluded already
        while (otherPosition == charPosition) {
            otherPosition = randomizer.getNextInt(value.length());
        }

        StringBuilder sb = new StringBuilder(value);
        char char1 = value.charAt(charPosition);
        char char2 = value.charAt(otherPosition);
        sb.setCharAt(charPosition, char2);
        sb.setCharAt(otherPosition, char1);
        String newValue = sb.toString();
        key.setString(newValue);

        String message = String.format("Changing [%s ===> %s] on %s",
                value, newValue, key.getName());
        LOG.debug("{}", message);

        return set;
    }

    private KeySet insertion(KeySet set, Key key) {
        LOG.debug("Applying Typo Error [insertion] to {}", key.getName());
        String value = key.getString();

        Randomizer randomizer = getRandomizer(key);
        key = removeAffectingMeta(key, Metadata.TYPO_INSERTION.getMetadata());
        set.append(key);

        char randomChar = availableInsertionCharacters[randomizer.getNextInt(availableInsertionCharacters.length)];
        int position = randomizer.getNextInt(value.length()+1);

        String newString = new StringBuilder(value).insert(position, randomChar).toString();

        String message = String.format("Inserted [%s ===> %s] on %s",
                value, newString, key.getName());
        LOG.debug(message);

        key.setString(newString);

        return set;
    }

    private KeySet deletion(KeySet set, Key key) {
        LOG.debug("Applying Typo Error [deletion] to {}", key.getName());
        String value = key.getString();

        Randomizer randomizer = getRandomizer(key);
        key = removeAffectingMeta(key, Metadata.TYPO_DELETION.getMetadata());
        set.append(key);

        String newString = new StringBuilder(value)
                .deleteCharAt(randomizer.getNextInt(value.length()))
                .toString();

        String message = String.format("Deletion [%s ===> %s] on %s",
                value, newString, key.getName());
        LOG.debug(message);

        key.setString(newString);

        return set;
    }

    private KeySet changeChar(KeySet set, Key key) {
        LOG.debug("Applying Typo Error [changeChar] to {}", key.getName());
        String value = key.getString();

        Randomizer randomizer = getRandomizer(key);
        key = removeAffectingMeta(key, Metadata.TYPO_CHANGE_CHAR.getMetadata());
        set.append(key);

        char randomChar = availableInsertionCharacters[randomizer.getNextInt(availableInsertionCharacters.length)];
        int position = randomizer.getNextInt(value.length());

        StringBuilder sb = new StringBuilder(value);
        sb.setCharAt(position, randomChar);

        String newString = sb.toString();
        String message = String.format("Changing Char [%s ===> %s] on %s",
                value, newString, key.getName());
        LOG.debug(message);

        key.setString(newString);
        return set;
    }

    private KeySet space(KeySet set, Key key) {
        LOG.debug("Applying Typo Error [space] to {}", key.getName());
        String value = key.getString();

        Randomizer randomizer = getRandomizer(key);
        key = removeAffectingMeta(key, Metadata.TYPO_SPACE.getMetadata());
        set.append(key);

        // +1 because we want at least one space
        int precedingSpaces = randomizer.getNextInt(5)+1;
        int trailingSpaces = randomizer.getNextInt(5)+1;

        StringBuilder sb = new StringBuilder(value);
        for (int i = 0; i < precedingSpaces; i++) {
            sb.insert(0, " ");
        }
        for (int i = 0; i < trailingSpaces; i++) {
            sb.append(" ");
        }

        String newString = sb.toString();
        String message = String.format("Inserting space [%s ===> '%s'] on %s",
                value, newString, key.getName());
        LOG.debug(message);

        key.setString(newString);

        return set;
    }

    private KeySet caseToggle(KeySet set, Key key) {
        LOG.debug("Applying Typo Error [caseToggle] to {}", key.getName());
        String value = key.getString();

        Randomizer randomizer = getRandomizer(key);
        key = removeAffectingMeta(key, Metadata.TYPO_TOGGLE.getMetadata());
        set.append(key);

        if (!value.matches(".*[a-zA-Z]+.*")) {
            LOG.warn("Cannot toggle non-alphabetical characters");
            return set;
        }

        int currentPos = 0;
        char[] valueAsArray = value.toCharArray();
        int iterations = randomizer.getNextInt(value.length());
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
        key.setString(newString);

        String message = String.format("Toggle Case [%s ===> %s] on %s",
                value, newString, key.getName());
        LOG.debug("{}", message);
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

}
