package kassasystem.money;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    private final Currency sek = Sek.SEK;

    @Test
    @DisplayName("Constructor Stores BigDecimal Amount And Currency")
    void constructorStoresAmountAndCurrency() {
        BigDecimal amount = new BigDecimal("100.25");

        Money money = new Money(sek, amount);

        assertEquals(amount, money.getAmount());
        assertSame(sek, money.getCurrency());
    }

    @Test
    @DisplayName("Constructor Preserves Decimal Precision")
    void constructorPreservesDecimalPrecision() {
        Money money = new Money(sek, new BigDecimal("0.10"));

        Money result = money.multiply(3);

        assertAmountEquals("0.30", result);
    }

    @Test
    @DisplayName("Constructor Rejects Null Amount")
    void constructorRejectsNullAmount() {
        assertThrows(NullPointerException.class, () -> new Money(sek, null));
    }

    @Test
    void shouldHandleZeroAmount() {
        Money money = new Money(sek, BigDecimal.ZERO);

        assertAmountEquals("0", money);
    }

    @Test
    void shouldAddTwoMoneyAmounts() {
        Money first = new Money(sek, new BigDecimal("100.00"));
        Money second = new Money(sek, new BigDecimal("50.25"));

        Money result = first.add(second);

        assertAmountEquals("150.25", result);
        assertSame(sek, result.getCurrency());
    }

    @Test
    void shouldSubtractTwoMoneyAmounts() {
        Money first = new Money(sek, new BigDecimal("100.00"));
        Money second = new Money(sek, new BigDecimal("30.25"));

        Money result = first.subtract(second);

        assertAmountEquals("69.75", result);
    }

    @Test
    void shouldMultiplyMoneyByAnInteger() {
        Money money = new Money(sek, new BigDecimal("25.25"));

        Money result = money.multiply(3);

        assertAmountEquals("75.75", result);
    }

    @Test
    void shouldMultiplyMoneyByZero() {
        Money money = new Money(sek, new BigDecimal("25.00"));

        Money result = money.multiply(0);

        assertAmountEquals("0.00", result);
    }

    @Test
    void shouldCompareEqualAmounts() {
        Money first = new Money(sek, new BigDecimal("100.0"));
        Money second = new Money(sek, new BigDecimal("100.00"));

        assertEquals(first, second);
    }

    @Test
    void shouldCompareDifferentAmountsAsNotEqual() {
        Money first = new Money(sek, new BigDecimal("100"));
        Money second = new Money(sek, new BigDecimal("50"));

        assertNotEquals(first, second);
    }

    @Test
    void shouldIdentifyGreaterAmount() {
        Money first = new Money(sek, new BigDecimal("100"));
        Money second = new Money(sek, new BigDecimal("50"));

        assertTrue(first.isGreaterThan(second));
    }

    @Test
    void shouldIdentifySmallerAmount() {
        Money first = new Money(sek, new BigDecimal("50"));
        Money second = new Money(sek, new BigDecimal("100"));

        assertTrue(first.isLessThan(second));
    }

    @Test
    void shouldIdentifyEqualAmountsWhenComparing() {
        Money first = new Money(sek, new BigDecimal("100.0"));
        Money second = new Money(sek, new BigDecimal("100.00"));

        assertFalse(first.isGreaterThan(second));
        assertFalse(first.isLessThan(second));
    }

    @Test
    @DisplayName("Add Rejects Different Currencies")
    void addRejectsDifferentCurrencies() {
        Money first = new Money(Sek.SEK, new BigDecimal("100.00"));
        Money second = new Money(Eur.EUR, new BigDecimal("50.00"));

        assertThrows(IllegalArgumentException.class, () -> first.add(second));
        assertAmountEquals("100.00", first);
        assertAmountEquals("50.00", second);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"100 SEK"})
    @DisplayName("Equals Returns False For Null Or Other Type")
    void equalsReturnsFalseForNullOrOtherType(String other) {
        Money money = new Money(sek, new BigDecimal("100"));

        assertFalse(money.equals(other));
    }

    @Test
    @DisplayName("Equal Money Objects Have Same Hash Code")
    void equalMoneyObjectsHaveSameHashCode() {
        Money first = new Money(sek, new BigDecimal("100.0"));
        Money second = new Money(sek, new BigDecimal("100.00"));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    @DisplayName("Money Supports Amounts Beyond Double Range")
    void supportsLargeAmountsWithoutOverflow() {
        BigDecimal largeAmount = new BigDecimal("1E+400");
        Money money = new Money(sek, largeAmount);

        assertEquals(largeAmount, money.getAmount());
        assertEquals(largeAmount.multiply(BigDecimal.valueOf(2)), money.multiply(2).getAmount());
    }

    private void assertAmountEquals(String expected, Money actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual.getAmount()));
    }
}
