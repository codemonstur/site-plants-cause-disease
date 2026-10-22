package app.util;

import app.model.Disease;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FunctionsTest {

    @Test
    void isNullOrEmpty() {
        assertTrue(Functions.isNullOrEmpty(null));
        assertTrue(Functions.isNullOrEmpty(""));
        assertFalse(Functions.isNullOrEmpty("x"));
    }

    @Test
    void orEmpty() {
        assertTrue(Functions.orEmpty(null).isEmpty());
        assertEquals(2, Functions.orEmpty(java.util.List.of("a", "b")).size());
    }

    @Test
    void toSourceName() throws Exception {
        assertEquals("Wikipedia", Disease.toSourceName("https://en.wikipedia.org/wiki/Bloating"));
        assertNull(Disease.toSourceName("https://example.com/x"));
    }
}
