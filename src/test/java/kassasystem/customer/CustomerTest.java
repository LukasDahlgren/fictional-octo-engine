package kassasystem.customer;

import kassasystem.payment.FakePayer;
import kassasystem.Purchase;
import kassasystem.CashRegister;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerTest {

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer("C001", "Test Customer");
    }

    @Test
    @DisplayName("Customer Stores Completed Purchase")
    void customerStoresCompletedPurchase() {
        CashRegister register = new CashRegister(Clock.systemUTC(), new FakePayer(true));
        Purchase purchase = register.startPurchase();
        ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        purchase.getBasket().addProduct(milk);
        register.completePurchase();

        customer.addPurchase(purchase);

        assertEquals(1, customer.getPurchaseHistory().size());
        assertSame(purchase, customer.getPurchaseHistory().get(0));
    }

    @Test
    @DisplayName("Customer Has Customer Number And Name")
    void customerHasCustomerNumberAndName() {
        assertEquals("C001", customer.getCustomerNumber());
        assertEquals("Test Customer", customer.getName());
    }

    @Test
    @DisplayName("Customer Has Active Membership Within Period")
    void customerHasActiveMembershipWithinPeriod() {
        Membership membership = new Membership(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
        Customer member = new Customer("C002", "Member", membership);

        boolean active = member.hasActiveMembershipOn(LocalDate.of(2026, 6, 15));

        assertTrue(active);
    }

    @Test
    @DisplayName("Customer Without Membership Has No Active Membership")
    void customerWithoutMembershipHasNoActiveMembership() {
        boolean active = customer.hasActiveMembershipOn(LocalDate.of(2026, 6, 15));

        assertFalse(active);
    }
}
