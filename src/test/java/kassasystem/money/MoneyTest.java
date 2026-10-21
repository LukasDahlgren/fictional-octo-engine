package kassasystem.money;

import org.junit.jupiter.api.Test;
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
}

