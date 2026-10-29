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
    @DisplayName("Pay Returns True When Payer Approves")
    void payReturnsTrueWhenPayerApproves() {
        Money amount = new Money(Sek.SEK, 100.0);
        Payer payer = paymentAmount -> true;
        Payment payment = new Payment(amount, payer);

        boolean result = payment.pay();

        assertTrue(result);
    }
    @Test
    @DisplayName("Pay Returns False When Payer Rejects")
    void payReturnsFalseWhenPayerRejects() {
        Money amount = new Money(Sek.SEK, 100.0);
        Payer payer = paymentAmount -> false;
        Payment payment = new Payment(amount, payer);

        boolean result = payment.pay();

        assertFalse(result);
    }
    @Test
    @DisplayName("Pay Passes Amount To Payer")
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
    @DisplayName("Constructor Rejects Null Payer")
    void constructorRejectsNullPayer() {
        Money amount = new Money(Sek.SEK, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> new Payment(amount, null)
        );
    }

    @Test
    @DisplayName("Constructor Rejects Null Amount")
    void constructorRejectsNullAmount() {
        Payer payer = paymentAmount -> true;

        assertThrows(
                IllegalArgumentException.class,
                () -> new Payment(null, payer)
        );
    }

    @Test
    @DisplayName("Pay Calls Payer Exactly Once")
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
    @DisplayName("Constructor Rejects Negative Amount")
    void constructorRejectsNegativeAmount() {
        Money amount = new Money(Sek.SEK, -100.0);
        Payer payer = paymentAmount -> true;

        assertThrows(
                IllegalArgumentException.class,
                () -> new Payment(amount, payer)
        );
    }

    @Test
    @DisplayName("Constructor Allows Zero Amount")
    void constructorAllowsZeroAmount() {
        Money amount = new Money(Sek.SEK, 0.0);
        Payer payer = paymentAmount -> true;

        assertDoesNotThrow(() -> new Payment(amount, payer));
    }

    @ParameterizedTest(name = "Amount: {0}")
    @ValueSource(doubles = {
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY
    })
    @DisplayName("Constructor Rejects Non Finite Amount")
    void constructorRejectsNonFiniteAmount(double value) {
        Money amount = new Money(Sek.SEK, value);
        Payer payer = paymentAmount -> true;

        assertThrows(
                IllegalArgumentException.class,
                () -> new Payment(amount, payer)
        );
    }

}