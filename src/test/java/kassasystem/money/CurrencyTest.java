package kassasystem.money;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CurrencyTest {

    @Test
    @DisplayName("Get Zero Money Returns Zero With Correct Currency")
    void getZeroMoneyReturnsZeroWithCorrectCurrency() {
        Currency currency = Sek.SEK;

        Money result = currency.getZeroMoney();

        assertEquals(0, new BigDecimal("0.0").compareTo(result.getAmount()));
        assertSame(currency, result.getCurrency());
    }

    @ParameterizedTest(name = "{0} compared with {1}")
    @CsvSource({
            "EUR, SEK, -1",
            "SEK, EUR, 1",
            "SEK, SEK, 0"
    })
    @DisplayName("Compare To Orders Currencies By Name")
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
