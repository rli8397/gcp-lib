package general;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import testhelpers.TestGraphs;

class OptionsTest {

    @Test
    void defaultValuesAreSet() {
        Options options = new Options();
        assertEquals(10.0, options.runtime);
        assertEquals(0, options.verbosity);
        assertNotNull(options.extras);
    }

    @Test
    void toStringIncludesConfiguredFields() {
        Options options = new Options();
        options.instance = TestGraphs.path(2);
        options.seed = 7L;
        options.runtime = 5.0;
        options.verbosity = 2;

        String text = options.toString();
        assertTrue(text.contains("seed=7"));
        assertTrue(text.contains("runtime=5.0"));
        assertTrue(text.contains("versbosity =2"));
    }
}
