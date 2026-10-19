package kassasystem.scanning;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EANTest {

    @Test
    @DisplayName("Accepts valid EAN-13 codes with zero and nonzero check digits")
    public void acceptsValidCodesWithDifferentCheckDigits() {
        EAN codeWithNonZeroCheckDigit = new EAN ("4006381333931");
        EAN codeWithZeroCheckDigit = new EAN("0000000000000");

        assertEquals("4006381333931", codeWithNonZeroCheckDigit.getCode());
        assertEquals("0000000000000", codeWithZeroCheckDigit.getCode());
    }

    @Test
    @DisplayName("Rejects a null code")
    public void rejectsNullCode() {
        assertThrows(IllegalArgumentException.class, () -> new EAN(null));
    }

    @Test
    @DisplayName("Rejects and empty code")
    public void rejectsEmptyCode() {
        assertThrows(IllegalArgumentException.class, () -> new EAN(""));
    }

    @Test
    @DisplayName("Rejects a code shorter than 13 digits")
    public void rejectsShortCode() {
        assertThrows(IllegalArgumentException.class, () -> new EAN("400638133393"));
    }

    @Test
    @DisplayName("Rejects a code longer than 13 digits")
    public void rejectsLongCode() {
        assertThrows(IllegalArgumentException.class, () -> new EAN("40063813339310"));
    }

    @Test
    @DisplayName("Rejects a code containing a letter")
    public void rejectsLetterInCode() {
        assertThrows(IllegalArgumentException.class, () -> new EAN("40063813339A1"));
    }

    @Test
    @DisplayName("Rejects a code containing punctuation")
    public void rejectsPunctuationInCode() {
        assertThrows(IllegalArgumentException.class, () -> new EAN("40063813339-1"));
    }

    @Test
    @DisplayName("Rejects a code containing whitespace")
    public void rejectsWhitespaceInCode() {
        assertThrows(IllegalArgumentException.class, () -> new EAN("4006381333 931"));
    }

    @Test
    @DisplayName("Rejects Unicode numerals instead of ASCII digits")
    public void rejectsUnicodeNumerals() {
        assertThrows(IllegalArgumentException.class, () -> new EAN("４００６３８１３３３９３１"));
    }

    @Test
    @DisplayName("Rejects 13 digits with an incorrect check digit")
    public void rejectsIncorrectCheckDigit() {
        assertThrows(IllegalArgumentException.class, () -> new EAN("4006381333932"));
    }

    @Test
    @DisplayName("EANs with the same code are equal and have the same hash code")
    public void eansWithSameCodeAreEqual() {
        EAN firstEAN = new EAN("4006381333931");
        EAN secondEAN = new EAN("4006381333931");

        assertEquals(firstEAN, secondEAN);
        assertEquals(firstEAN.hashCode(), secondEAN.hashCode());
    }

    @Test
    @DisplayName("Returns the EAN code as a string")
    public void toStringReturnsCode() {
        EAN ean = new EAN("4006381333931");

        assertEquals("4006381333931", ean.toString());
    }


}
