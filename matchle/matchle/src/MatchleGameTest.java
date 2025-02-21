import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;

public class MatchleGameTest {

    @Test
    public void testSimpleMatchle() {
        // --------------------------------------------------------
        // 1. Build a small corpus of 3-letter words
        // --------------------------------------------------------
        NGram cat = NGram.from("cat");
        NGram dog = NGram.from("dog");
        NGram fox = NGram.from("fox");

        // Using Corpus.Builder to create a corpus
        Corpus corpus = Corpus.Builder.EMPTY
                .add(cat)
                .add(dog)
                .add(fox)
                .build();

        // Basic checks
        assertNotNull("Corpus should not be null", corpus);
        assertEquals("All words should be length 3.", 3, corpus.wordSize());
        assertEquals("Corpus should contain exactly 3 NGrams.", 3, corpus.size());

        // --------------------------------------------------------
        // 2. Choose a 'key' from the corpus (the solution word)
        //    For the sake of the test, pick 'cat'
        // --------------------------------------------------------
        NGram key = cat;

        // --------------------------------------------------------
        // 3. Make a guess
        // --------------------------------------------------------
        NGram guess = NGram.from("cot"); // Another 3-letter guess
        assertEquals("Guess should also be length 3.", 3, guess.size());

        // --------------------------------------------------------
        // 4. Demonstrate matchle logic:
        //    For each letter in the guess, determine if it:
        //      - matches exactly at that index,
        //      - appears elsewhere in the key,
        //      - or does not appear at all.
        // --------------------------------------------------------
        // We'll build an array of 'result' states for demonstration:
        //   "MATCH" if guess[i] == key[i]
        //   "ELSEWHERE" if guess[i] is in key but at a different index
        //   "NO MATCH" otherwise

        String[] result = new String[guess.size()];

        for (int i = 0; i < guess.size(); i++) {
            char g = guess.get(i);
            NGram.IndexedCharacter ic = new NGram.IndexedCharacter(i, g);

            if (key.matches(ic)) {
                result[i] = "MATCH";
            } else if (key.containsElsewhere(ic)) {
                result[i] = "ELSEWHERE";
            } else if (key.contains(g)) {
                // 'contains(g)' is true, but 'containsElsewhere(ic)' was false,
                // which might mean it's already matched or there's no open spot left.
                // We'll label it "ELSEWHERE?" for demonstration.
                result[i] = "ELSEWHERE?";
            } else {
                result[i] = "NO MATCH";
            }
        }

        // Manually verify:
        //   guess = "cot", key = "cat"
        //   index 0: guess='c', key='c' => MATCH
        //   index 1: guess='o', key='a' => NO MATCH
        //   index 2: guess='t', key='t' => MATCH
        //
        // Expect: ["MATCH", "NO MATCH", "MATCH"]
        assertEquals("First letter should match.", "MATCH", result[0]);
        assertEquals("Second letter should not match or appear.", "NO MATCH", result[1]);
        assertEquals("Third letter should match.", "MATCH", result[2]);
    }

    @Test
    public void testAnotherGuessScenario() {
        // Build a small 3-letter corpus again
        List<NGram> threeLetterWords = Arrays.asList(
            NGram.from("cat"),
            NGram.from("dog"),
            NGram.from("fox")
        );
        Corpus corpus = Corpus.Builder.EMPTY.addAll(threeLetterWords).build();
        assertNotNull("Corpus should not be null", corpus);
        assertEquals("Expected word size 3", 3, corpus.wordSize());

        // Pick 'dog' as our key
        NGram key = NGram.from("dog");

        // Make a guess: "god"
        NGram guess = NGram.from("god");
        int exactMatches = 0;

        for (int i = 0; i < guess.size(); i++) {
            NGram.IndexedCharacter ic = new NGram.IndexedCharacter(i, guess.get(i));
            if (key.matches(ic)) {
                exactMatches++;
            }
        }

        // "dog" vs "god":
        //   index 0 => guess='g', key='d' => not match
        //   index 1 => guess='o', key='o' => match
        //   index 2 => guess='d', key='g' => not match
        // exactMatches = 1
        assertEquals("Should match only on index 1.", 1, exactMatches);
    }
}
