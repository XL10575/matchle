import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;

public class MatchleScorerTest {

    // Helper method to create a small corpus with three 3-letter words.
    private Corpus createTestCorpus() {
        List<NGram> words = Arrays.asList(
            NGram.from("cat"),
            NGram.from("cot"),
            NGram.from("cut")
        );
        return Corpus.Builder.EMPTY.addAll(words).build();
    }

    @Test
    public void testScore() {
        Corpus corpus = createTestCorpus();
        MatchleScorer scorer = new MatchleScorer(corpus);
        NGram key = NGram.from("cat");
        NGram guess = NGram.from("cot");
        // Based on our matching logic:
        // "cat" vs "cot": exact matches at indices 0 and 2, absent at index 1 => score should be 2.
        long score = scorer.score(key, guess);
        assertEquals("Score for key 'cat' and guess 'cot' should be 2", 2, score);
    }

    @Test
    public void testScoreWorstCase() {
        Corpus corpus = createTestCorpus();
        MatchleScorer scorer = new MatchleScorer(corpus);
        NGram guess = NGram.from("cot");
        long worstScore = scorer.scoreWorstCase(guess);
        // For keys: "cat" yields score 2, "cot" yields 1, "cut" yields 2; worst-case = 2.
        assertEquals("Worst-case score for guess 'cot' should be 2", 2, worstScore);
    }

    @Test
    public void testScoreAverageCase() {
        Corpus corpus = createTestCorpus();
        MatchleScorer scorer = new MatchleScorer(corpus);
        NGram guess = NGram.from("cot");
        long averageScore = scorer.scoreAverageCase(guess);
        // The sum across keys: 2 + 1 + 2 = 5.
        assertEquals("Average-case score for guess 'cot' should be 5", 5, averageScore);
    }

    @Test
    public void testBestWorstCaseGuess() {
        Corpus corpus = createTestCorpus();
        MatchleScorer scorer = new MatchleScorer(corpus);
        NGram bestGuess = scorer.bestWorstCaseGuess();
        assertNotNull("Best worst-case guess should not be null", bestGuess);
        long score = scorer.scoreWorstCase(bestGuess);
        assertTrue("Worst-case score should be non-negative", score >= 0);
    }

    @Test
    public void testBestAverageCaseGuess() {
        Corpus corpus = createTestCorpus();
        MatchleScorer scorer = new MatchleScorer(corpus);
        NGram bestGuess = scorer.bestAverageCaseGuess();
        assertNotNull("Best average-case guess should not be null", bestGuess);
        long score = scorer.scoreAverageCase(bestGuess);
        assertTrue("Average-case score should be non-negative", score >= 0);
    }

    @Test(expected = NullPointerException.class)
    public void testScoreNullKey() {
        Corpus corpus = createTestCorpus();
        MatchleScorer scorer = new MatchleScorer(corpus);
        NGram guess = NGram.from("cot");
        scorer.score(null, guess);
    }

    @Test(expected = NullPointerException.class)
    public void testScoreNullGuess() {
        Corpus corpus = createTestCorpus();
        MatchleScorer scorer = new MatchleScorer(corpus);
        NGram key = NGram.from("cat");
        scorer.score(key, null);
    }
}
