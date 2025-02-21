import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;

public class MatchleScorerTest {

    /**
     * Helper method to create a small corpus with three 3-letter words.
     */
    private Corpus createTestCorpus() {
        List<NGram> words = Arrays.asList(
            NGram.from("cat"),
            NGram.from("cot"),
            NGram.from("cut")
        );
        return Corpus.Builder.EMPTY.addAll(words).build();
    }

    /**
     * Test the score method.
     * For key "cat" and guess "cot", the feedback filter should:
     * - Require 'c' at index 0 and 't' at index 2 (exact matches)
     * - Require absence of 'o' (from index 1, since 'a' != 'o')
     * In our test corpus, "cat" and "cut" satisfy these conditions (score = 2).
     */
    @Test
    public void testScore() {
        Corpus corpus = createTestCorpus();
        MatchleScorer scorer = new MatchleScorer(corpus);
        NGram key = NGram.from("cat");
        NGram guess = NGram.from("cot");
        long score = scorer.score(key, guess);
        assertEquals("Score for key 'cat' and guess 'cot' should be 2", 2, score);
    }

    /**
     * Test the worst-case score for a guess.
     * For guess "cot":
     * - For key "cat": score = 2.
     * - For key "cot": score = 1 (exact match for all positions).
     * - For key "cut": score = 2.
     * Therefore, worst-case score = max(2, 1, 2) = 2.
     */
    @Test
    public void testScoreWorstCase() {
        Corpus corpus = createTestCorpus();
        MatchleScorer scorer = new MatchleScorer(corpus);
        NGram guess = NGram.from("cot");
        long worstScore = scorer.scoreWorstCase(guess);
        assertEquals("Worst-case score for guess 'cot' should be 2", 2, worstScore);
    }

    /**
     * Test the average-case score for a guess.
     * For guess "cot", the scores for keys "cat", "cot", and "cut" are 2, 1, and 2 respectively.
     * The sum (average-case score) is 2 + 1 + 2 = 5.
     */
    @Test
    public void testScoreAverageCase() {
        Corpus corpus = createTestCorpus();
        MatchleScorer scorer = new MatchleScorer(corpus);
        NGram guess = NGram.from("cot");
        long avgScore = scorer.scoreAverageCase(guess);
        assertEquals("Average-case score for guess 'cot' should be 5", 5, avgScore);
    }

    /**
     * Test that bestWorstCaseGuess returns a non-null value.
     */
    @Test
    public void testBestWorstCaseGuess() {
        Corpus corpus = createTestCorpus();
        MatchleScorer scorer = new MatchleScorer(corpus);
        NGram bestGuess = scorer.bestWorstCaseGuess();
        assertNotNull("Best worst-case guess should not be null", bestGuess);
        long score = scorer.scoreWorstCase(bestGuess);
        assertTrue("Score should be non-negative", score >= 0);
    }

    /**
     * Test that bestAverageCaseGuess returns a non-null value.
     */
    @Test
    public void testBestAverageCaseGuess() {
        Corpus corpus = createTestCorpus();
        MatchleScorer scorer = new MatchleScorer(corpus);
        NGram bestGuess = scorer.bestAverageCaseGuess();
        assertNotNull("Best average-case guess should not be null", bestGuess);
        long score = scorer.scoreAverageCase(bestGuess);
        assertTrue("Score should be non-negative", score >= 0);
    }

    /**
     * Test that score throws NullPointerException when key is null.
     */
    @Test(expected = NullPointerException.class)
    public void testScoreNullKey() {
        Corpus corpus = createTestCorpus();
        MatchleScorer scorer = new MatchleScorer(corpus);
        NGram guess = NGram.from("cot");
        scorer.score(null, guess);
    }

    /**
     * Test that score throws NullPointerException when guess is null.
     */
    @Test(expected = NullPointerException.class)
    public void testScoreNullGuess() {
        Corpus corpus = createTestCorpus();
        MatchleScorer scorer = new MatchleScorer(corpus);
        NGram key = NGram.from("cat");
        scorer.score(key, null);
    }
}
