package kassasystem;

import kassasystem.payment.FakePayer;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CashRegisterTest {

    private final ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);

    private CashRegister register;
    private Product milk;
    private Product bread;

    @BeforeEach
    void setUp() {
        register = new CashRegister(Clock.systemUTC(), new FakePayer(true));
        milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        bread = new Product("Bread", new Money(Sek.SEK, 20.00), food);
    }

    @Test
    @DisplayName("Register Rejects A Missing Clock")
    void constructorRejectsNullClock() {
        assertThrows(IllegalArgumentException.class, () -> new CashRegister((Clock) null, new FakePayer(true)));
    }


    @Test
    @DisplayName("Failed Payment Keeps The Purchase Active And Creates No Receipt")
    void completePurchaseKeepsPurchaseActiveWhenPaymentFails() {
        register = new CashRegister(Clock.systemUTC(), new FakePayer(false));
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(milk);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.completePurchase());

        assertAll("Rejected payment preserves the active purchase",
                () -> assertEquals("Payment failed", exception.getMessage()),
                () -> assertSame(purchase, register.getCurrentPurchase()),
                () -> assertEquals(Purchase.Status.ACTIVE, purchase.getStatus()),
                () -> assertEquals(1, purchase.getBasket().getTotalQuantity())
        );
    }

    @Test
    @DisplayName("Receipt Uses The Time And Time Zone From The Supplied Register Clock")
    void completePurchaseUsesSuppliedClockForReceiptTimestamp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-20T10:15:30Z"),
                ZoneId.of("Europe/Stockholm"));
        register = new CashRegister(clock, new FakePayer(true));
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(milk);

        Receipt receipt = register.completePurchase();

        LocalDateTime expectedTimestamp = LocalDateTime.of(2026, 10, 20, 12, 15, 30);
        assertEquals(expectedTimestamp, receipt.getTimestamp());
    }

    @Test
    @DisplayName("Register Starts An Active Purchase With An Empty Basket")
    void startPurchaseCreatesCurrentActivePurchase() {
        Purchase purchase = register.startPurchase();

        assertCurrentActiveEmptyPurchase(purchase);
    }

    @Test
    @DisplayName("A New Purchase Cannot Replace The Current Purchase")
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
    @DisplayName("Completing A Purchase Frees The Register")
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
    @DisplayName("Register Can Start A New Purchase After Completion")
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
    @DisplayName("Completed Purchase Returns A Receipt With Its Items Quantities And Total")
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
    @DisplayName("Completed Purchase Rejects Changes And Preserves The Receipt Contents")
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
    @DisplayName("Receipt Creation Failure Keeps The Purchase Active In The Register")
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
    @DisplayName("Register Cannot Complete A Purchase When There Is No Current Purchase")
    void completePurchaseRejectsMissingCurrentPurchase() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.completePurchase());

        assertAll("Completion is rejected while the register stays free",
                () -> assertEquals("Cannot complete a purchase when there is no current purchase", exception.getMessage()),
                () -> assertNull(register.getCurrentPurchase())
        );
    }

    @Test
    @DisplayName("Register Rejects An Empty Basket And Keeps The Purchase Active")
    void completePurchaseRejectsEmptyBasket() {
        Purchase purchase = register.startPurchase();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.completePurchase());

        assertAll("Rejected checkout preserves the empty active purchase",
                () -> assertEquals("Cannot complete a purchase with no products in the basket", exception.getMessage()),
                () -> assertCurrentActiveEmptyPurchase(purchase)
        );
    }

    @ParameterizedTest(name = "With offer: {0}")
    @ValueSource(booleans = {false, true})
    @DisplayName("Customer Checkout Rejects An Empty Basket And Preserves Purchase History")
    void completeCustomerPurchaseRejectsEmptyBasket(boolean withOffer) {
        Customer customer = new Customer("C001", "Test customer");
        Purchase previousPurchase = register.startPurchase();
        previousPurchase.getBasket().addProduct(milk);
        register.completePurchase(customer);
        Purchase purchase = register.startPurchase();
        DiscountOffer offer = new DiscountOffer(20,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        IllegalStateException exception;
        if (withOffer) {
            exception = assertThrows(IllegalStateException.class,
                    () -> register.completePurchase(customer, offer));
        } else {
            exception = assertThrows(IllegalStateException.class,
                    () -> register.completePurchase(customer));
        }

        assertEquals("Cannot complete a purchase with no products in the basket", exception.getMessage());
        assertAll("Rejected checkout preserves the empty active purchase and existing history",
                () -> assertCurrentActiveEmptyPurchase(purchase),
                () -> assertEquals(List.of(previousPurchase), customer.getPurchaseHistory())
        );
    }

    @Test
    @DisplayName("Register Cannot Complete A Previously Cancelled Purchase")
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
    @DisplayName("Register Rejects An Inactive Purchase That Is Still Current")
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
    @DisplayName("Cancelling A Purchase Frees The Register")
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
    @DisplayName("Register Can Start A New Purchase After Cancellation")
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
    @DisplayName("Register Cannot Cancel A Purchase When There Is No Current Purchase")
    void cancelPurchaseRejectsMissingCurrentPurchase() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.cancelPurchase());

        assertAll("Cancellation is rejected while the register stays free",
                () -> assertEquals("Cannot cancel a purchase when there is no current purchase", exception.getMessage()),
                () -> assertNull(register.getCurrentPurchase())
        );
    }

    @Test
    @DisplayName("Register Cannot Cancel A Previously Completed Purchase")
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
    @DisplayName("Customer Checkout Rejects A Missing Customer And Keeps The Purchase Active")
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
    @DisplayName("Active Member Receives Ten Percent Off The Entire Purchase")
    void completePurchaseAppliesMembershipDiscountToWholePurchase() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-10-16T10:00:00Z"),
                ZoneId.of("UTC")
        );
        register = new CashRegister(clock, new FakePayer(true));

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
    @DisplayName("The Largest Offer Is Chosen Over The Membership Discount")
    void completePurchaseAppliesGreatestOfferDiscount() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-10-16T10:00:00Z"),
                ZoneId.of("UTC")
        );
        register = new CashRegister(clock, new FakePayer(true));

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
    @DisplayName("A Completed Purchase Is Stored In The Customer Purchase History")
    void completedPurchaseIsStoredInCustomerHistory() {
        Customer customer = new Customer("C001", "Test customer");

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
