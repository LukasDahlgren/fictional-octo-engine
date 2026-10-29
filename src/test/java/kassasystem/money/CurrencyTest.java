package kassasystem.money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CurrencyTest {

    @Test
    @DisplayName("Nollbeloppet har värdet noll och rätt valuta")
    void getZeroMoneyReturnsZeroWithCorrectCurrency() {
        Currency currency = Sek.SEK;

        Money result = currency.getZeroMoney();

        assertEquals(0.0, result.getAmount());
        assertSame(currency, result.getCurrency());
    }

    @ParameterizedTest(name = "{0} jämfört med {1}")
    @CsvSource({
            "EUR, SEK, -1",
            "SEK, EUR, 1",
            "SEK, SEK, 0"
    })
    @DisplayName("Valutor sorteras alfabetiskt efter namn")
    void compareToOrdersCurrenciesByName(
            String firstName,
            String secondName,
            int expectedSign
    ) {
        Currency first = new Currency(firstName, "first");
        Currency second = new Currency(secondName, "second");

        int result = first.compareTo(second);

        assertEquals(expectedSign, Integer.signum(result));
    }

}
