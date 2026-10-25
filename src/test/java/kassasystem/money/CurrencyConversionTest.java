package kassasystem.money;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CurrencyConversionTest {

     private final Currency sek = Sek.SEK;
     private final Currency eur = Eur.EUR;

     @Test
     void euroToSekShouldBeCorrect() {
         CurrencyConversion conversion = new CurrencyConversion(10.0, 0.02);
         Money amount = new Money(Eur.EUR, 100.0);

         Money result = conversion.convertToSek(amount);

         assertEquals(Sek.SEK, result.getCurrency());
         assertEquals(1030.0, result.getAmount(), 0.001);
     }
}
