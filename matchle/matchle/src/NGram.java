import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class NGram implements Iterable<NGram.IndexedCharacter> {

    private final ArrayList<Character> ngram;
    private final Set<Character> charset;

    public static record IndexedCharacter(int index, Character character) {}

    public static final class NullCharacterException extends Exception {
        private static final long serialVersionUID = 20250131L; // arbitrary value
        private final int index;

        public NullCharacterException(int index) {
            super("Null character found at index: " + index);
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    /**
     * Validates that the provided list of characters is not null and contains no null elements.
     *
     * @param ngram the list of characters representing the n-gram.
     * @return the validated list.
     * @throws NullPointerException if the list itself is null.
     * @throws IllegalArgumentException if any character in the list is null.
     */
    public static final List<Character> validate(List<Character> ngram) {
        if (ngram == null) {
            throw new NullPointerException("Provided character list is null.");
        }
        for (int i = 0; i < ngram.size(); i++) {
            if (ngram.get(i) == null) {
                throw new IllegalArgumentException(
                    "Null character found at index " + i,
                    new NullCharacterException(i)
                );
            }
        }
        return ngram;
    }

    // Private constructor with defensive null check.
    private NGram(ArrayList<Character> ngram) {
        if (ngram == null) {
            throw new NullPointerException("Input ngram list cannot be null.");
        }
        this.ngram = new ArrayList<>(ngram);
        this.charset = new HashSet<>(this.ngram);
    }

    /**
     * Factory method to create a new NGram from a List of Characters.
     *
     * @param chars the list of characters for the n-gram.
     * @return a new NGram instance.
     */
    public static final NGram from(List<Character> chars) {
        validate(chars);
        return new NGram(new ArrayList<>(chars));
    }

    /**
     * Factory method to create a new NGram from a String.
     *
     * @param str the input string.
     * @return a new NGram instance.
     */
    public static final NGram from(String str) {
        if (str == null) {
            throw new NullPointerException("Provided string is null.");
        }
        ArrayList<Character> list = new ArrayList<>(str.length());
        for (char c : str.toCharArray()) {
            list.add(c);
        }
        validate(list);
        return new NGram(list);
    }

    /**
     * Checks if the n-gram matches the given IndexedCharacter at its index.
     *
     * @param c the IndexedCharacter to compare.
     * @return true if the character at the given index equals the provided character; false otherwise.
     */
    public boolean matches(IndexedCharacter c) {
        if (c == null) {
            throw new NullPointerException("IndexedCharacter cannot be null.");
        }
        int i = c.index();
        if (i < 0 || i >= ngram.size()) {
            return false; // out of bounds
        }
        return ngram.get(i).equals(c.character());
    }

    /**
     * Checks whether this n-gram contains the given character.
     *
     * @param c the character to check.
     * @return true if the character is present; false otherwise.
     */
    public boolean contains(char c) {
        return charset.contains(c);
    }

    /**
     * Checks if the provided IndexedCharacter appears elsewhere in the n-gram.
     *
     * @param c the IndexedCharacter to check.
     * @return true if the character appears in a different index; false otherwise.
     */
    public boolean containsElsewhere(IndexedCharacter c) {
        if (c == null) {
            throw new NullPointerException("IndexedCharacter cannot be null.");
        }
        if (!contains(c.character())) {
            return false;
        }
        if (!matches(c)) {
            return true;
        }
        long count = ngram.stream().filter(ch -> ch.equals(c.character())).count();
        return count > 1;
    }

    /**
     * Creates a Stream of IndexedCharacter objects, with indexes 0..size-1.
     *
     * @return a stream of IndexedCharacter.
     */
    public Stream<IndexedCharacter> stream() {
        return IntStream.range(0, ngram.size())
                        .mapToObj(i -> new IndexedCharacter(i, ngram.get(i)));
    }

    @Override
    public Iterator<IndexedCharacter> iterator() {
        return new NGramIterator();
    }

    public final class NGramIterator implements Iterator<IndexedCharacter> {
        private int currentIndex = 0;

        @Override
        public boolean hasNext() {
            return currentIndex < ngram.size();
        }

        @Override
        public IndexedCharacter next() {
            IndexedCharacter ic = new IndexedCharacter(currentIndex, ngram.get(currentIndex));
            currentIndex++;
            return ic;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove operation not supported.");
        }
    }

    /**
     * Returns the character at the given index.
     *
     * @param index the index of the character.
     * @return the character at the specified index.
     * @throws IndexOutOfBoundsException if the index is invalid.
     */
    public Character get(int index) {
        if (index < 0 || index >= ngram.size()) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for NGram of size " + ngram.size());
        }
        return ngram.get(index);
    }

    /**
     * Returns the size (number of characters) of this n-gram.
     *
     * @return the size of the n-gram.
     */
    public int size() {
        return ngram.size();
    }

    /**
     * Overriding equals to compare NGrams by their content.
     *
     * @param o the object to compare.
     * @return true if the object is an NGram with the same content; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NGram)) {
            return false;
        }
        NGram other = (NGram) o;
        return this.ngram.equals(other.ngram);
    }

    /**
     * Overriding hashCode consistently with equals.
     *
     * @return the hash code for this n-gram.
     */
    @Override
    public int hashCode() {
        return Objects.hash(ngram);
    }
}
