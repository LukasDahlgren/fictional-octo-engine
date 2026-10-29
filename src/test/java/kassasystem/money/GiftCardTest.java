
package kassasystem.money;

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
}

