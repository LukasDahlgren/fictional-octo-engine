package kassasystem.discount;

import kassasystem.customer.Customer;
import kassasystem.customer.Membership;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DiscountCalculatorTest {

    private Money originalPrice;
    private Customer customerWithoutMembership;
    private Customer customerWithMembership;
    private DiscountCalculator calculator;
    private LocalDate purchaseDate;
    private LocalDate offerStartDate;
    private LocalDate offerEndDate;
    private DiscountOffer twentyPercentOffer;

    @BeforeEach
    void setUp() {
        originalPrice = new Money(Sek.SEK, 100.0);
        customerWithoutMembership =
                new Customer("C001", "Test Customer");

        Membership membership = new Membership(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );

        customerWithMembership =
                new Customer("C002", "Member", membership);

        calculator = new DiscountCalculator();
        purchaseDate = LocalDate.of(2026, 10, 15);
        offerStartDate = LocalDate.of(2026, 10, 1);
        offerEndDate = LocalDate.of(2026, 10, 31);

        twentyPercentOffer = new DiscountOffer(
                20.0,
                offerStartDate,
                offerEndDate
        );
    }

    @Test
    @DisplayName("Customer Without Membership Or Offer Pays Original Price")
    void customerWithoutMembershipOrOfferPaysOriginalPrice() {
        Money finalPrice = calculator.calculatePrice(
                originalPrice,
                customerWithoutMembership,
                purchaseDate
        );

        assertEquals(100.0, finalPrice.getAmount(), 0.001);
        assertSame(
                originalPrice.getCurrency(),
                finalPrice.getCurrency()
        );
    }

    @Test
    @DisplayName("Customer With Active Membership Receives Ten Percent Discount")
    void customerWithActiveMembershipReceivesTenPercentDiscount() {
        Money finalPrice = calculator.calculatePrice(
                originalPrice,
                customerWithMembership,
                purchaseDate
        );

        assertEquals(90.0, finalPrice.getAmount(), 0.001);
        assertSame(
                originalPrice.getCurrency(),
                finalPrice.getCurrency()
        );
    }

    @Test
    @DisplayName("Active Offer Discount Is Applied To Non Member")
    void activeOfferDiscountIsAppliedToNonMember() {
        Money finalPrice = calculator.calculatePrice(
                originalPrice,
                customerWithoutMembership,
                purchaseDate,
                twentyPercentOffer
        );

        assertEquals(80.0, finalPrice.getAmount(), 0.001);
        assertSame(
                originalPrice.getCurrency(),
                finalPrice.getCurrency()
        );
    }

    @Test
    @DisplayName("Membership Discount Is Selected When It Is Greater")
    void membershipDiscountIsSelectedWhenItIsGreater() {
        DiscountOffer fivePercentOffer = new DiscountOffer(
                5.0,
                offerStartDate,
                offerEndDate
        );

        Money finalPrice = calculator.calculatePrice(
                originalPrice,
                customerWithMembership,
                purchaseDate,
                fivePercentOffer
        );

        assertEquals(90.0, finalPrice.getAmount(), 0.001);
    }

    @Test
    @DisplayName("Offer Discount Is Selected When It Is Greater")
    void offerDiscountIsSelectedWhenItIsGreater() {
        Money finalPrice = calculator.calculatePrice(
                originalPrice,
                customerWithMembership,
                purchaseDate,
                twentyPercentOffer
        );

        assertEquals(80.0, finalPrice.getAmount(), 0.001);
    }

    @Test
    @DisplayName("Expired Offer Does Not Give Discount")
    void expiredOfferDoesNotGiveDiscount() {
        LocalDate dayAfterOffer = LocalDate.of(2026, 11, 1);

        Money finalPrice = calculator.calculatePrice(
                originalPrice,
                customerWithoutMembership,
                dayAfterOffer,
                twentyPercentOffer
        );

        assertEquals(100.0, finalPrice.getAmount(), 0.001);
        assertSame(
                originalPrice.getCurrency(),
                finalPrice.getCurrency()
        );
    }
}