package kassasystem;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
        register = new CashRegister();
        milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        bread = new Product("Bread", new Money(Sek.SEK, 20.00), food);
    }

    @Test
    @DisplayName("Kassan avvisar en saknad klocka")
    void constructorRejectsNullClock() {
        assertThrows(IllegalArgumentException.class, () -> new CashRegister(null));
    }

    @Test
    @DisplayName("Kvittot får tid och tidszon från kassans angivna klocka")
    void completePurchaseUsesSuppliedClockForReceiptTimestamp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-20T10:15:30Z"),
                ZoneId.of("Europe/Stockholm"));
        register = new CashRegister(clock);
        register.startPurchase();

        Receipt receipt = register.completePurchase();

        LocalDateTime expectedTimestamp = LocalDateTime.of(2026, 10, 20, 12, 15, 30);
        assertEquals(expectedTimestamp, receipt.getTimestamp());
    }

    @Test
    @DisplayName("Kassan startar ett aktivt köp med en tom korg")
    void startPurchaseCreatesCurrentActivePurchase() {
        Purchase purchase = register.startPurchase();

        assertCurrentActiveEmptyPurchase(purchase);
    }

    @Test
    @DisplayName("Ett nytt köp kan inte skriva över kassans pågående köp")
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
    @DisplayName("Ett slutfört köp frigör kassan")
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
    @DisplayName("Kassan kan starta ett nytt köp efter ett slutfört köp")
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
    @DisplayName("Ett slutfört köp returnerar ett kvitto med köpets varor, antal och totalpris")
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
    @DisplayName("Ett slutfört köp avvisar ändringar och behåller kvittots innehåll")
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
    @DisplayName("Ett fel vid kvittoskapandet behåller köpet aktivt i kassan")
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
    @DisplayName("Kassan kan inte slutföra när det saknas ett pågående köp")
    void completePurchaseRejectsMissingCurrentPurchase() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.completePurchase());

        assertAll("Completion is rejected while the register stays free",
                () -> assertEquals("Cannot complete a purchase when there is no current purchase", exception.getMessage()),
                () -> assertNull(register.getCurrentPurchase())
        );
    }

    @Test
    @DisplayName("Kassan kan inte slutföra ett redan avbrutet köp")
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
    @DisplayName("Kassan avvisar ett inaktivt köp som fortfarande är aktuellt")
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
    @DisplayName("Ett avbrutet köp frigör kassan")
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
    @DisplayName("Kassan kan starta ett nytt köp efter ett avbrutet köp")
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
    @DisplayName("Kassan kan inte avbryta när det saknas ett pågående köp")
    void cancelPurchaseRejectsMissingCurrentPurchase() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.cancelPurchase());

        assertAll("Cancellation is rejected while the register stays free",
                () -> assertEquals("Cannot cancel a purchase when there is no current purchase", exception.getMessage()),
                () -> assertNull(register.getCurrentPurchase())
        );
    }

    @Test
    @DisplayName("Kassan kan inte avbryta ett redan slutfört köp")
    void cancelPurchaseRejectsPreviouslyCompletedPurchase() {
        Purchase purchase = register.startPurchase();
        register.completePurchase();

        assertThrows(IllegalStateException.class, () -> register.cancelPurchase());

        assertAll("Rejected cancellation preserves the completed purchase and free register",
                () -> assertNull(register.getCurrentPurchase()),
                () -> assertEquals(Purchase.Status.COMPLETED, purchase.getStatus())
        );
    }

    @Test
    @DisplayName("Kundcheckout avvisar en saknad kund och behåller köpet aktivt")
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
    @DisplayName("Aktiv medlem får tio procent rabatt på hela köpet")
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
        Customer customer = new Customer("C001", "Medlem", membership);
        Product product = new Product(
                "Testprodukt",
                new Money(Sek.SEK, 100.0),
                food
        );

        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(product);

        Receipt receipt = register.completePurchase(customer);

        assertEquals(90.0, receipt.getTotalPrice().getAmount(), 0.001);
    }

    @Test
    @DisplayName("Det största erbjudandet väljs framför medlemsrabatten")
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
        Customer customer = new Customer("C001", "Medlem", membership);

        DiscountOffer offer = new DiscountOffer(
                20.0,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );

        Product product = new Product(
                "Testprodukt",
                new Money(Sek.SEK, 100.0),
                food
        );

        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(product);

        Receipt receipt = register.completePurchase(customer, offer);

        assertEquals(80.0, receipt.getTotalPrice().getAmount(), 0.001);
    }

    @Test
    @DisplayName("Ett slutfört köp sparas i kundens köphistorik")
    void completedPurchaseIsStoredInCustomerHistory() {
        Customer customer = new Customer("C001", "Testkund");

        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(milk);

        register.completePurchase(customer);

        assertAll(
                "Köphistoriken innehåller det slutförda köpet",
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
