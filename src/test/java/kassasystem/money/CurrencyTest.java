package kassasystem.money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
