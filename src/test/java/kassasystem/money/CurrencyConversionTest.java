package kassasystem.money;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyConversionTest {

    @Test
    void euroToSekShouldBeCorrect() {
        CurrencyConversion conversion = conversion("10", "0.02");
        Money amount = new Money(Eur.EUR, new BigDecimal("100.00"));

        Money result = conversion.convertToSek(amount);

        assertSame(Sek.SEK, result.getCurrency());
        assertEquals(new BigDecimal("1030.00"), result.getAmount());
    }

    @Test
    void conversionWithSameCurrencyReturnsSameMoney() {
        CurrencyConversion conversion = conversion("10", "0.02");
        Money amount = new Money(Sek.SEK, new BigDecimal("100.00"));

        assertSame(amount, conversion.convertToSek(amount));
    }

    @Test
    void rejectZeroExchangeRate() {
        assertThrows(IllegalArgumentException.class,
                () -> conversion("0", "0.02"));
    }

    @Test
    void rejectNegativeExchangeRate() {
        assertThrows(IllegalArgumentException.class,
                () -> conversion("-1", "0.02"));
    }

    @Test
    void rejectNegativeConversionFee() {
        assertThrows(IllegalArgumentException.class,
                () -> conversion("10", "-0.02"));
    }

    @Test
    void rejectZeroConversionFee() {
        assertThrows(IllegalArgumentException.class,
                () -> conversion("10", "0"));
    }

    @Test
    void rejectNullExchangeRateOrFee() {
        assertThrows(NullPointerException.class,
                () -> new CurrencyConversion(null, new BigDecimal("0.02")));
        assertThrows(NullPointerException.class,
                () -> new CurrencyConversion(new BigDecimal("10"), null));
    }

    @Test
    void rejectNullAsAmount() {
        CurrencyConversion conversion = conversion("10", "0.02");

        assertThrows(NullPointerException.class, () -> conversion.convertToSek(null));
    }

    @Test
    void rejectUnknownCurrency() {
        Currency unsupportedCurrency = new Currency("USD", "Usd") {};
        CurrencyConversion conversion = conversion("10", "0.02");
        Money amount = new Money(unsupportedCurrency, new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class,
                () -> conversion.convertToSek(amount));
    }

    @Test
    @DisplayName("Conversion Rounds To Two Decimal Places")
    void conversionRoundsToTwoDecimalPlaces() {
        CurrencyConversion conversion = conversion("10.123", "0.02");
        Money amount = new Money(Eur.EUR, new BigDecimal("1.00"));

        Money result = conversion.convertToSek(amount);

        assertEquals(new BigDecimal("20.33"), result.getAmount());
    }

    @Test
    @DisplayName("Conversion Handles Amounts Beyond Double Range")
    void conversionHandlesLargeAmounts() {
        CurrencyConversion conversion = conversion("10", "0.02");
        Money amount = new Money(Eur.EUR, new BigDecimal("1E+400"));

        Money result = conversion.convertToSek(amount);

        assertEquals(new BigDecimal("1E+400").multiply(new BigDecimal("10.2"))
                .add(new BigDecimal("10.00")).setScale(2), result.getAmount());
    }

    private CurrencyConversion conversion(String rate, String fee) {
        return new CurrencyConversion(new BigDecimal(rate), new BigDecimal(fee));
    }
}
