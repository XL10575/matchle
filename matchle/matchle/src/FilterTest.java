import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Optional;

public class FilterTest {

    // Test that creating a Filter from a null predicate fails.
    @Test(expected = NullPointerException.class)
    public void testFilterFromNullPredicate() {
        Filter.from(null);
    }
    
    // Test that test() throws when passed a null NGram.
    @Test(expected = NullPointerException.class)
    public void testTestNullNGram() {
        Filter f = Filter.from(nGram -> true);
        f.test(null);
    }
    
    // Test that and(Optional.empty()) returns the original filter.
    @Test
    public void testAndEmptyOptional() {
        Filter f = Filter.from(nGram -> nGram.size() > 0);
        Filter combined = f.and(Optional.empty());
        NGram n = NGram.from("hello");
        assertTrue(combined.test(n));
    }
    
    // Test that and(null) (for Optional version) throws.
    @Test(expected = NullPointerException.class)
    public void testAndNullOptional() {
        Filter f = Filter.from(nGram -> true);
        f.and((Optional<Filter>) null);
    }
    
    // Test that and(Filter other) correctly performs logical AND.
    @Test
    public void testAndWithFilter() {
        Filter f1 = Filter.from(nGram -> nGram.size() == 5);
        Filter f2 = Filter.from(nGram -> nGram.contains('a'));
        Filter combined = f1.and(f2);
        NGram n1 = NGram.from("apple");  // size 5, contains 'a'
        NGram n2 = NGram.from("hello");  // size 5, no 'a'
        assertTrue(combined.test(n1));
        assertFalse(combined.test(n2));
    }
    
    // Test that the constant Filter.FALSE always returns false.
    @Test
    public void testFilterFalse() {
        Filter falseFilter = Filter.FALSE;
        NGram n = NGram.from("anything");
        assertFalse(falseFilter.test(n));
    }
}
