package org.libelektra.util;

import java.util.Random;

public class RandomizerSingelton {

    private static Randomizer instance;

    private static Randomizer getInstance() {
        if (RandomizerSingelton.instance == null) {
            RandomizerSingelton.instance = new Randomizer();
        }
        return RandomizerSingelton.instance;
    }

    private static Randomizer getInstance(int seed) {
        if (RandomizerSingelton.instance == null) {
            RandomizerSingelton.instance = new Randomizer(seed);
        }
        return RandomizerSingelton.instance;
    }

    private RandomizerSingelton() {
    }


    private static class Randomizer {
        private final Random randomizer;

        public Randomizer(int seed) {
            randomizer = new Random(seed);
        }

        public Randomizer() {
            randomizer = new Random();
        }

        public int getNextInt() {
            return randomizer.nextInt() & Integer.MAX_VALUE;
        }

        public int getNextInt(int bound) {
            return randomizer.nextInt(bound);
        }

        public void setSeed(int seed) {
            randomizer.setSeed(seed);
        }
    }

}
