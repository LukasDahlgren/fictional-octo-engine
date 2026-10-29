package kassasystem.scanning;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EANTest {

    @Test
    @DisplayName("Accepts Valid Codes With Different Check Digits")
    public void acceptsValidCodesWithDifferentCheckDigits() {
        EAN codeWithNonZeroCheckDigit = new EAN ("4006381333931");
        EAN codeWithZeroCheckDigit = new EAN("0000000000000");

        assertEquals("4006381333931", codeWithNonZeroCheckDigit.getCode());
        assertEquals("0000000000000", codeWithZeroCheckDigit.getCode());
    }

    @Test
    @DisplayName("Rejects Null Code")
    public void rejectsNullCode() {
        assertThrows(IllegalArgumentException.class, () -> new EAN(null));
    }

    @Test
    @DisplayName("Rejects Empty Code")
    public void rejectsEmptyCode() {
        assertThrows(IllegalArgumentException.class, () -> new EAN(""));
    }

    @Test
    @DisplayName("Rejects Short Code")
    public void rejectsShortCode() {
        assertThrows(IllegalArgumentException.class, () -> new EAN("400638133393"));
    }

    @Test
    @DisplayName("Rejects Long Code")
    public void rejectsLongCode() {
        assertThrows(IllegalArgumentException.class, () -> new EAN("40063813339310"));
    }

    @Test
    @DisplayName("Rejects Letter In Code")
    public void rejectsLetterInCode() {
        assertThrows(IllegalArgumentException.class, () -> new EAN("40063813339A1"));
    }

    @Test
    @DisplayName("Rejects Punctuation In Code")
    public void rejectsPunctuationInCode() {
        assertThrows(IllegalArgumentException.class, () -> new EAN("40063813339-1"));
    }

    @Test
    @DisplayName("Rejects Whitespace In Code")
    public void rejectsWhitespaceInCode() {
        assertThrows(IllegalArgumentException.class, () -> new EAN("4006381333 931"));
    }

    @Test
    @DisplayName("Rejects Unicode Numerals")
    public void rejectsUnicodeNumerals() {
        assertThrows(IllegalArgumentException.class, () -> new EAN("４００６３８１３３３９３１"));
    }

    @Test
    @DisplayName("Rejects Incorrect Check Digit")
    public void rejectsIncorrectCheckDigit() {
        assertThrows(IllegalArgumentException.class, () -> new EAN("4006381333932"));
    }

    @Test
    @DisplayName("EANs With Same Code Are Equal")
    public void eansWithSameCodeAreEqual() {
        EAN firstEAN = new EAN("4006381333931");
        EAN secondEAN = new EAN("4006381333931");

        assertEquals(firstEAN, secondEAN);
        assertEquals(firstEAN.hashCode(), secondEAN.hashCode());
    }

    @Test
    @DisplayName("To String Returns Code")
    public void toStringReturnsCode() {
        EAN ean = new EAN("4006381333931");

        assertEquals("4006381333931", ean.toString());
    }

    @Test
    @DisplayName("Equals Returns True For Same Instance")
    void equalsReturnsTrueForSameInstance() {
        EAN ean = new EAN("4006381333931");

        assertEquals(ean, ean);
    }

    @Test
    @DisplayName("Equals Returns True For Same Code")
    void equalsReturnsTrueForSameCode() {
        EAN first = new EAN("4006381333931");
        EAN second = new EAN("4006381333931");

        assertEquals(first, second);
    }

    @Test
    @DisplayName("Equals Returns False For Different Type")
    void equalsReturnsFalseForDifferentType() {
        EAN ean = new EAN("4006381333931");

        assertNotEquals(ean, "4006381333931");
    }

    @Test
    @DisplayName("Equals Returns False For Null")
    void equalsReturnsFalseForNull() {
        EAN ean = new EAN("4006381333931");

        assertNotEquals(ean, null);
    }


}
