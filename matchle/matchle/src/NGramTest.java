import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class NGramTest {

    // ------------------------------------------
    // Test: from(String)
    // ------------------------------------------

    @Test
    public void testFromStringNormal() {
        NGram n = NGram.from("test");
        assertEquals("NGram length should match the string length", 4, n.size());
        assertEquals("First character should be 't'", 't', (char) n.get(0));
        assertEquals("Second character should be 'e'", 'e', (char) n.get(1));
    }

    @Test(expected = NullPointerException.class)
    public void testFromStringNull() {
        NGram.from((String) null);
    }

    @Test
    public void testFromStringEmpty() {
        // Checking how the code handles an empty string: NGram of length 0
        NGram n = NGram.from("");
        assertEquals("NGram should have length 0 for empty string", 0, n.size());
    }

    // ------------------------------------------
    // Test: from(List<Character>)
    // ------------------------------------------

    @Test
    public void testFromListNormal() {
        List<Character> chars = Arrays.asList('a', 'b', 'c');
        NGram n = NGram.from(chars);
        assertEquals("NGram should have length 3", 3, n.size());
        assertEquals("First character should be 'a'", 'a', (char) n.get(0));
    }

    @Test(expected = NullPointerException.class)
    public void testFromListNullList() {
        NGram.from((List<Character>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromListContainsNullChar() {
        // A list with a null character => validate(...) should throw.
        List<Character> chars = new ArrayList<>();
        chars.add('a');
        chars.add(null); 
        chars.add('c');
        NGram.from(chars);
    }

    @Test
    public void testFromListEmpty() {
        NGram n = NGram.from(new ArrayList<Character>());
        assertEquals("NGram should have length 0 for empty list", 0, n.size());
    }

    // ------------------------------------------
    // Test: get(int)
    // ------------------------------------------

    @Test
    public void testGetValidIndex() {
        NGram n = NGram.from("abc");
        assertEquals("Should return 'b' at index 1", 'b', (char) n.get(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetIndexOutOfBounds() {
        NGram n = NGram.from("abc");
        n.get(3); // out of bounds, since valid indices are 0..2
    }

    // ------------------------------------------
    // Test: matches(IndexedCharacter)
    // ------------------------------------------

    @Test
    public void testMatchesValid() {
        NGram n = NGram.from("cat");
        NGram.IndexedCharacter ic = new NGram.IndexedCharacter(1, 'a'); // 'a' at index 1
        assertTrue("Should match 'a' at index 1", n.matches(ic));
    }

    @Test
    public void testMatchesInvalidIndex() {
        NGram n = NGram.from("dog");
        NGram.IndexedCharacter ic = new NGram.IndexedCharacter(5, 'x'); 
        // index 5 is out of range => matches should be false
        assertFalse(n.matches(ic));
    }

    @Test
    public void testMatchesDifferentChar() {
        NGram n = NGram.from("fox");
        NGram.IndexedCharacter ic = new NGram.IndexedCharacter(0, 'f');
        assertTrue("Should match 'f' at index 0", n.matches(ic));

        // Now mismatch
        NGram.IndexedCharacter ic2 = new NGram.IndexedCharacter(0, 'x');
        assertFalse("Should not match 'x' at index 0", n.matches(ic2));
    }

    // ------------------------------------------
    // Test: contains(char)
    // ------------------------------------------

    @Test
    public void testContainsChar() {
        NGram n = NGram.from("hello");
        assertTrue("Should contain 'h'", n.contains('h'));
        assertFalse("Should not contain 'z'", n.contains('z'));
    }

    // ------------------------------------------
    // Test: containsElsewhere(IndexedCharacter)
    // ------------------------------------------

    @Test
    public void testContainsElsewhereDifferentIndex() {
        // "hello" => 'l' occurs at indexes 2 and 3
        NGram n = NGram.from("hello");
        // check 'l' at index 2 => exact match => we expect elseWhere to be true if 'l' also appears at another index
        NGram.IndexedCharacter ic = new NGram.IndexedCharacter(2, 'l');
        // 'l' is present at index 3 => so it should return true
        assertTrue("Another 'l' is elsewhere", n.containsElsewhere(ic));
    }

    @Test
    public void testContainsElsewhereNoOtherIndex() {
        // "cat" => 'a' is only at index 1
        NGram n = NGram.from("cat");
        NGram.IndexedCharacter ic = new NGram.IndexedCharacter(1, 'a');
        // 'a' doesn't occur anywhere else => containsElsewhere should be false
        assertFalse(n.containsElsewhere(ic));
    }

    @Test
    public void testContainsElsewhereNotContainsAtAll() {
        NGram n = NGram.from("cat");
        NGram.IndexedCharacter ic = new NGram.IndexedCharacter(0, 'z');
        // 'z' not in "cat"
        assertFalse(n.containsElsewhere(ic));
    }

    @Test
    public void testContainsElsewhereNullIC() {
        // coverage for the null check in containsElsewhere
        NGram n = NGram.from("cat");
        try {
            n.containsElsewhere(null);
            fail("Expected NullPointerException for null IndexedCharacter");
        } catch (NullPointerException e) {
            // success
        }
    }

    // ------------------------------------------
    // Test: equals(...) and hashCode()
    // ------------------------------------------

    @Test
    public void testEqualsAndHashCode() {
        NGram n1 = NGram.from("word");
        NGram n2 = NGram.from("word");
        NGram n3 = NGram.from("wore");

        assertTrue("NGrams with same content should be equal", n1.equals(n2));
        assertEquals("Hash codes of equal NGrams should match", n1.hashCode(), n2.hashCode());
        assertFalse("Different NGrams should not be equal", n1.equals(n3));
    }

    @Test
    public void testEqualsSameObject() {
        NGram n = NGram.from("same");
        assertTrue("Should be equal to itself", n.equals(n));
    }

    @Test
    public void testEqualsNullOrDifferentClass() {
        NGram n = NGram.from("test");
        assertFalse("NGram is not equal to null", n.equals(null));
        assertFalse("NGram is not equal to different class object", n.equals("string"));
    }

    // ------------------------------------------
    // Test iteration() (iterator) and stream()
    // ------------------------------------------

    @Test
    public void testIteratorAndStream() {
        NGram n = NGram.from("abc");

        // Iterator
        Iterator<NGram.IndexedCharacter> it = n.iterator();
        int index = 0;
        while(it.hasNext()) {
            NGram.IndexedCharacter ic = it.next();
            assertEquals(index, ic.index());
            index++;
        }
        assertEquals("We should have iterated through 3 characters", 3, index);

        // Stream
        long count = n.stream().count();
        assertEquals("Stream count should also be 3", 3, count);
    }

    // ------------------------------------------
    // Additional edge-cases
    // ------------------------------------------

    @Test
    public void testValidateStaticMethod() {
        // test validate() with an entire list of non-null characters
        List<Character> valid = Arrays.asList('x', 'y', 'z');
        assertNotNull("Validate should return the same list reference", NGram.validate(valid));
    }

    @Test(expected = NullPointerException.class)
    public void testValidateStaticMethodNullList() {
        NGram.validate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateStaticMethodNullCharacter() {
        List<Character> invalid = new ArrayList<>();
        invalid.add('a');
        invalid.add(null);
        NGram.validate(invalid);
    }
}
