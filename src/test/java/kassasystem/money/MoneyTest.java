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
    @DisplayName("Pengar i olika valutor kan inte adderas")
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
    @DisplayName("Pengar är inte lika med null eller ett annat objektslag")
    void equalsReturnsFalseForNullOrOtherType(String other) {
        Money money = new Money(Sek.SEK, 100.0);

        assertFalse(money.equals(other));
    }

    @Test
    @DisplayName("Lika pengabelopp har samma hashkod")
    void equalMoneyObjectsHaveSameHashCode() {
        Money first = new Money(Sek.SEK, 100.0);
        Money second = new Money(Sek.SEK, 100.0);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

}

