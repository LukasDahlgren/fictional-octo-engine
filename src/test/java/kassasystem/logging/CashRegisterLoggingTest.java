package kassasystem.logging;

import kassasystem.payment.FakePayer;
import kassasystem.CashRegister;
import kassasystem.Purchase;
import kassasystem.customer.Customer;
import kassasystem.discount.DiscountOffer;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;
import kassasystem.receipt.Receipt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CashRegisterLoggingTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Constructor Rejects Null Transaction Logger")
    void constructorRejectsNullTransactionLogger() {
        Clock clock = Clock.systemUTC();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new CashRegister(null, clock, new FakePayer(true)));

        assertEquals("Transaction logger must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor Rejects Null Clock With Transaction Logger")
    void constructorRejectsNullClockWithTransactionLogger() {
        Path logFile = tempDir.resolve("transactions.txt");
        TransactionLogger logger = new TransactionLogger(logFile);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new CashRegister(logger,null, new FakePayer(true)));

        assertEquals("Clock must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Logs Completed Purchase")
    void logsCompletedPurchase() throws IOException {
        Path logFile = tempDir.resolve("transactions.txt");
        TransactionLogger logger = new TransactionLogger(logFile);
        CashRegister cashRegister =
                new CashRegister(logger, Clock.systemUTC(), new FakePayer(true));

        ProductCategory food =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product milk =
                new Product("Milk", new Money(Sek.SEK, 12.50), food);

        Purchase purchase = cashRegister.startPurchase();
        purchase.getBasket().addProduct(milk);

        Receipt receipt = cashRegister.completePurchase();

        assertEquals(receipt.toText(), Files.readString(logFile));
    }

    @ParameterizedTest(name = "Med erbjudande: {0}")
    @ValueSource(booleans = {false, true})
    @DisplayName("Misslyckad kundbetalning behåller köp, logg och tidigare köphistorik")
    void failedCustomerPaymentPreservesPurchaseLogAndHistory(boolean withOffer) throws IOException {
        Clock clock = Clock.fixed(Instant.parse("2026-10-16T10:00:00Z"), ZoneOffset.UTC);
        Path logFile = tempDir.resolve("transactions.txt");
        TransactionLogger logger = new TransactionLogger(logFile);
        Customer customer = new Customer("C001", "Testkund");
        ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        CashRegister previousRegister = new CashRegister(logger, clock, new FakePayer(true));
        Purchase previousPurchase = previousRegister.startPurchase();
        previousPurchase.getBasket().addProduct(milk);
        previousRegister.completePurchase(customer);
        String previousLog = Files.readString(logFile);
        CashRegister register = new CashRegister(logger, clock, new FakePayer(false));
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(milk);
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

        assertEquals("Payment failed", exception.getMessage());
        assertAll("Rejected payment preserves the purchase, log and history",
                () -> assertSame(purchase, register.getCurrentPurchase()),
                () -> assertEquals(Purchase.Status.ACTIVE, purchase.getStatus()),
                () -> assertEquals(new Money(Sek.SEK, 12.50), purchase.getBasket().getTotalPrice()),
                () -> assertEquals(previousLog, Files.readString(logFile)),
                () -> assertEquals(List.of(previousPurchase), customer.getPurchaseHistory())
        );
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = Purchase.Status.class, names = {"CANCELLED", "COMPLETED"})
    @DisplayName("Inactive Purchase Does Not Change Transaction Log")
    void inactivePurchaseDoesNotChangeTransactionLog(Purchase.Status status) throws IOException {
        Path logFile = tempDir.resolve("transactions.txt");
        String previousLog = "Previous transaction\n";
        Files.writeString(logFile, previousLog);
        TransactionLogger logger = new TransactionLogger(logFile);
        CashRegister cashRegister = new CashRegister(logger, Clock.systemUTC(), new FakePayer(true));
        Purchase purchase = startInactivePurchase(cashRegister, status);
        String logBeforeRejectedCheckout = Files.readString(logFile);

        assertThrows(IllegalStateException.class, () -> cashRegister.completePurchase());

        assertAll("Rejected checkout preserves the purchase and log",
                () -> assertNull(cashRegister.getCurrentPurchase()),
                () -> assertEquals(status, purchase.getStatus()),
                () -> assertEquals(new Money(Sek.SEK, 12.50), purchase.getBasket().getTotalPrice()),
                () -> assertEquals(logBeforeRejectedCheckout, Files.readString(logFile))
        );
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = Purchase.Status.class, names = {"CANCELLED", "COMPLETED"})
    @DisplayName("Inactive Customer Purchase Preserves Log And History")
    void inactiveCustomerPurchasePreservesLogAndHistory(Purchase.Status status)
            throws IOException {
        Path logFile = tempDir.resolve("transactions.txt");
        String previousLog = "Previous transaction\n";
        Files.writeString(logFile, previousLog);
        TransactionLogger logger = new TransactionLogger(logFile);
        CashRegister cashRegister = new CashRegister(logger, Clock.systemUTC(), new FakePayer(true));
        Customer customer = new Customer("C001", "Test Customer");
        CashRegister previousRegister = new CashRegister(Clock.systemUTC(), new FakePayer(true));
        Purchase previousPurchase = previousRegister.startPurchase();
        ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        previousPurchase.getBasket().addProduct(milk);
        previousRegister.completePurchase();
        customer.addPurchase(previousPurchase);
        Purchase purchase = startInactivePurchase(cashRegister, status);
        String logBeforeRejectedCheckout = Files.readString(logFile);

        assertThrows(IllegalStateException.class,
                () -> cashRegister.completePurchase(customer));

        assertAll("Rejected checkout preserves the purchase, log and existing history",
                () -> assertNull(cashRegister.getCurrentPurchase()),
                () -> assertEquals(status, purchase.getStatus()),
                () -> assertEquals(new Money(Sek.SEK, 12.50), purchase.getBasket().getTotalPrice()),
                () -> assertEquals(logBeforeRejectedCheckout, Files.readString(logFile)),
                () -> assertEquals(List.of(previousPurchase), customer.getPurchaseHistory())
        );
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = Purchase.Status.class, names = {"CANCELLED", "COMPLETED"})
    @DisplayName("Inactive Customer Purchase With Offer Preserves Log And History")
    void inactiveCustomerPurchaseWithOfferPreservesLogAndHistory(Purchase.Status status)
            throws IOException {
        Path logFile = tempDir.resolve("transactions.txt");
        String previousLog = "Previous transaction\n";
        Files.writeString(logFile, previousLog);
        TransactionLogger logger = new TransactionLogger(logFile);
        CashRegister cashRegister = new CashRegister(logger, Clock.systemUTC(), new FakePayer(true));
        Customer customer = new Customer("C001", "Test Customer");
        CashRegister previousRegister = new CashRegister(Clock.systemUTC(), new FakePayer(true));
        Purchase previousPurchase = previousRegister.startPurchase();
        ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        previousPurchase.getBasket().addProduct(milk);
        previousRegister.completePurchase();
        customer.addPurchase(previousPurchase);
        Purchase purchase = startInactivePurchase(cashRegister, status);
        String logBeforeRejectedCheckout = Files.readString(logFile);
        DiscountOffer offer = new DiscountOffer(20,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThrows(IllegalStateException.class,
                () -> cashRegister.completePurchase(customer, offer));

        assertAll("Rejected checkout preserves the purchase, log and existing history",
                () -> assertNull(cashRegister.getCurrentPurchase()),
                () -> assertEquals(status, purchase.getStatus()),
                () -> assertEquals(new Money(Sek.SEK, 12.50), purchase.getBasket().getTotalPrice()),
                () -> assertEquals(logBeforeRejectedCheckout, Files.readString(logFile)),
                () -> assertEquals(List.of(previousPurchase), customer.getPurchaseHistory())
        );
    }

    @Test
    @DisplayName("Ett loggfel behåller det betalda köpet i historiken och tillåter nästa köp")
    void loggingFailureDoesNotPreventNextPurchase() throws IOException {
        Path logFile = tempDir.resolve("transactions.txt");
        Files.createDirectory(logFile);
        TransactionLogger logger = new TransactionLogger(logFile);
        CashRegister register = new CashRegister(logger, Clock.systemUTC(), new FakePayer(true));
        Customer customer = new Customer("C001", "Test Customer");
        ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(milk);

        assertThrows(UncheckedIOException.class, () -> register.completePurchase(customer));

        assertEquals(Purchase.Status.COMPLETED, purchase.getStatus());
        assertNull(register.getCurrentPurchase());
        assertEquals(List.of(purchase), customer.getPurchaseHistory());

        Files.delete(logFile);
        Purchase nextPurchase = register.startPurchase();
        nextPurchase.getBasket().addProduct(milk);

        Receipt nextReceipt = register.completePurchase(customer);

        assertAll("Next purchase completes and logs normally",
                () -> assertEquals(Purchase.Status.COMPLETED, nextPurchase.getStatus()),
                () -> assertNull(register.getCurrentPurchase()),
                () -> assertEquals(List.of(purchase, nextPurchase), customer.getPurchaseHistory()),
                () -> assertEquals(nextReceipt.toText(), Files.readString(logFile))
        );
    }

    private Purchase startInactivePurchase(CashRegister cashRegister, Purchase.Status status) {
        ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        Purchase purchase = cashRegister.startPurchase();
        purchase.getBasket().addProduct(milk);
        if (status == Purchase.Status.CANCELLED) {
            cashRegister.cancelPurchase();
        } else {
            cashRegister.completePurchase();
        }
        return purchase;
    }
}
