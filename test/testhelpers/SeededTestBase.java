package testhelpers;

import org.junit.jupiter.api.BeforeEach;

import general.HeuristicClasses.Heuristic;

/**
 * Sets a fixed random seed before each test so stochastic code is reproducible.
 */
public abstract class SeededTestBase {
    protected static final long TEST_SEED = 42L;

    @BeforeEach
    void seedRandom() {
        Heuristic.setRandSeed(TEST_SEED);
    }
}
