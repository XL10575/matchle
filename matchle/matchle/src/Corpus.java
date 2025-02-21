import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
    private final Set<NGram> corpus;  // the unmodifiable set of NGrams
    private final int wordSize;       // the common size of all NGrams in corpus

    // =========================================================
    // Private Constructor
    // =========================================================
    private Corpus(Set<NGram> corpus, int wordSize) {
        if (corpus == null) {
            throw new NullPointerException("Corpus set cannot be null.");
        }
        // Make a defensive copy to ensure immutability
        this.corpus = Collections.unmodifiableSet(new HashSet<>(corpus));
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
     * Delegates to the internal corpus Set's size.
     *
     * @return the number of NGrams in the corpus
     */
    public int size() {
        return corpus.size();
    }

    /**
     * Delegates to the internal corpus Set's contains.
     */
    public boolean contains(NGram ngram) {
        if (ngram == null) {
            throw new NullPointerException("NGram cannot be null.");
        }
        return corpus.contains(ngram);
    }

    /**
     * Delegates to the internal corpus Set's iterator.
     */
    @Override
    public Iterator<NGram> iterator() {
        return corpus.iterator();
    }

    /**
     * Delegates to the internal corpus Set's stream.
     */
    public Stream<NGram> stream() {
        return corpus.stream();
    }

    /**
     * Returns how many NGrams in this corpus pass the given filter.
     */
    public long size(Filter filter) {
        if (filter == null) {
            throw new NullPointerException("Filter cannot be null.");
        }
        return corpus.stream()
                     .filter(filter::test)
                     .count();
    }

    // =========================================================
    // Nested Builder Class
    // =========================================================
    public static final class Builder {

        private final Set<NGram> ngrams;

        /**
         * Private constructor that sets the underlying Set.
         */
        private Builder(Set<NGram> ngrams) {
            if (ngrams == null) {
                throw new NullPointerException("NGram set cannot be null.");
            }
            this.ngrams = ngrams;
        }

        /**
         * A builder with no n-grams.
         */
        public static final Builder EMPTY = new Builder(new HashSet<>());

        /**
         * Builds a new Builder from the contents of the given Corpus.
         */
        public static final Builder of(Corpus corpus) {
            if (corpus == null) {
                throw new NullPointerException("Corpus cannot be null.");
            }
            return new Builder(new HashSet<>(corpus.corpus));
        }

        /**
         * Adds a single NGram to the builder or throws NullPointerException if the NGram is null.
         */
        public Builder add(NGram ngram) {
            if (ngram == null) {
                throw new NullPointerException("NGram cannot be null.");
            }
            ngrams.add(ngram);
            return this;
        }

        /**
         * Adds all non-null NGrams from the given collection.
         */
        public Builder addAll(Collection<NGram> collection) {
            if (collection == null) {
                throw new NullPointerException("Collection of NGrams cannot be null.");
            }
            collection.stream()
                      .filter(Objects::nonNull)
                      .forEach(ngrams::add);
            return this;
        }

        /**
         * Returns whether all NGrams in this builder have the given wordSize.
         */
        public boolean isConsistent(Integer wordSize) {
            if (wordSize == null) {
                throw new NullPointerException("wordSize cannot be null.");
            }
            for (NGram n : ngrams) {
                if (n == null) {
                    throw new NullPointerException("NGram in builder is null.");
                }
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
                // No single word size to enforce => size 0
                return new Corpus(Set.of(), 0);
            }
            int guessedSize = ngrams.iterator().next().size();
            for (NGram n : ngrams) {
                if (n == null) {
                    throw new NullPointerException("NGram in builder is null.");
                }
                if (n.size() != guessedSize) {
                    throw new IllegalStateException("Inconsistent NGram sizes in builder.");
                }
            }
            return new Corpus(ngrams, guessedSize);
        }
    }
}
