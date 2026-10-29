package kassasystem;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import kassasystem.customer.Customer;
import kassasystem.customer.Membership;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;
import kassasystem.receipt.Receipt;
import kassasystem.receipt.ReceiptLine;
import kassasystem.discount.DiscountOffer;
import kassasystem.payment.Payer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CashRegisterTest {

    private final ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);

    private CashRegister register;
    private Product milk;
    private Product bread;

    @BeforeEach
    void setUp() {
        register = new CashRegister();
        milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        bread = new Product("Bread", new Money(Sek.SEK, 20.00), food);
    }

    @Test
    @DisplayName("Constructor Rejects Null Clock")
    void constructorRejectsNullClock() {
        assertThrows(IllegalArgumentException.class, () -> new CashRegister(null));
    }

    @Test
    @DisplayName("Complete Purchase Uses Supplied Clock For Receipt Timestamp")
    void completePurchaseUsesSuppliedClockForReceiptTimestamp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-20T10:15:30Z"),
                ZoneId.of("Europe/Stockholm"));
        register = new CashRegister(clock);
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(milk);

        Receipt receipt = register.completePurchase();

        LocalDateTime expectedTimestamp = LocalDateTime.of(2026, 10, 20, 12, 15, 30);
        assertEquals(expectedTimestamp, receipt.getTimestamp());
    }

    @Test
    @DisplayName("Start Purchase Creates Current Active Purchase")
    void startPurchaseCreatesCurrentActivePurchase() {
        Purchase purchase = register.startPurchase();

        assertCurrentActiveEmptyPurchase(purchase);
    }

    @Test
    @DisplayName("Start Purchase Rejects Existing Current Purchase")
    void startPurchaseRejectsExistingCurrentPurchase() {
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(milk);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.startPurchase());

        assertAll("Rejected start preserves the active purchase",
                () -> assertEquals("Cannot start a purchase while another purchase is current", exception.getMessage()),
                () -> assertSame(purchase, register.getCurrentPurchase()),
                () -> assertEquals(Purchase.Status.ACTIVE, purchase.getStatus()),
                () -> assertEquals(1, purchase.getBasket().getTotalQuantity()),
                () -> assertSame(milk, purchase.getBasket().getItems().get(0).getProduct())
        );
    }

    @Test
    @DisplayName("Complete Purchase Completes Current Purchase And Frees Register")
    void completePurchaseCompletesCurrentPurchaseAndFreesRegister() {
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(milk);

        register.completePurchase();

        assertAll("Completion frees the register and preserves the purchase contents",
                () -> assertEquals(Purchase.Status.COMPLETED, purchase.getStatus()),
                () -> assertNull(register.getCurrentPurchase()),
                () -> assertEquals(1, purchase.getBasket().getTotalQuantity()),
                () -> assertSame(milk, purchase.getBasket().getItems().get(0).getProduct())
        );
    }

    @Test
    @DisplayName("Start Purchase After Complete Purchase")
    void startPurchaseAfterCompletePurchase() {
        Purchase previousPurchase = register.startPurchase();
        previousPurchase.getBasket().addProduct(milk);
        register.completePurchase();

        Purchase nextPurchase = register.startPurchase();

        assertAll("Register starts a distinct active purchase with an empty basket",
                () -> assertNotSame(previousPurchase, nextPurchase),
                () -> assertCurrentActiveEmptyPurchase(nextPurchase)
        );
    }

    @Test
    @DisplayName("Complete Purchase Returns Receipt With Purchase Contents")
    void completePurchaseReturnsReceiptWithPurchaseContents() {
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(milk);
        purchase.getBasket().addProduct(milk);
        purchase.getBasket().addProduct(bread);

        Receipt receipt = register.completePurchase();

        assertEquals(2, receipt.getItems().size());
        assertAll("Receipt contains the purchased rows and total",
                () -> assertReceiptLine(receipt.getItems().get(0), "Milk", 2, 12.50),
                () -> assertReceiptLine(receipt.getItems().get(1), "Bread", 1, 20.00),
                () -> assertEquals(45.00, receipt.getTotalPrice().getAmount(), 0.001),
                () -> assertEquals(Sek.SEK, receipt.getTotalPrice().getCurrency())
        );
    }

    @Test
    @DisplayName("Completed Purchase Rejects Basket Changes And Preserves Receipt")
    void completedPurchaseRejectsBasketChangesAndPreservesReceipt() {
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(milk);
        Receipt receipt = register.completePurchase();

        assertThrows(IllegalStateException.class, () -> purchase.getBasket().addProduct(milk));

        assertEquals(1, receipt.getItems().size());
        assertAll("Returned receipt keeps the original purchase contents",
                () -> assertEquals(1, purchase.getBasket().getTotalQuantity()),
                () -> assertReceiptLine(receipt.getItems().get(0), "Milk", 1, 12.50),
                () -> assertEquals(12.50, receipt.getTotalPrice().getAmount(), 0.001)
        );
    }

    @Test
    @DisplayName("Complete Purchase Keeps Purchase Active When Receipt Creation Fails")
    void completePurchaseKeepsPurchaseActiveWhenReceiptCreationFails() {
        Purchase purchase = register.startPurchase();
        Product invalidProduct = new Product("Milk", new Money(Sek.SEK, -1.00), food);
        purchase.getBasket().addProduct(invalidProduct);

        assertThrows(IllegalArgumentException.class, () -> register.completePurchase());

        assertAll("Receipt creation failure preserves the active purchase",
                () -> assertSame(purchase, register.getCurrentPurchase()),
                () -> assertEquals(Purchase.Status.ACTIVE, purchase.getStatus()),
                () -> assertEquals(1, purchase.getBasket().getTotalQuantity()),
                () -> assertSame(invalidProduct, purchase.getBasket().getItems().get(0).getProduct())
        );
    }

    @Test
    @DisplayName("Complete Purchase Rejects Missing Current Purchase")
    void completePurchaseRejectsMissingCurrentPurchase() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.completePurchase());

        assertAll("Completion is rejected while the register stays free",
                () -> assertEquals("Cannot complete a purchase when there is no current purchase", exception.getMessage()),
                () -> assertNull(register.getCurrentPurchase())
        );
    }

    @Test
    @DisplayName("Complete Purchase Rejects Empty Basket And Preserves Active Purchase")
    void completePurchaseRejectsEmptyBasket() {
        Purchase purchase = register.startPurchase();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.completePurchase());

        assertAll("Rejected checkout preserves the empty active purchase",
                () -> assertEquals("Cannot complete a purchase with no products in the basket", exception.getMessage()),
                () -> assertCurrentActiveEmptyPurchase(purchase)
        );
    }

    @Test
    @DisplayName("Customer Checkout Rejects Empty Basket And Preserves Purchase And History")
    void completeCustomerPurchaseRejectsEmptyBasket() {
        Customer customer = new Customer("C001", "Testkund");
        Purchase previousPurchase = register.startPurchase();
        previousPurchase.getBasket().addProduct(milk);
        register.completePurchase(customer);
        Purchase purchase = register.startPurchase();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.completePurchase(customer));

        assertAll("Rejected checkout preserves the empty active purchase and existing history",
                () -> assertEquals("Cannot complete a purchase with no products in the basket", exception.getMessage()),
                () -> assertCurrentActiveEmptyPurchase(purchase),
                () -> assertEquals(List.of(previousPurchase), customer.getPurchaseHistory())
        );
    }

    @Test
    @DisplayName("Customer Checkout With Offer Rejects Empty Basket And Preserves Purchase And History")
    void completeCustomerPurchaseWithOfferRejectsEmptyBasket() {
        Customer customer = new Customer("C001", "Testkund");
        Purchase previousPurchase = register.startPurchase();
        previousPurchase.getBasket().addProduct(milk);
        register.completePurchase(customer);
        Purchase purchase = register.startPurchase();
        DiscountOffer offer = new DiscountOffer(20,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.completePurchase(customer, offer));

        assertAll("Rejected checkout preserves the empty active purchase and existing history",
                () -> assertEquals("Cannot complete a purchase with no products in the basket", exception.getMessage()),
                () -> assertCurrentActiveEmptyPurchase(purchase),
                () -> assertEquals(List.of(previousPurchase), customer.getPurchaseHistory())
        );
    }

    @Test
    @DisplayName("Complete Purchase Rejects Previously Cancelled Purchase")
    void completePurchaseRejectsPreviouslyCancelledPurchase() {
        Purchase purchase = register.startPurchase();
        register.cancelPurchase();

        assertThrows(IllegalStateException.class, () -> register.completePurchase());

        assertAll("Rejected completion preserves the cancelled purchase and free register",
                () -> assertNull(register.getCurrentPurchase()),
                () -> assertEquals(Purchase.Status.CANCELLED, purchase.getStatus())
        );
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = Purchase.Status.class, names = { "CANCELLED", "COMPLETED" })
    @DisplayName("Complete Purchase Rejects Inactive Current Purchase")
    void completePurchaseRejectsInactiveCurrentPurchase(Purchase.Status status) {
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(milk);
        if (status == Purchase.Status.CANCELLED) {
            purchase.cancel();
        } else {
            purchase.complete();
        }

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.completePurchase());

        assertAll("Rejected completion preserves the current purchase",
                () -> assertEquals("Only active purchases can be completed", exception.getMessage()),
                () -> assertSame(purchase, register.getCurrentPurchase()),
                () -> assertEquals(status, purchase.getStatus()),
                () -> assertEquals(new Money(Sek.SEK, 12.50), purchase.getBasket().getTotalPrice())
        );
    }

    @Test
    @DisplayName("Cancel Purchase Cancels Current Purchase And Frees Register")
    void cancelPurchaseCancelsCurrentPurchaseAndFreesRegister() {
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(milk);

        register.cancelPurchase();

        assertAll("Cancellation frees the register and preserves the purchase contents",
                () -> assertEquals(Purchase.Status.CANCELLED, purchase.getStatus()),
                () -> assertNull(register.getCurrentPurchase()),
                () -> assertEquals(1, purchase.getBasket().getTotalQuantity()),
                () -> assertSame(milk, purchase.getBasket().getItems().get(0).getProduct())
        );
    }

    @Test
    @DisplayName("Start Purchase After Cancel Purchase")
    void startPurchaseAfterCancelPurchase() {
        Purchase previousPurchase = register.startPurchase();
        previousPurchase.getBasket().addProduct(milk);
        register.cancelPurchase();

        Purchase nextPurchase = register.startPurchase();

        assertAll("Register starts a distinct active purchase with an empty basket",
                () -> assertNotSame(previousPurchase, nextPurchase),
                () -> assertCurrentActiveEmptyPurchase(nextPurchase)
        );
    }

    @Test
    @DisplayName("Cancel Purchase Rejects Missing Current Purchase")
    void cancelPurchaseRejectsMissingCurrentPurchase() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.cancelPurchase());

        assertAll("Cancellation is rejected while the register stays free",
                () -> assertEquals("Cannot cancel a purchase when there is no current purchase", exception.getMessage()),
                () -> assertNull(register.getCurrentPurchase())
        );
    }

    @Test
    @DisplayName("Cancel Purchase Rejects Previously Completed Purchase")
    void cancelPurchaseRejectsPreviouslyCompletedPurchase() {
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(milk);
        register.completePurchase();

        assertThrows(IllegalStateException.class, () -> register.cancelPurchase());

        assertAll("Rejected cancellation preserves the completed purchase and free register",
                () -> assertNull(register.getCurrentPurchase()),
                () -> assertEquals(Purchase.Status.COMPLETED, purchase.getStatus())
        );
    }

    @Test
    @DisplayName("Complete Purchase Rejects Null Customer")
    void completePurchaseRejectsNullCustomer() {
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(milk);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> register.completePurchase(null));

        assertAll("Rejected checkout preserves the active purchase",
                () -> assertEquals("Customer must not be null", exception.getMessage()),
                () -> assertSame(purchase, register.getCurrentPurchase()),
                () -> assertEquals(Purchase.Status.ACTIVE, purchase.getStatus()),
                () -> assertEquals(new Money(Sek.SEK, 12.50), purchase.getBasket().getTotalPrice())
        );
    }

    @Test
    @DisplayName("Complete Purchase Applies Membership Discount To Whole Purchase")
    void completePurchaseAppliesMembershipDiscountToWholePurchase() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-10-16T10:00:00Z"),
                ZoneId.of("UTC")
        );
        register = new CashRegister(clock);

        Membership membership = new Membership(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
        Customer customer = new Customer("C001", "Member", membership);
        Product product = new Product(
                "Test product",
                new Money(Sek.SEK, 100.0),
                food
        );

        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(product);

        Receipt receipt = register.completePurchase(customer);

        assertEquals(90.0, receipt.getTotalPrice().getAmount(), 0.001);
    }

    @Test
    @DisplayName("Complete Purchase Applies Greatest Offer Discount")
    void completePurchaseAppliesGreatestOfferDiscount() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-10-16T10:00:00Z"),
                ZoneId.of("UTC")
        );
        register = new CashRegister(clock);

        Membership membership = new Membership(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );
        Customer customer = new Customer("C001", "Member", membership);

        DiscountOffer offer = new DiscountOffer(
                20.0,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );

        Product product = new Product(
                "Test product",
                new Money(Sek.SEK, 100.0),
                food
        );

        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(product);

        Receipt receipt = register.completePurchase(customer, offer);

        assertEquals(80.0, receipt.getTotalPrice().getAmount(), 0.001);
    }

    @Test
    @DisplayName("Completed Purchase Is Stored In Customer History")
    void completedPurchaseIsStoredInCustomerHistory() {
        Customer customer = new Customer("C001", "Test Customer");

        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(milk);

        register.completePurchase(customer);

        assertAll(
                "Purchase history contains the completed purchase",
                () -> assertEquals(1, customer.getPurchaseHistory().size()),
                () -> assertSame(
                        purchase,
                        customer.getPurchaseHistory().get(0)
                ),
                () -> assertEquals(
                        Purchase.Status.COMPLETED,
                        purchase.getStatus()
                )
        );
    }

    private void assertCurrentActiveEmptyPurchase(Purchase purchase) {
        assertAll("Current purchase is active and empty",
                () -> assertSame(purchase, register.getCurrentPurchase()),
                () -> assertEquals(Purchase.Status.ACTIVE, purchase.getStatus()),
                () -> assertTrue(purchase.getBasket().isEmpty())
        );
    }

    private void assertReceiptLine(ReceiptLine line, String expectedName,
            int expectedQuantity, double expectedUnitPrice) {
        assertAll("Receipt line for " + expectedName,
                () -> assertEquals(expectedName, line.getProductName()),
                () -> assertEquals(expectedQuantity, line.getQuantity()),
                () -> assertEquals(expectedUnitPrice, line.getUnitPrice().getAmount(), 0.001)
        );
    }

}
