package org.libelektra.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class RandomizerService {

    private Random randomizer;
    public static final String SEED_META ="inject/rand/seed";

    public RandomizerService(@Value("${injection.seed}") Integer initialSeed) {
        randomizer = new Random(initialSeed);
    }

    public int getNextInt(int bound) {
        return randomizer.nextInt(bound);
    }

    public void setSeed(int seed) {
        randomizer.setSeed(seed);
    }

    public int getNextInt() {
        return randomizer.nextInt() & Integer.MAX_VALUE;
    }


}
