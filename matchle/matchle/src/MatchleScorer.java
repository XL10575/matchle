import java.util.Comparator;
import java.util.Objects;
import java.util.function.ToLongFunction;

public class MatchleScorer {
    private final Corpus corpus;

    public MatchleScorer(Corpus corpus) {
        if (corpus == null || corpus.size() == 0) {
            throw new IllegalArgumentException("Corpus must not be null or empty");
        }
        this.corpus = corpus;
    }

    /**
     * Computes the score for a given key/guess pair using a parallel stream.
     */
    public long score(NGram key, NGram guess) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(guess, "Guess cannot be null");
        // Generate feedback filter from comparing key and guess.
        Filter feedback = NGramMatcher.of(key, guess).match();
        // Count in parallel how many NGrams in the corpus pass the filter.
        return corpus.corpus().parallelStream()
                     .filter(feedback::test)
                     .count();
    }

    /**
     * Worst-case score for a guess: maximum score over all keys in the corpus.
     */
    public long scoreWorstCase(NGram guess) {
        Objects.requireNonNull(guess, "Guess cannot be null");
        return corpus.corpus().parallelStream()
                     .mapToLong(key -> score(key, guess))
                     .max()
                     .orElseThrow();
    }

    /**
     * Average-case score for a guess: sum of the scores over all keys.
     */
    public long scoreAverageCase(NGram guess) {
        Objects.requireNonNull(guess, "Guess cannot be null");
        return corpus.corpus().parallelStream()
                     .mapToLong(key -> score(key, guess))
                     .sum();
    }

    /**
     * Finds the guess that minimizes the given criterion.
     */
    public NGram bestGuess(ToLongFunction<NGram> criterion) {
        Objects.requireNonNull(criterion, "Criterion cannot be null");
        return corpus.corpus().parallelStream()
                     .min(Comparator.comparingLong(criterion))
                     .orElse(null);
    }

    /**
     * Best guess according to worst-case scoring.
     */
    public NGram bestWorstCaseGuess() {
        return bestGuess(this::scoreWorstCase);
    }

    /**
     * Best guess according to average-case scoring.
     */
    public NGram bestAverageCaseGuess() {
        return bestGuess(this::scoreAverageCase);
    }
}
