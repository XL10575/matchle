import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

/**
 * A dictionary of NGrams, all of the same length.
 */
public final class Corpus implements Iterable<NGram> {

    // =========================================================
    // Private Fields
    // =========================================================
    private final Set<NGram> corpus;  // Unmodifiable set of NGrams
    private final int wordSize;       // Common size of all NGrams in corpus

    // =========================================================
    // Private Constructor
    // =========================================================
    private Corpus(Set<NGram> corpus, int wordSize) {
        // Defensive null check and immutability:
        this.corpus = Collections.unmodifiableSet(new HashSet<>(Objects.requireNonNull(corpus, "Corpus set cannot be null.")));
        // Optionally, you can force wordSize > 0 if desired when corpus is non-empty.
        this.wordSize = wordSize;
    }

    // =========================================================
    // Public Methods
    // =========================================================

    /**
     * Returns an unmodifiable set of NGrams.
     */
    public Set<NGram> corpus() {
        return corpus;
    }

    /**
     * Returns the common size of the NGrams in this corpus.
     */
    public int wordSize() {
        return wordSize;
    }

    /**
     * Returns the number of NGrams in the corpus.
     */
    public int size() {
        return corpus.size();
    }

    /**
     * Checks if the given NGram is contained in this corpus.
     */
    public boolean contains(NGram ngram) {
        return corpus.contains(Objects.requireNonNull(ngram, "NGram cannot be null."));
    }

    /**
     * Returns an iterator over the NGrams.
     */
    @Override
    public Iterator<NGram> iterator() {
        return corpus.iterator();
    }

    /**
     * Returns a stream of NGrams.
     */
    public Stream<NGram> stream() {
        return corpus.stream();
    }

    /**
     * Returns the count of NGrams that satisfy the given filter.
     */
    public long size(Filter filter) {
        Objects.requireNonNull(filter, "Filter cannot be null.");
        return corpus.stream()
                     .filter(filter::test)
                     .count();
    }

    // =========================================================
    // Nested Builder Class with enhanced defensive checks
    // =========================================================
    public static final class Builder {

        private final Set<NGram> ngrams;

        /**
         * Private constructor that sets the underlying set.
         */
        private Builder(Set<NGram> ngrams) {
            this.ngrams = new HashSet<>(Objects.requireNonNull(ngrams, "NGram set cannot be null."));
        }

        /**
         * A builder with no NGrams.
         */
        public static final Builder EMPTY = new Builder(new HashSet<>());

        /**
         * Creates a new Builder from the contents of the given Corpus.
         */
        public static final Builder of(Corpus corpus) {
            return new Builder(new HashSet<>(Objects.requireNonNull(corpus, "Corpus cannot be null.").corpus()));
        }

        /**
         * Adds a single NGram to the builder.
         *
         * @throws NullPointerException if the NGram is null.
         */
        public Builder add(NGram ngram) {
            ngrams.add(Objects.requireNonNull(ngram, "NGram cannot be null."));
            return this;
        }

        /**
         * Adds all non-null NGrams from the given collection.
         *
         * @throws NullPointerException if the collection is null.
         */
        public Builder addAll(Collection<NGram> collection) {
            Objects.requireNonNull(collection, "Collection of NGrams cannot be null.");
            collection.stream()
                      .filter(Objects::nonNull)
                      .forEach(ngrams::add);
            return this;
        }

        /**
         * Returns whether all NGrams in this builder have the given wordSize.
         *
         * @throws NullPointerException if wordSize is null.
         */
        public boolean isConsistent(Integer wordSize) {
            Objects.requireNonNull(wordSize, "wordSize cannot be null.");
            for (NGram n : ngrams) {
                // Extra defensive check: ensure no null elements are inside.
                Objects.requireNonNull(n, "NGram in builder is null.");
                if (n.size() != wordSize) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Builds a new Corpus if all NGrams share the same word size;
         * otherwise throws an IllegalStateException.
         */
        public Corpus build() {
            if (ngrams.isEmpty()) {
                // When empty, we define a corpus with size 0
                return new Corpus(Set.of(), 0);
            }
            int guessedSize = ngrams.iterator().next().size();
            for (NGram n : ngrams) {
                Objects.requireNonNull(n, "NGram in builder is null.");
                if (n.size() != guessedSize) {
                    throw new IllegalStateException("Inconsistent NGram sizes in builder.");
                }
            }
            return new Corpus(ngrams, guessedSize);
        }
    }
}
