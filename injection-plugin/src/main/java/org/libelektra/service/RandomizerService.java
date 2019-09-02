package org.libelektra.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RandomizerService {

    private Random randomizer;
    private final static Logger LOG = LoggerFactory.getLogger(RandomizerService.class);

    private AtomicLong counter = new AtomicLong(0);

    public RandomizerService(@Value("${injection.seed}") Integer initialSeed) {
        randomizer = new Random(initialSeed);
    }

    public int getNextInt(int bound) {
        counter.incrementAndGet();
        return randomizer.nextInt(bound);
    }

    public void setSeed(int seed) {
        randomizer.setSeed(seed);
    }

    public int getNextInt() {
        return randomizer.nextInt() & Integer.MAX_VALUE;
    }

    @PostConstruct
    public void tearDown() {
        LOG.info("Called randomizerService {} times", counter.get());
    }
}
