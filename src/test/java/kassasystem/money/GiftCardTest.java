
package kassasystem.money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GiftCardTest {

    @Test
    void shouldCreateGiftCardWithCorrectBalance() {
        GiftCard giftCard = new GiftCard(
                new Money(Sek.SEK, 500.0)
        );

        assertEquals(500.0, giftCard.getBalance().getAmount(), 0.001);
    }

    @Test
    void shouldReduceBalanceWhenAmountIsUsed() {
        GiftCard giftCard = new GiftCard(
                new Money(Sek.SEK, 500.0)
        );

        giftCard.use(new Money(Sek.SEK, 150.0));

        assertEquals(350.0, giftCard.getBalance().getAmount(), 0.001);
    }

    @Test
    void shouldAllowUsingTheEntireBalance() {
        GiftCard giftCard = new GiftCard(
                new Money(Sek.SEK, 200.0)
        );

        giftCard.use(new Money(Sek.SEK, 200.0));

        assertEquals(0.0, giftCard.getBalance().getAmount(), 0.001);
    }

    @Test
    void shouldRejectUsingMoreThanTheBalance() {
        GiftCard giftCard = new GiftCard(
                new Money(Sek.SEK, 100.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> giftCard.use(new Money(Sek.SEK, 150.0))
        );

        assertEquals(100.0, giftCard.getBalance().getAmount(), 0.001);
    }

    @Test
    void shouldRejectUsingNegativeAmount() {
        GiftCard giftCard = new GiftCard(
                new Money(Sek.SEK, 100.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> giftCard.use(new Money(Sek.SEK, -20.0))
        );

        assertEquals(100.0, giftCard.getBalance().getAmount(), 0.001);
    }

    @Test
    @DisplayName("Presentkortet avvisar NaN utan att ändra saldot")
    void useRejectsNaNAndPreservesBalance() {
        Money initialBalance = new Money(Sek.SEK, 100.0);
        GiftCard giftCard = new GiftCard(initialBalance);
        Money invalidAmount = new Money(Sek.SEK, Double.NaN);

        assertThrows(
                IllegalArgumentException.class,
                () -> giftCard.use(invalidAmount)
        );

        assertEquals(initialBalance, giftCard.getBalance());
    }
    @Test
    @DisplayName("Presentkortet avvisar annan valuta utan att ändra saldot")
    void useRejectsDifferentCurrencyAndPreservesBalance() {
        Money initialBalance = new Money(Sek.SEK, 100.0);
        GiftCard giftCard = new GiftCard(initialBalance);
        Money euroAmount = new Money(Eur.EUR, 10.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> giftCard.use(euroAmount)
        );

        assertEquals(initialBalance, giftCard.getBalance());
    }
    @Test
    @DisplayName("Presentkortet avvisar ett negativt startsaldo")
    void constructorRejectsNegativeInitialBalance() {
        Money initialBalance = new Money(Sek.SEK, -100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> new GiftCard(initialBalance)
        );
    }
}

