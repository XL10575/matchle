import java.util.Optional;
import java.util.function.Predicate;

public final class Filter {

    private final Predicate<NGram> predicate;

    /**
     * Private constructor that sets the internal predicate.
     */
    private Filter(Predicate<NGram> predicate) {
        if (predicate == null) {
            throw new NullPointerException("Internal predicate cannot be null.");
        }
        this.predicate = predicate;
    }

    /**
     * Static factory method to create a new Filter from a Predicate<NGram>.
     */
    public static Filter from(Predicate<NGram> predicate) {
        if (predicate == null) {
            throw new NullPointerException("Predicate<NGram> cannot be null.");
        }
        return new Filter(predicate);
    }

    /**
     * A filter that always evaluates to false.
     */
    public static final Filter FALSE = new Filter(nGram -> false);

    /**
     * Delegates the test to the internal predicate.
     */
    public boolean test(NGram ngram) {
        if (ngram == null) {
            throw new NullPointerException("NGram cannot be null.");
        }
        return predicate.test(ngram);
    }

    /**
     * Returns a new Filter that is the logical AND of 'this' and 'other'
     * if 'other' is present. Otherwise returns 'this' if 'other' is empty.
     */
    public Filter and(Optional<Filter> other) {
        if (other == null) {
            throw new NullPointerException("Optional<Filter> cannot be null.");
        }
        if (other.isEmpty()) {
            // No additional filter, so just return this
            return this;
        }
        Filter otherFilter = other.get();
        if (otherFilter == null) {
            throw new NullPointerException("Filter inside Optional cannot be null.");
        }
        // Combine predicates with logical AND
        Predicate<NGram> newPredicate = nGram ->
                this.predicate.test(nGram) && otherFilter.predicate.test(nGram);
        return new Filter(newPredicate);
    }

    /**
     * Overloaded method to combine this filter with another non-null Filter.
     */
    public Filter and(Filter other) {
        if (other == null) {
            throw new NullPointerException("Filter cannot be null.");
        }
        return and(Optional.of(other));
    }
}
