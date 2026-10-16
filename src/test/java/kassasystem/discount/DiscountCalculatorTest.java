package kassasystem.discount;

import kassasystem.customer.Customer;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DiscountCalculatorTest {

    @Test
    @DisplayName("Kund utan medlemskap och erbjudande betalar ordinarie pris")
    void customerWithoutMembershipOrOfferPaysOriginalPrice() {
        Money originalPrice = new Money(Sek.SEK, 100.0);
        Customer customer = new Customer("C001", "Testkund");
        DiscountCalculator calculator = new DiscountCalculator();
        LocalDate purchaseDate = LocalDate.of(2026, 10, 16);

        Money finalPrice = calculator.calculatePrice(
                originalPrice,
                customer,
                purchaseDate
        );

        assertEquals(100.0, finalPrice.getAmount(), 0.001);
        assertSame(Sek.SEK, finalPrice.getCurrency());
    }
}