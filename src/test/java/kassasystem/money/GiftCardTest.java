
package kassasystem.money;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GiftCardTest {

    @Test
    void shouldCreateGiftCardWithCorrectBalance() {
        GiftCard giftCard = new GiftCard(
                new Money(Sek.SEK, new BigDecimal("500.0"))
        );

        assertEquals(0, new BigDecimal("500.0").compareTo(giftCard.getBalance().getAmount()));
    }

    @Test
    void shouldReduceBalanceWhenAmountIsUsed() {
        GiftCard giftCard = new GiftCard(
                new Money(Sek.SEK, new BigDecimal("500.0"))
        );

        giftCard.use(new Money(Sek.SEK, new BigDecimal("150.0")));

        assertEquals(0, new BigDecimal("350.0").compareTo(giftCard.getBalance().getAmount()));
    }

    @Test
    void shouldAllowUsingTheEntireBalance() {
        GiftCard giftCard = new GiftCard(
                new Money(Sek.SEK, new BigDecimal("200.0"))
        );

        giftCard.use(new Money(Sek.SEK, new BigDecimal("200.0")));

        assertEquals(0, new BigDecimal("0.0").compareTo(giftCard.getBalance().getAmount()));
    }

    @Test
    void shouldRejectUsingMoreThanTheBalance() {
        GiftCard giftCard = new GiftCard(
                new Money(Sek.SEK, new BigDecimal("100.0"))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> giftCard.use(new Money(Sek.SEK, new BigDecimal("150.0")))
        );

        assertEquals(0, new BigDecimal("100.0").compareTo(giftCard.getBalance().getAmount()));
    }

    @Test
    void shouldRejectUsingNegativeAmount() {
        GiftCard giftCard = new GiftCard(
                new Money(Sek.SEK, new BigDecimal("100.0"))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> giftCard.use(new Money(Sek.SEK, new BigDecimal("-20.0")))
        );

        assertEquals(0, new BigDecimal("100.0").compareTo(giftCard.getBalance().getAmount()));
    }

    @Test
    @DisplayName("Use Preserves Exact Decimal Balance")
    void usePreservesExactDecimalBalance() {
        GiftCard giftCard = new GiftCard(
                new Money(Sek.SEK, new BigDecimal("0.10"))
        );

        giftCard.use(new Money(Sek.SEK, new BigDecimal("0.03")));

        assertEquals(new BigDecimal("0.07"), giftCard.getBalance().getAmount());
    }
    @Test
    @DisplayName("Use Rejects Different Currency And Preserves Balance")
    void useRejectsDifferentCurrencyAndPreservesBalance() {
        Money initialBalance = new Money(Sek.SEK, new BigDecimal("100.0"));
        GiftCard giftCard = new GiftCard(initialBalance);
        Money euroAmount = new Money(Eur.EUR, new BigDecimal("10.0"));

        assertThrows(
                IllegalArgumentException.class,
                () -> giftCard.use(euroAmount)
        );

        assertEquals(initialBalance, giftCard.getBalance());
    }
    @Test
    @DisplayName("Constructor Rejects Negative Initial Balance")
    void constructorRejectsNegativeInitialBalance() {
        Money initialBalance = new Money(Sek.SEK, new BigDecimal("-100.0"));

        assertThrows(
                IllegalArgumentException.class,
                () -> new GiftCard(initialBalance)
        );
    }
}

