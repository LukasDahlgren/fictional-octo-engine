package kassasystem.payment;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}