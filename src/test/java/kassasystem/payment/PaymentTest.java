package kassasystem.payment;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentTest {

    @Test
    @DisplayName("Betalningen lyckas när betalaren godkänner den")
    void payReturnsTrueWhenPayerApproves() {
        Money amount = new Money(Sek.SEK, 100.0);
        Payer payer = paymentAmount -> true;
        Payment payment = new Payment(amount, payer);

        boolean result = payment.pay();

        assertTrue(result);
    }
    @Test
    @DisplayName("Betalningen misslyckas när betalaren avvisar den")
    void payReturnsFalseWhenPayerRejects() {
        Money amount = new Money(Sek.SEK, 100.0);
        Payer payer = paymentAmount -> false;
        Payment payment = new Payment(amount, payer);

        boolean result = payment.pay();

        assertFalse(result);
    }
    @Test
    @DisplayName("Betalningen skickar rätt belopp till betalaren")
    void payPassesAmountToPayer() {
        Money amount = new Money(Sek.SEK, 100.0);
        Money[] receivedAmount = new Money[1];

        Payer payer = paymentAmount -> {
            receivedAmount[0] = paymentAmount;
            return true;
        };

        Payment payment = new Payment(amount, payer);

        payment.pay();

        assertEquals(amount, receivedAmount[0]);
    }

    @Test
    @DisplayName("Betalningen avvisar en saknad betalare")
    void constructorRejectsNullPayer() {
        Money amount = new Money(Sek.SEK, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> new Payment(amount, null)
        );
    }

    @Test
    @DisplayName("Betalningen avvisar ett saknat belopp")
    void constructorRejectsNullAmount() {
        Payer payer = paymentAmount -> true;

        assertThrows(
                IllegalArgumentException.class,
                () -> new Payment(null, payer)
        );
    }

    @Test
    @DisplayName("Betalningen anropar betalaren exakt en gång")
    void payCallsPayerExactlyOnce() {
        Money amount = new Money(Sek.SEK, 100.0);
        int[] callCount = new int[1];

        Payer payer = paymentAmount -> {
            callCount[0]++;
            return true;
        };

        Payment payment = new Payment(amount, payer);

        payment.pay();

        assertEquals(1, callCount[0]);
    }

    @Test
    @DisplayName("Betalningen avvisar ett negativt belopp")
    void constructorRejectsNegativeAmount() {
        Money amount = new Money(Sek.SEK, -100.0);
        Payer payer = paymentAmount -> true;

        assertThrows(
                IllegalArgumentException.class,
                () -> new Payment(amount, payer)
        );
    }

    @Test
    @DisplayName("Betalningen tillåter beloppet noll")
    void constructorAllowsZeroAmount() {
        Money amount = new Money(Sek.SEK, 0.0);
        Payer payer = paymentAmount -> true;

        assertDoesNotThrow(() -> new Payment(amount, payer));
    }

    @ParameterizedTest(name = "Belopp: {0}")
    @ValueSource(doubles = {
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY
    })
    @DisplayName("Betalningen avvisar belopp som inte är ändliga")
    void constructorRejectsNonFiniteAmount(double value) {
        Money amount = new Money(Sek.SEK, value);
        Payer payer = paymentAmount -> true;

        assertThrows(
                IllegalArgumentException.class,
                () -> new Payment(amount, payer)
        );
    }

}