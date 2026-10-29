package kassasystem.customer;

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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerTest {

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer("C001", "Testkund");
    }

    @Test
    @DisplayName("Kunden sparar ett genomfört köp i sin köphistorik")
    void customerStoresCompletedPurchase() {
        CashRegister register = new CashRegister();
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
    @DisplayName("Kunden har kundnummer och namn")
    void customerHasCustomerNumberAndName() {
        assertEquals("C001", customer.getCustomerNumber());
        assertEquals("Testkund", customer.getName());
    }

    @Test
    @DisplayName("Kundens medlemskap är aktivt inom giltighetsperioden")
    void customerHasActiveMembershipWithinPeriod() {
        Membership membership = new Membership(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
        Customer member = new Customer("C002", "Medlem", membership);

        boolean active = member.hasActiveMembershipOn(LocalDate.of(2026, 6, 15));

        assertTrue(active);
    }

    @Test
    @DisplayName("Kund utan medlemskap har inget aktivt medlemskap")
    void customerWithoutMembershipHasNoActiveMembership() {
        boolean active = customer.hasActiveMembershipOn(LocalDate.of(2026, 6, 15));

        assertFalse(active);
    }
}
