package kassasystem.payment;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    @Test
    void successfulPaymentCompletesPurchase() {
        // Arrange
        Payer payer = amount -> true;
        Money amount = new Money(Sek.SEK, 100.0);
        Payment payment = new Payment(amount, payer);

        // Act
        boolean result = payment.pay();

        // Assert
        assertTrue(result);
    }
}

