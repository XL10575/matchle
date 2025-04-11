import org.junit.Test;
import static org.junit.Assert.*;

public class NGramMatcherTest {

    // Test different lengths: match() should return Filter.FALSE.
    @Test
    public void testDifferentLengths() {
        NGram key = NGram.from("rebus");
        NGram guess = NGram.from("four");
        NGramMatcher matcher = NGramMatcher.of(key, guess);
        Filter feedback = matcher.match();
        assertFalse(feedback.test(NGram.from("rebus")));
    }
    
    // Test exact and absent feedback.
    @Test
    public void testExactAndAbsent() {
        NGram key = NGram.from("cat");
        NGram guess = NGram.from("cot");
        NGramMatcher matcher = NGramMatcher.of(key, guess);
        Filter feedback = matcher.match();
        
        // For "cat" vs "cot", index 0 and 2 are exact, index 1 absent.
        assertTrue(feedback.test(NGram.from("cat")));
        assertFalse(feedback.test(NGram.from("cab"))); // fails at index 2
    }
    
    // Test misplaced match functionality.
    @Test
    public void testMisplaced() {
        NGram key = NGram.from("cat");
        NGram guess = NGram.from("tac");
        NGramMatcher matcher = NGramMatcher.of(key, guess);
        Filter feedback = matcher.match();
        // For "cat" vs "tac", expect that key "cat" still satisfies the feedback,
        // because letters exist but in different positions.
        assertTrue(feedback.test(NGram.from("cat")));
    }
    
    // Test anagram scenario.
    @Test
    public void testAnagram() {
        NGram key = NGram.from("rebus");
        NGram guess = NGram.from("rubes");
        Filter feedback = NGramMatcher.of(key, guess).match();
        
        // The target key should satisfy its own feedback.
        assertTrue(feedback.test(key));
        // Testing with an incorrect distribution should fail.
        assertFalse(feedback.test(NGram.from("rubee"))); // extra 'e'
    }
}
