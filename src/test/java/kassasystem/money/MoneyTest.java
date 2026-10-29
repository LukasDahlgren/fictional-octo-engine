package kassasystem.money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    private final Currency sek = Sek.SEK;

    @Test
    void shouldCreateMoneyWithCorrectAmount() {
        Money money = new Money(sek, 100.0);

        assertEquals(100.0, money.getAmount(), 0.001);
    }

    @Test
    void shouldCreateMoneyWithCorrectCurrency() {
        Money money = new Money(sek, 100.0);

        assertEquals(sek, money.getCurrency());
    }

    @Test
    void shouldHandleZeroAmount() {
        Money money = new Money(sek, 0.0);

        assertEquals(0.0, money.getAmount(), 0.001);
    }

    @Test
    void shouldHandleDecimalAmount() {
        Money money = new Money(sek, 99.95);

        assertEquals(99.95, money.getAmount(), 0.001);
    }
    @Test
    void shouldAddTwoMoneyAmounts() {
        Money first = new Money(Sek.SEK, 100.0);
        Money second = new Money(Sek.SEK, 50.0);

        Money result = first.add(second);

        assertEquals(150.0, result.getAmount(), 0.001);
        assertEquals(Sek.SEK, result.getCurrency());
    }

    @Test
    void shouldSubtractTwoMoneyAmounts() {
        Money first = new Money(Sek.SEK, 100.0);
        Money second = new Money(Sek.SEK, 30.0);

        Money result = first.subtract(second);

        assertEquals(70.0, result.getAmount(), 0.001);
        assertEquals(Sek.SEK, result.getCurrency());
    }

    @Test
    void shouldMultiplyMoneyByAnInteger() {
        Money money = new Money(Sek.SEK, 25.0);

        Money result = money.multiply(3);

        assertEquals(75.0, result.getAmount(), 0.001);
        assertEquals(Sek.SEK, result.getCurrency());
    }

    @ParameterizedTest(name = "Factor {0}")
    @ValueSource(ints = { -1, -2, Integer.MIN_VALUE })
    @DisplayName("Multiply Rejects Negative Factors")
    void multiplyRejectsNegativeFactors(int factor) {
        Money money = new Money(Sek.SEK, 25.0);

        assertThrows(IllegalArgumentException.class, () -> money.multiply(factor));
    }

    @Test
    void shouldMultiplyMoneyByZero() {
        Money money = new Money(Sek.SEK, 25.0);

        Money result = money.multiply(0);

        assertEquals(0.0, result.getAmount(), 0.001);
    }

    @Test
    void shouldCompareEqualAmounts() {
        Money first = new Money(Sek.SEK, 100.0);
        Money second = new Money(Sek.SEK, 100.0);

        assertTrue(first.equals(second));
    }

    @Test
    void shouldCompareDifferentAmountsAsNotEqual() {
        Money first = new Money(Sek.SEK, 100.0);
        Money second = new Money(Sek.SEK, 50.0);

        assertFalse(first.equals(second));
    }

    @Test
    void shouldIdentifyGreaterAmount() {
        Money first = new Money(Sek.SEK, 100.0);
        Money second = new Money(Sek.SEK, 50.0);

        assertTrue(first.isGreaterThan(second));
    }

    @Test
    void shouldIdentifySmallerAmount() {
        Money first = new Money(Sek.SEK, 50.0);
        Money second = new Money(Sek.SEK, 100.0);

        assertTrue(first.isLessThan(second));
    }

    @Test
    void shouldIdentifyEqualAmountsWhenComparing() {
        Money first = new Money(Sek.SEK, 100.0);
        Money second = new Money(Sek.SEK, 100.0);

        assertFalse(first.isGreaterThan(second));
        assertFalse(first.isLessThan(second));
    }

    @Test
    @DisplayName("Add Rejects Different Currencies")
    void addRejectsDifferentCurrencies() {
        Money first = new Money(Sek.SEK, 100.0);
        Money second = new Money(Eur.EUR, 50.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> first.add(second)
        );

        assertEquals(new Money(Sek.SEK, 100.0), first);
        assertEquals(new Money(Eur.EUR, 50.0), second);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"100 SEK"})
    @DisplayName("Equals Returns False For Null Or Other Type")
    void equalsReturnsFalseForNullOrOtherType(String other) {
        Money money = new Money(Sek.SEK, 100.0);

        assertFalse(money.equals(other));
    }

    @Test
    @DisplayName("Equal Money Objects Have Same Hash Code")
    void equalMoneyObjectsHaveSameHashCode() {
        Money first = new Money(Sek.SEK, 100.0);
        Money second = new Money(Sek.SEK, 100.0);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    @DisplayName("Add Handles Non-Finite Amounts")
    void addHandlesNonFiniteAmounts() {
        Money nonFinite = new Money(Sek.SEK, Double.NaN);
        Money finite = new Money(Sek.SEK, 2.0);

        Money nonFiniteFirst = nonFinite.add(finite);
        Money nonFiniteSecond = finite.add(nonFinite);

        assertTrue(Double.isNaN(nonFiniteFirst.getAmount()));
        assertTrue(Double.isNaN(nonFiniteSecond.getAmount()));
    }

    @Test
    @DisplayName("Subtract Handles Non-Finite Amounts")
    void subtractHandlesNonFiniteAmounts() {
        Money nonFinite = new Money(Sek.SEK, Double.NaN);
        Money finite = new Money(Sek.SEK, 2.0);

        Money nonFiniteFirst = nonFinite.subtract(finite);
        Money nonFiniteSecond = finite.subtract(nonFinite);

        assertTrue(Double.isNaN(nonFiniteFirst.getAmount()));
        assertTrue(Double.isNaN(nonFiniteSecond.getAmount()));
    }

    @Test
    @DisplayName("Multiply Handles Non-Finite Amounts")
    void multiplyHandlesNonFiniteAmounts() {
        Money infinite = new Money(Sek.SEK, Double.POSITIVE_INFINITY);

        Money result = infinite.multiply(2);

        assertEquals(Double.POSITIVE_INFINITY, result.getAmount());
    }

    @Test
    @DisplayName("Comparisons Handle Non-Finite Amounts")
    void comparisonsHandleNonFiniteAmounts() {
        Money infinite = new Money(Sek.SEK, Double.POSITIVE_INFINITY);
        Money finite = new Money(Sek.SEK, 2.0);

        assertTrue(infinite.isGreaterThan(finite));
        assertFalse(finite.isGreaterThan(infinite));
        assertFalse(infinite.isLessThan(finite));
        assertTrue(finite.isLessThan(infinite));
    }

    @Test
    @DisplayName("Equals Handles Non-Finite Amounts And Different Currencies")
    void equalsHandlesNonFiniteAmountsAndDifferentCurrencies() {
        Money firstInfinite = new Money(Sek.SEK, Double.POSITIVE_INFINITY);
        Money secondInfinite = new Money(Sek.SEK, Double.POSITIVE_INFINITY);
        Money notANumber = new Money(Sek.SEK, Double.NaN);
        Money finite = new Money(Sek.SEK, 2.0);
        Money otherCurrency = new Money(Eur.EUR, Double.POSITIVE_INFINITY);

        assertEquals(firstInfinite, secondInfinite);
        assertFalse(firstInfinite.equals(notANumber));
        assertFalse(firstInfinite.equals(finite));
        assertFalse(firstInfinite.equals(otherCurrency));
        assertFalse(finite.equals(firstInfinite));
    }

    @Test
    @DisplayName("Equal Non-Finite Money Objects Have The Same Hash Code")
    void equalNonFiniteMoneyObjectsHaveTheSameHashCode() {
        Money first = new Money(Sek.SEK, Double.POSITIVE_INFINITY);
        Money second = new Money(Sek.SEK, Double.POSITIVE_INFINITY);

        assertEquals(first.hashCode(), second.hashCode());
    }

}

