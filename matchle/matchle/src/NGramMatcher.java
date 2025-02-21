import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Package-private final class implementing the MATCH algorithm
 * for comparing a key NGram with a guessed NGram.
 */
final class NGramMatcher {

    private final NGram key;
    private final NGram guess;

    // Private constructor: only callable by 'of(...)'
    private NGramMatcher(NGram key, NGram guess) {
        this.key = key;
        this.guess = guess;
    }

    /**
     * Factory method to create a new NGramMatcher from the given key and guess.
     */
    public static NGramMatcher of(NGram key, NGram guess) {
        return new NGramMatcher(key, guess);
    }

    /**
     * Executes the MATCH algorithm and returns a Filter representing the feedback.
     * If key and guess differ in length, returns Filter.FALSE.
     */
    public Filter match() {
        // 1. If key/guess length differ, return FALSE immediately
        if (key.size() != guess.size()) {
            return Filter.FALSE;
        }

        int n = key.size();
        boolean[] keyMatched = new boolean[n];
        boolean[] guessMatched = new boolean[n];
        List<Filter> partialFilters = new ArrayList<>();

        // PASS 1: Exact matches
        doExactMatches(n, keyMatched, guessMatched, partialFilters);

        // PASS 2: Misplaced matches
        doMisplacedMatches(n, keyMatched, guessMatched, partialFilters);

        // PASS 3: Absent characters
        doAbsentCharacters(n, guessMatched, partialFilters);

        // Combine all partial filters into a single Filter
        Filter result = Filter.from(ng -> true);
        for (Filter f : partialFilters) {
            result = result.and(Optional.of(f));
        }
        return result;
    }

    /**
     * PASS 1: Identify exact matches (same character, same index).
     */
    private void doExactMatches(int n,
                                boolean[] keyMatched,
                                boolean[] guessMatched,
                                List<Filter> partialFilters) {
        for (int i = 0; i < n; i++) {
            char keyChar = key.get(i);
            char guessChar = guess.get(i);

            if (keyChar == guessChar) {
                keyMatched[i] = true;
                guessMatched[i] = true;

                NGram.IndexedCharacter ic = new NGram.IndexedCharacter(i, guessChar);
                Filter exactFilter = Filter.from(ng -> ng.matches(ic));
                partialFilters.add(exactFilter);
            }
        }
    }
    private void doAbsentCharacters(int n,
                                    boolean[] guessMatched,
                                    List<Filter> partialFilters) {
        for (int i = 0; i < n; i++) {
            if (!guessMatched[i]) {
                char guessChar = guess.get(i);

                // If still unmatched, it is absent in the key
                Filter absentFilter = Filter.from(ng -> !ng.contains(guessChar));
                partialFilters.add(absentFilter);
            }
        }
    }

    private void doMisplacedMatches(int n,
                                boolean[] keyMatched,
                                boolean[] guessMatched,
                                List<Filter> partialFilters) {
    // Outer loop only does 'for' + a single method call => reduced complexity
    for (int i = 0; i < n; i++) {
        handleMisplacedAtIndex(i, n, keyMatched, guessMatched, partialFilters);
    }
}

    private void handleMisplacedAtIndex(int i,
                                    int n,
                                    boolean[] keyMatched,
                                    boolean[] guessMatched,
                                    List<Filter> partialFilters) {
    if (guessMatched[i]) {
        return;
    }

    char guessChar = guess.get(i);
    for (int j = 0; j < n; j++) {
        if (!keyMatched[j] && key.get(j) == guessChar) {
            // Found a misplaced match
            keyMatched[j] = true;
            guessMatched[i] = true;

            NGram.IndexedCharacter ic = new NGram.IndexedCharacter(i, guessChar);
            Filter misplacedFilter = Filter.from(ng -> ng.containsElsewhere(ic));
            partialFilters.add(misplacedFilter);

            break; // stop once we match guess[i] 
        }
    }
}
}
