package kassasystem.discount;

import kassasystem.customer.Customer;
import kassasystem.customer.Membership;
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
    @Test
    @DisplayName("Kund med aktivt medlemskap får tio procent rabatt")
    void customerWithActiveMembershipReceivesTenPercentDiscount() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        Membership membership = new Membership(startDate, endDate);
        Customer customer = new Customer("C002", "Medlem", membership);
        Money originalPrice = new Money(Sek.SEK, 100.0);
        DiscountCalculator calculator = new DiscountCalculator();
        LocalDate purchaseDate = LocalDate.of(2026, 10, 16);

        Money finalPrice = calculator.calculatePrice(
                originalPrice,
                customer,
                purchaseDate
        );

        assertEquals(90.0, finalPrice.getAmount(), 0.001);
        assertSame(Sek.SEK, finalPrice.getCurrency());
    }
    @Test
    @DisplayName("Aktivt erbjudande ger rabatt till kund utan medlemskap")
    void activeOfferDiscountIsAppliedToNonMember() {
        Money originalPrice = new Money(Sek.SEK, 100.0);
        Customer customer = new Customer("C001", "Testkund");
        LocalDate purchaseDate = LocalDate.of(2026, 10, 15);
        DiscountOffer offer = new DiscountOffer(
                20.0,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );
        DiscountCalculator calculator = new DiscountCalculator();

        Money finalPrice = calculator.calculatePrice(
                originalPrice,
                customer,
                purchaseDate,
                offer
        );

        assertEquals(80.0, finalPrice.getAmount(), 0.001);
        assertSame(originalPrice.getCurrency(), finalPrice.getCurrency());
    }
    @Test
    @DisplayName("Medlemsrabatten väljs när den är större än erbjudandet")
    void membershipDiscountIsSelectedWhenItIsGreater() {
        LocalDate purchaseDate = LocalDate.of(2026, 10, 15);
        Membership membership = new Membership(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
        Customer customer = new Customer("C002", "Medlem", membership);
        Money originalPrice = new Money(Sek.SEK, 100.0);
        DiscountOffer offer = new DiscountOffer(
                5.0,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );
        DiscountCalculator calculator = new DiscountCalculator();

        Money finalPrice = calculator.calculatePrice(
                originalPrice,
                customer,
                purchaseDate,
                offer
        );

        assertEquals(90.0, finalPrice.getAmount(), 0.001);
    }
    @Test
    @DisplayName("Erbjudandet väljs när det är större än medlemsrabatten")
    void offerDiscountIsSelectedWhenItIsGreater() {
        LocalDate purchaseDate = LocalDate.of(2026, 10, 15);
        Membership membership = new Membership(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
        Customer customer = new Customer("C002", "Medlem", membership);
        Money originalPrice = new Money(Sek.SEK, 100.0);
        DiscountOffer offer = new DiscountOffer(
                20.0,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );
        DiscountCalculator calculator = new DiscountCalculator();

        Money finalPrice = calculator.calculatePrice(
                originalPrice,
                customer,
                purchaseDate,
                offer
        );

        assertEquals(80.0, finalPrice.getAmount(), 0.001);
    }
}