package kassasystem.money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CurrencyConversionTest {

     @Test
     void euroToSekShouldBeCorrect() {
         CurrencyConversion conversion = new CurrencyConversion(10.0, 0.02);
         Money amount = new Money(Eur.EUR, 100.0);

         Money result = conversion.convertToSek(amount);

         assertEquals(Sek.SEK, result.getCurrency());
         assertEquals(1030.0, result.getAmount(), 0.001);
     }

     @Test
    void conversionWithSameCurrency() {
         CurrencyConversion conversion = new CurrencyConversion(10.0, 0.02);
         Money amount = new Money(Sek.SEK, 100.0);

         Money result = conversion.convertToSek(amount);

         assertSame(amount, result);
     }

     @Test
    void rejectZeroExchangeRate() {
         assertThrows(IllegalArgumentException.class, () -> new CurrencyConversion(0, 0.02));
     }

     @Test
    void rejectNegativeExchangeRate() {
         assertThrows(IllegalArgumentException.class, () -> new CurrencyConversion(-1, 0.02));
     }

     @Test
    void rejectNegativeConversionFee() {
         assertThrows(IllegalArgumentException.class, () -> new CurrencyConversion(10.0, -0.02));
     }

     @Test
    void rejectZeroConversionFee() {
         assertThrows(IllegalArgumentException.class, () -> new CurrencyConversion(10, 0));
     }

     @Test
    void rejectNullAsAmount() {
         CurrencyConversion conversion = new CurrencyConversion(10.0, 0.02);

         assertThrows(NullPointerException.class, () -> conversion.convertToSek(null));

     }

     @Test
    void rejectUnknownCurrency() {
         Currency unsupportedCurrency = new Currency("USD", "Usd") {};

         CurrencyConversion conversion = new CurrencyConversion(10.0, 0.02);
         Money amount = new Money(unsupportedCurrency, 100.0);

         assertThrows(IllegalArgumentException.class,() -> conversion.convertToSek(amount));
     }

    @Test
    @DisplayName("Valutaväxlingen avrundar till två decimaler")
    void conversionRoundsToTwoDecimalPlaces() {
        CurrencyConversion conversion =
                new CurrencyConversion(10.123, 0.02);
        Money amount = new Money(Eur.EUR, 1.0);

        Money result = conversion.convertToSek(amount);

        assertEquals(new Money(Sek.SEK, 20.33), result);
    }

    @Test
    @DisplayName("Valutaväxlingen avvisar NaN som valutakurs")
    void constructorRejectsNaNExchangeRate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CurrencyConversion(Double.NaN, 0.02)
        );
    }

}
