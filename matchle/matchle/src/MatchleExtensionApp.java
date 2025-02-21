import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A console-based Matchle game application.
 * 
 * The game:
 *  - Reads a word list from "words.txt" and filters for words of length n.
 *  - Randomly selects 100 words to build the corpus.
 *  - Chooses a random key (hidden from the user).
 *  - In each round, the user is prompted for a guess.
 *    * Detailed per-character feedback is provided.
 *    * Candidate guesses are computed from the full corpus and filtered by known correct positions.
 *    * A utility then displays the best guess suggestions (by worst-case and average-case scoring)
 *      from the remaining candidate words.
 *  - The game ends when the key is guessed or maximum rounds are reached.
 *
 * All helper methods have been refactored so that no single method has cyclomatic complexity greater than 4.
 */
public class MatchleExtensionApp {

    public static void main(String[] args) {
        List<String> words = downloadWordList("words.txt");
        if (words == null || words.isEmpty()) {
            System.err.println("No words loaded from file.");
            return;
        }
        int n = 5; // fixed word length
        Corpus corpus = buildCorpus(words, n, 100);
        System.out.println("Corpus built with " + corpus.size() + " n-grams.");

        MatchleScorer scorer = new MatchleScorer(corpus);
        NGram key = chooseRandomKey(corpus);
        if (key == null) {
            System.err.println("No key available.");
            return;
        }
        System.out.println("A random key has been selected. (It is hidden from you.)");
        
        // Initialize correctPositions array; '_' denotes unknown.
        char[] correctPositions = new char[n];
        Arrays.fill(correctPositions, '_');
        
        // Display initial candidate information.
        List<NGram> initCandidates = getCandidateGuesses(corpus, scorer, correctPositions);
        System.out.println("Initial candidate guesses: " +
            initCandidates.stream().map(MatchleExtensionApp::ngramToString)
                          .collect(Collectors.joining(", ")));
                          
        runGame(corpus, scorer, key, n, 10, correctPositions);
    }
    
    /**
     * Reads words from the specified file.
     */
    public static List<String> downloadWordList(String filePath) {
        if (filePath == null) {
            throw new NullPointerException("File path is null.");
        }
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            return lines.stream()
                .map(String::trim)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error reading words from " + filePath, e);
        }
    }
    
    /**
     * Builds a Corpus from random words of length n.
     */
    public static Corpus buildCorpus(List<String> words, int n, int sampleSize) {
        List<String> filtered = words.stream()
                .filter(w -> w != null && w.length() == n)
                .collect(Collectors.toList());
        if (filtered.size() > sampleSize) {
            Collections.shuffle(filtered);
            filtered = filtered.subList(0, sampleSize);
        }
        Map<NGram, Integer> freqMap = new HashMap<>();
        for (String word : filtered) {
            NGram ng = NGram.from(word);
            freqMap.put(ng, freqMap.getOrDefault(ng, 0) + 1);
        }
        Corpus.Builder builder = Corpus.Builder.EMPTY;
        for (NGram ng : freqMap.keySet()) {
            builder.add(ng);
        }
        return builder.build();
    }
    
    /**
     * Runs the game loop for a maximum of maxRounds.
     */
    public static void runGame(Corpus corpus, MatchleScorer scorer, NGram key,
                               int n, int maxRounds, char[] correctPositions) {
        Scanner scanner = new Scanner(System.in);
        int round = 1;
        while (round <= maxRounds) {
            boolean correct = processRound(round, corpus, scorer, key, n, scanner, correctPositions);
            if (correct) {
                break;
            }
            round++;
        }
        if (round > maxRounds) {
            System.out.println("Game over! The key was: " + ngramToString(key));
        }
        scanner.close();
    }
    
    /**
     * Processes one round of the game.
     * Returns true if the correct guess was made.
     */
    public static boolean processRound(int round, Corpus corpus, MatchleScorer scorer, 
                                        NGram key, int n, Scanner scanner, char[] correctPositions) {
        System.out.println("---------- Round " + round + " ----------");
        System.out.println("Enter your guess word of length " + n + ":");
        String input = scanner.nextLine().trim();
        NGram guess;
        if (input.isEmpty() || input.length() != n) {
            System.out.println("Invalid input. Using top candidate guess.");
            guess = getCandidateGuess(corpus, scorer, correctPositions);
        } else {
            guess = NGram.from(input);
        }
        String feedback = getFeedbackMessage(key, guess);
        System.out.println("Feedback: " + feedback);
        updateCorrectPositions(key, guess, correctPositions);
        if (guess.equals(key)) {
            System.out.println("Congratulations! You found the key: " + ngramToString(key));
            return true;
        } else {
            List<NGram> candidates = getCandidateGuesses(corpus, scorer, correctPositions);
            System.out.println("Candidate guesses: " +
                candidates.stream().map(MatchleExtensionApp::ngramToString)
                          .collect(Collectors.joining(", ")));
            // Utility: show best guesses by worst-case and average-case.
            Map<String, NGram> best = getBestGuessesUtility(corpus, scorer, correctPositions);
            if (!best.isEmpty()) {
                System.out.println("Best worst-case guess: " + ngramToString(best.get("Worst-case")));
                System.out.println("Best average-case guess: " + ngramToString(best.get("Average-case")));
            }
            return false;
        }
    }
    
    /**
     * Updates the correctPositions array where the guess matches the key exactly.
     */
    public static void updateCorrectPositions(NGram key, NGram guess, char[] correctPositions) {
        int n = key.size();
        for (int i = 0; i < n; i++) {
            if (key.get(i).equals(guess.get(i))) {
                correctPositions[i] = key.get(i);
            }
        }
    }
    
    /**
     * Chooses a random NGram key from the corpus.
     */
    public static NGram chooseRandomKey(Corpus corpus) {
        if (corpus == null) {
            throw new NullPointerException("Corpus is null.");
        }
        int size = corpus.size();
        if (size == 0) return null;
        int index = (int)(Math.random() * size);
        Iterator<NGram> it = corpus.iterator();
        for (int i = 0; i < index && it.hasNext(); i++) {
            it.next();
        }
        return it.hasNext() ? it.next() : null;
    }
    
    /**
     * Converts an NGram to its String representation.
     */
    public static String ngramToString(NGram ngram) {
        if (ngram == null) {
            throw new NullPointerException("NGram is null.");
        }
        StringBuilder sb = new StringBuilder();
        for (NGram.IndexedCharacter ic : ngram) {
            sb.append(ic.character());
        }
        return sb.toString();
    }
    
    /**
     * Returns detailed feedback comparing the guess to the key.
     * For each position, it indicates if the letter is correct, misplaced, or missing.
     */
    public static String getFeedbackMessage(NGram key, NGram guess) {
        if (key == null || guess == null) {
            throw new NullPointerException("Key and guess cannot be null.");
        }
        int n = key.size();
        boolean[] exact = computeExactMatches(key, guess);
        boolean[] misplaced = computeMisplacedMatches(key, guess, exact);
        StringBuilder feedback = new StringBuilder();
        for (int i = 0; i < n; i++) {
            char letter = guess.get(i);
            if (exact[i]) {
                feedback.append("Position ").append(i + 1).append(" (").append(letter).append("): Correct. ");
            } else if (misplaced[i]) {
                feedback.append("Position ").append(i + 1).append(" (").append(letter).append("): Present but misplaced. ");
            } else {
                feedback.append("Position ").append(i + 1).append(" (").append(letter).append("): Missing. ");
            }
        }
        return feedback.toString();
    }
    
    /**
     * Computes an array indicating exact matches between key and guess.
     */
    public static boolean[] computeExactMatches(NGram key, NGram guess) {
        int n = key.size();
        boolean[] exact = new boolean[n];
        for (int i = 0; i < n; i++) {
            exact[i] = key.get(i).equals(guess.get(i));
        }
        return exact;
    }
    
    /**
     * Computes an array indicating misplaced matches between key and guess.
     */
    public static boolean[] computeMisplacedMatches(NGram key, NGram guess, boolean[] exact) {
        int n = key.size();
        boolean[] misplaced = new boolean[n];
        Map<Character, Integer> freq = new HashMap<>();
        for (int i = 0; i < n; i++) {
            if (!exact[i]) {
                char letter = key.get(i);
                freq.put(letter, freq.getOrDefault(letter, 0) + 1);
            }
        }
        for (int i = 0; i < n; i++) {
            if (!exact[i]) {
                char letter = guess.get(i);
                if (freq.getOrDefault(letter, 0) > 0) {
                    misplaced[i] = true;
                    freq.put(letter, freq.get(letter) - 1);
                }
            }
        }
        return misplaced;
    }
    
    /**
     * Returns candidate guesses from the full corpus, sorted by worst-case score,
     * and filtered to only include words that match the known correct positions.
     * (The key is ensured to be included.)
     */
    public static List<NGram> getCandidateGuesses(Corpus corpus, MatchleScorer scorer, char[] correctPositions) {
        List<NGram> candidates = corpus.stream()
            .filter(ng -> matchesCorrectPositions(ng, correctPositions))
            .sorted(Comparator.comparingLong(ng -> scorer.scoreWorstCase(ng)))
            .collect(Collectors.toList());
        if (!candidates.contains(ngramFromCorrectPositions(correctPositions))) {
            // If no candidate exactly matches known correct positions, add the constructed candidate.
            candidates.add(ngramFromCorrectPositions(correctPositions));
            candidates.sort(Comparator.comparingLong(ng -> scorer.scoreWorstCase(ng)));
        }
        return candidates;
    }
    
    /**
     * Returns the top candidate guess from the filtered candidate list.
     */
    public static NGram getCandidateGuess(Corpus corpus, MatchleScorer scorer, char[] correctPositions) {
        List<NGram> candidates = getCandidateGuesses(corpus, scorer, correctPositions);
        return candidates.isEmpty() ? null : candidates.get(0);
    }
    
    /**
     * Checks whether the given candidate NGram has, at every position where a correct letter is known,
     * the same letter as in correctPositions.
     */
    public static boolean matchesCorrectPositions(NGram candidate, char[] correctPositions) {
        int n = candidate.size();
        for (int i = 0; i < n; i++) {
            if (correctPositions[i] != '_' && candidate.get(i) != correctPositions[i]) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Constructs an NGram from the correctPositions array.
     * For positions still unknown ('_'), uses a placeholder (e.g., 'a').
     */
    public static NGram ngramFromCorrectPositions(char[] correctPositions) {
        StringBuilder sb = new StringBuilder();
        for (char c : correctPositions) {
            sb.append(c == '_' ? 'a' : c);
        }
        return NGram.from(sb.toString());
    }
    
    /**
     * Utility method that computes and returns the best guess suggestions
     * (both worst-case and average-case) from candidates matching known correct positions.
     */
    public static Map<String, NGram> getBestGuessesUtility(Corpus corpus, MatchleScorer scorer, char[] correctPositions) {
        List<NGram> filtered = corpus.stream()
            .filter(ng -> matchesCorrectPositions(ng, correctPositions))
            .collect(Collectors.toList());
        Corpus.Builder builder = Corpus.Builder.EMPTY;
        for (NGram ng : filtered) {
            builder.add(ng);
        }
        Corpus filteredCorpus = builder.build();
        Map<String, NGram> bestGuesses = new HashMap<>();
        if (filteredCorpus.size() > 0) {
            MatchleScorer filteredScorer = new MatchleScorer(filteredCorpus);
            bestGuesses.put("Worst-case", filteredScorer.bestWorstCaseGuess());
            bestGuesses.put("Average-case", filteredScorer.bestAverageCaseGuess());
        }
        return bestGuesses;
    }
}
