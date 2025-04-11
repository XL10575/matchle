import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CorpusTest {

    /**
     * Building an empty corpus via the builder triggers the code path
     * that calls the private constructor with Set.of() and wordSize=0.
     */
    @Test
    public void testBuildEmptyCorpus() {
        Corpus c = Corpus.Builder.EMPTY.build();
        assertNotNull("Corpus should not be null even if built empty", c);
        assertEquals("Size of empty corpus should be 0", 0, c.size());
        assertEquals("WordSize of empty corpus should be 0", 0, c.wordSize());
    }

    /**
     * Building a corpus with one NGram triggers the normal code path
     * that calls the private constructor with a non-empty set.
     */
    @Test
    public void testBuildNonEmptyCorpus() {
        Corpus c = Corpus.Builder.EMPTY
            .add(NGram.from("cat"))
            .build();
        assertNotNull("Corpus should not be null", c);
        assertEquals("Should contain exactly 1 NGram", 1, c.size());
        assertEquals("Word size should match 'cat' => 3", 3, c.wordSize());
    }

    /**
     * Checking the 'contains' and 'size(Filter)' methods for coverage.
     */
    @Test
    public void testContainsAndSizeFilter() {
        Corpus c = Corpus.Builder.EMPTY
            .add(NGram.from("cat"))
            .add(NGram.from("dog"))
            .build();
        
        // Test 'contains'
        assertTrue("Corpus should contain 'cat'", c.contains(NGram.from("cat")));
        assertFalse("Corpus should not contain 'fox'", c.contains(NGram.from("fox")));

        // Test 'size(Filter)'
        Filter length3Filter = Filter.from(nGram -> nGram.size() == 3);
        // cat and dog => 2 matches
        assertEquals("Should match exactly 2 NGrams of length 3", 2, c.size(length3Filter));
    }

    /**
     * Testing iterator() and stream() for coverage.
     */
    @Test
    public void testIteratorAndStream() {
        Corpus c = Corpus.Builder.EMPTY
            .add(NGram.from("cat"))
            .add(NGram.from("dog"))
            .build();
        
        // Test 'stream()'
        long count = c.stream().count();
        assertEquals("Corpus should contain 2 NGrams", 2, count);

        // Test 'iterator()'
        int itCount = 0;
        for (NGram n : c) {
            itCount++;
        }
        assertEquals("Should iterate through 2 NGrams", 2, itCount);
    }

    /**
     * Validate building with inconsistent NGram sizes.
     * This ensures coverage for the size-checking path in build().
     */
    @Test(expected = IllegalStateException.class)
    public void testBuildInconsistentSizes() {
        Corpus.Builder.EMPTY
            .add(NGram.from("hello")) // length 5
            .add(NGram.from("cat"))   // length 3
            .build();                 // should throw IllegalStateException
    }

    /**
     * Validate that 'addAll' can handle a null or empty collection appropriately.
     */
    @Test(expected = NullPointerException.class)
    public void testAddAllNullCollection() {
        Corpus.Builder.EMPTY.addAll(null);
    }

    /**
     * Confirm that add(NGram) rejects null NGram.
     */
    @Test(expected = NullPointerException.class)
    public void testAddNullNGram() {
        Corpus.Builder.EMPTY.add(null);
    }
}
