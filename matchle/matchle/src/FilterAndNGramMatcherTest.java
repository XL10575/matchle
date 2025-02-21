import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Optional;
import java.util.function.Predicate;

public class FilterAndNGramMatcherTest {

    // Tests for Filter
    
    @Test
    public void testFilterBasic() {
        // A filter that checks if the NGram has size 5
        Filter sizeFiveFilter = Filter.from(nGram -> nGram.size() == 5);

        // Some test NGrams
        NGram n1 = NGram.from("hello"); // size 5
        NGram n2 = NGram.from("cat");   // size 3

        assertTrue("NGram 'hello' should pass sizeFiveFilter", sizeFiveFilter.test(n1));
        assertFalse("NGram 'cat' should fail sizeFiveFilter", sizeFiveFilter.test(n2));
    }

    @Test
    public void testFilterFALSE() {
        // The filter that always returns false
        Filter alwaysFalse = Filter.FALSE;

        // Any NGram tested should return false
        NGram hello = NGram.from("hello");
        assertFalse("Filter.FALSE should fail any NGram", alwaysFalse.test(hello));
    }

    @Test
    public void testFilterAnd() {
        // Create two filters
        // 1) Must contain 'l'
        Filter containsL = Filter.from(nGram -> nGram.contains('l'));
        // 2) Must have size == 5
        Filter sizeFive = Filter.from(nGram -> nGram.size() == 5);

        // Combine them via 'and'
        Filter combined = containsL.and(Optional.of(sizeFive));

        // Test NGrams
        NGram n1 = NGram.from("hello"); // contains 'l' and size = 5 => true
        NGram n2 = NGram.from("help");  // contains 'l' but size = 4 => false
        NGram n3 = NGram.from("world"); // contains 'l' and size = 5 => true

        assertTrue(combined.test(n1));
        assertFalse(combined.test(n2));
        assertTrue(combined.test(n3));

        // If we pass Optional.empty(), it should return the original filter
        Filter unchanged = containsL.and(Optional.empty());
        assertTrue(unchanged.test(NGram.from("help"))); // only checks contains('l')
    }

    @Test(expected = NullPointerException.class)
    public void testFilterFromNullPredicate() {
        // Attempting to create a Filter with a null predicate should throw NPE
        Filter.from(null);
    }

    // =========================================================================
    // Tests for NGramMatcher
    // =========================================================================

    @Test
    public void testNGramMatcherDifferentLengths() {
        // key is length 5, guess is length 4 => result filter should be FALSE
        NGram key = NGram.from("rebus");
        NGram guess = NGram.from("four");
        NGramMatcher matcher = NGramMatcher.of(key, guess);
        Filter f = matcher.match();

        // The filter should always return false
        assertFalse(f.test(NGram.from("rebus")));
        assertFalse(f.test(NGram.from("redux")));
    }

    @Test
    public void testNGramMatcherExactAndAbsent() {
        NGram key = NGram.from("cat");
        NGram guess = NGram.from("cot");
        NGramMatcher matcher = NGramMatcher.of(key, guess);
        Filter feedback = matcher.match();

        // "cat" itself always satisfies
        assertTrue(feedback.test(NGram.from("cat")));
        assertTrue("Depending on the logic, 'cut' might pass if there's no constraint for 'a' at index1",
                feedback.test(NGram.from("cut")));
        assertFalse(feedback.test(NGram.from("cot")));

        // Another check: "cab" => has 'c' at index0, 't'? => no, so it fails the EXACT match for index2
        assertFalse(feedback.test(NGram.from("cab")));
    }

    @Test
    public void testNGramMatcherMisplaced() {
        NGram key = NGram.from("cat");
        NGram guess = NGram.from("tac");
        NGramMatcher matcher = NGramMatcher.of(key, guess);
        Filter feedback = matcher.match();

        // "cat" must pass its own feedback if we interpret the mismatch carefully
        assertTrue("Key should satisfy its own feedback", feedback.test(NGram.from("cat")));
        assertFalse(feedback.test(NGram.from("tac")));
    }

    @Test
    public void testNGramMatcherAnagram() {
        // key = "rebus", guess = "rubes"
        NGram key = NGram.from("rebus");
        NGram guess = NGram.from("rubes");
        Filter feedback = NGramMatcher.of(key, guess).match();

        // key should pass its own feedback
        assertTrue(feedback.test(key));

        // guess might also pass if all letters are accounted for
        assertFalse(feedback.test(guess));

        // Something that doesn't have the right distribution of letters or positions might fail
        assertFalse(feedback.test(NGram.from("rubee"))); // has 2 'e' but only 1 in key
        assertFalse(feedback.test(NGram.from("rebux"))); // 'x' is not in the key
    }
}
