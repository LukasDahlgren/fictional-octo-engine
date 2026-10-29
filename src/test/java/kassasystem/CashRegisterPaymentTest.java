package kassasystem;

import kassasystem.payment.FakePayer;
import kassasystem.payment.Payer;
import kassasystem.customer.Customer;
import kassasystem.customer.Membership;
import kassasystem.discount.DiscountOffer;
import kassasystem.logging.TransactionLogger;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;
import kassasystem.receipt.Receipt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CashRegisterPaymentTest {

    private enum CheckoutType {
        ANONYMOUS, CUSTOMER, OFFER
    }

    @TempDir
    Path tempDir;

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-16T10:00:00Z"), ZoneOffset.UTC);
    private Product product;
    private Customer customer;
    private DiscountOffer offer;

    @BeforeEach
    void setUp() {
        ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);
        product = new Product("Milk", new Money(Sek.SEK, 100.0), food);
        Membership membership = new Membership(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        customer = new Customer("C001", "Member", membership);
        offer = new DiscountOffer(20,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
    }

    @ParameterizedTest(name = "With logger: {0}")
    @ValueSource(booleans = {false, true})
    @DisplayName("Both Constructors Reject A Missing Payer")
    void constructorRejectsNullPayer(boolean withLogger) {
        TransactionLogger logger = new TransactionLogger(tempDir.resolve("transactions.txt"));

        IllegalArgumentException exception;
        if (withLogger) {
            exception = assertThrows(IllegalArgumentException.class,
                    () -> new CashRegister(logger, clock, null));
        } else {
            exception = assertThrows(IllegalArgumentException.class,
                    () -> new CashRegister(clock, null));
        }

        assertEquals("Payer must not be null", exception.getMessage());
    }

    @ParameterizedTest(name = "{0}: {1} SEK")
    @CsvSource({"ANONYMOUS, 100.0", "CUSTOMER, 90.0", "OFFER, 80.0"})
    @DisplayName("Successful Payment Completes And Logs Each Checkout Variant With The Correct Final Amount")
    void checkoutCompletesAtFinalPrice(CheckoutType checkoutType, double expectedAmount) throws IOException {
        Path logFile = tempDir.resolve("transactions.txt");
        List<Money> paymentAttempts = new ArrayList<>();
        Payer payer = amount -> {
            assertFalse(Files.exists(logFile));
            paymentAttempts.add(amount);
            return true;
        };
        TransactionLogger logger = new TransactionLogger(logFile);
        CashRegister register = new CashRegister(logger, clock, payer);
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(product);

        Receipt receipt = completePurchase(register, checkoutType);

        Money expectedPrice = new Money(Sek.SEK, expectedAmount);
        assertAll("Successful payment completes the purchase at the final price",
                () -> assertEquals(List.of(expectedPrice), paymentAttempts),
                () -> assertEquals(expectedPrice, receipt.getTotalPrice()),
                () -> assertEquals(Purchase.Status.COMPLETED, purchase.getStatus()),
                () -> assertNull(register.getCurrentPurchase()),
                () -> assertEquals(receipt.toText(), Files.readString(logFile))
        );
        if (checkoutType == CheckoutType.ANONYMOUS) {
            assertEquals(List.of(), customer.getPurchaseHistory());
        } else {
            assertEquals(List.of(purchase), customer.getPurchaseHistory());
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(CheckoutType.class)
    @DisplayName("Rejected Payment Preserves The Purchase Previous Log And Purchase History")
    void rejectedPaymentPreservesPurchaseLogAndHistory(CheckoutType checkoutType) throws IOException {
        CashRegister previousRegister = new CashRegister(clock, new FakePayer(true));
        Purchase previousPurchase = previousRegister.startPurchase();
        previousPurchase.getBasket().addProduct(product);
        previousRegister.completePurchase(customer);
        Path logFile = tempDir.resolve("transactions.txt");
        String previousLog = "Previous transaction\n";
        Files.writeString(logFile, previousLog);
        TransactionLogger logger = new TransactionLogger(logFile);
        CashRegister register = new CashRegister(logger, clock, new FakePayer(false));
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(product);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> completePurchase(register, checkoutType));

        assertEquals("Payment failed", exception.getMessage());
        assertAll("Rejected payment preserves checkout state",
                () -> assertSame(purchase, register.getCurrentPurchase()),
                () -> assertEquals(Purchase.Status.ACTIVE, purchase.getStatus()),
                () -> assertEquals(1, purchase.getBasket().getTotalQuantity()),
                () -> assertEquals(previousLog, Files.readString(logFile)),
                () -> assertEquals(List.of(previousPurchase), customer.getPurchaseHistory())
        );
        purchase.getBasket().addProduct(product);
        assertEquals(2, purchase.getBasket().getTotalQuantity());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(CheckoutType.class)
    @DisplayName("Payer Failure Is Propagated Without Completing Or Logging The Purchase")
    void payerExceptionPreservesPurchase(CheckoutType checkoutType) {
        IllegalStateException failure = new IllegalStateException("Payment service unavailable");
        Payer payer = amount -> {
            throw failure;
        };
        Path logFile = tempDir.resolve("transactions.txt");
        TransactionLogger logger = new TransactionLogger(logFile);
        CashRegister register = new CashRegister(logger, clock, payer);
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(product);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> completePurchase(register, checkoutType));

        assertAll("Payment service failure preserves checkout state",
                () -> assertSame(failure, exception),
                () -> assertSame(purchase, register.getCurrentPurchase()),
                () -> assertEquals(Purchase.Status.ACTIVE, purchase.getStatus()),
                () -> assertEquals(1, purchase.getBasket().getTotalQuantity()),
                () -> assertFalse(Files.exists(logFile)),
                () -> assertEquals(List.of(), customer.getPurchaseHistory())
        );
    }

    @ParameterizedTest(name = "{0}: {1} SEK")
    @CsvSource({"ANONYMOUS, 100.0", "CUSTOMER, 90.0", "OFFER, 80.0"})
    @DisplayName("Logging Failure After Payment Leaves The Purchase Completed And Prevents Another Charge")
    void loggingFailureCannotChargePurchaseAgain(CheckoutType checkoutType, double expectedAmount) {
        List<Money> paymentAttempts = new ArrayList<>();
        Payer payer = amount -> {
            paymentAttempts.add(amount);
            return true;
        };
        TransactionLogger logger = new TransactionLogger(tempDir);
        CashRegister register = new CashRegister(logger, clock, payer);
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(product);

        UncheckedIOException exception = assertThrows(UncheckedIOException.class,
                () -> completePurchase(register, checkoutType));

        assertAll("Paid purchase remains completed after logging fails",
                () -> assertEquals("Purchase completed and paid, but transaction could not be logged",
                        exception.getMessage()),
                () -> assertEquals(Purchase.Status.COMPLETED, purchase.getStatus()),
                () -> assertNull(register.getCurrentPurchase())
        );
        assertThrows(IllegalStateException.class, () -> completePurchase(register, checkoutType));
        assertThrows(IllegalStateException.class, () -> register.cancelPurchase());
        assertThrows(IllegalStateException.class, () -> purchase.getBasket().addProduct(product));
        assertThrows(IllegalStateException.class, () -> purchase.getBasket().getItems().get(0).increaseQuantity());
        assertEquals(List.of(new Money(Sek.SEK, expectedAmount)), paymentAttempts);
        if (checkoutType == CheckoutType.ANONYMOUS) {
            assertEquals(List.of(), customer.getPurchaseHistory());
        } else {
            assertEquals(List.of(purchase), customer.getPurchaseHistory());
        }
    }

    @ParameterizedTest(name = "{0}: {1} SEK")
    @CsvSource({"ANONYMOUS, 100.0", "CUSTOMER, 90.0", "OFFER, 80.0"})
    @DisplayName("Rejected Payment Can Be Retried Without Duplicate Log Entries Or Purchase History")
    void rejectedPaymentCanBeRetried(CheckoutType checkoutType, double expectedAmount) throws IOException {
        List<Money> paymentAttempts = new ArrayList<>();
        Payer payer = amount -> {
            paymentAttempts.add(amount);
            return paymentAttempts.size() > 1;
        };
        Path logFile = tempDir.resolve("transactions.txt");
        CashRegister register = new CashRegister(new TransactionLogger(logFile), clock, payer);
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(product);

        assertThrows(IllegalStateException.class, () -> completePurchase(register, checkoutType));

        assertAll("Rejected first attempt has no checkout side effects",
                () -> assertEquals(Purchase.Status.ACTIVE, purchase.getStatus()),
                () -> assertSame(purchase, register.getCurrentPurchase()),
                () -> assertFalse(Files.exists(logFile)),
                () -> assertEquals(List.of(), customer.getPurchaseHistory())
        );

        Receipt receipt = completePurchase(register, checkoutType);

        Money expectedPrice = new Money(Sek.SEK, expectedAmount);
        assertAll("Successful retry completes and logs the purchase once",
                () -> assertEquals(List.of(expectedPrice, expectedPrice), paymentAttempts),
                () -> assertEquals(expectedPrice, receipt.getTotalPrice()),
                () -> assertEquals(Purchase.Status.COMPLETED, purchase.getStatus()),
                () -> assertNull(register.getCurrentPurchase()),
                () -> assertEquals(receipt.toText(), Files.readString(logFile))
        );
        if (checkoutType == CheckoutType.ANONYMOUS) {
            assertEquals(List.of(), customer.getPurchaseHistory());
        } else {
            assertEquals(List.of(purchase), customer.getPurchaseHistory());
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(CheckoutType.class)
    @DisplayName("Payer Is Not Used Without A Current Purchase")
    void missingPurchaseDoesNotCallPayer(CheckoutType checkoutType) {
        List<Money> paymentAttempts = new ArrayList<>();
        Payer payer = amount -> {
            paymentAttempts.add(amount);
            return true;
        };
        CashRegister register = new CashRegister(clock, payer);

        assertThrows(IllegalStateException.class, () -> completePurchase(register, checkoutType));

        assertEquals(List.of(), paymentAttempts);
        assertNull(register.getCurrentPurchase());
        assertEquals(List.of(), customer.getPurchaseHistory());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(CheckoutType.class)
    @DisplayName("An Empty Basket Is Rejected Before The Payer Is Used")
    void emptyBasketDoesNotCallPayer(CheckoutType checkoutType) {
        List<Money> paymentAttempts = new ArrayList<>();
        Payer payer = amount -> {
            paymentAttempts.add(amount);
            return true;
        };
        CashRegister register = new CashRegister(clock, payer);
        Purchase purchase = register.startPurchase();

        assertThrows(IllegalStateException.class, () -> completePurchase(register, checkoutType));

        assertAll("Empty purchase remains unpaid and active",
                () -> assertEquals(List.of(), paymentAttempts),
                () -> assertSame(purchase, register.getCurrentPurchase()),
                () -> assertEquals(Purchase.Status.ACTIVE, purchase.getStatus()),
                () -> assertEquals(List.of(), customer.getPurchaseHistory())
        );
    }

    @ParameterizedTest(name = "{0}: {1}")
    @CsvSource({"ANONYMOUS, CANCELLED", "CUSTOMER, CANCELLED", "OFFER, CANCELLED",
            "ANONYMOUS, COMPLETED", "CUSTOMER, COMPLETED", "OFFER, COMPLETED"})
    @DisplayName("An Inactive Purchase Is Rejected Before The Payer Is Used")
    void inactivePurchaseDoesNotCallPayer(CheckoutType checkoutType, Purchase.Status status) {
        List<Money> paymentAttempts = new ArrayList<>();
        Payer payer = amount -> {
            paymentAttempts.add(amount);
            return true;
        };
        CashRegister register = new CashRegister(clock, payer);
        Purchase purchase = register.startPurchase();
        purchase.getBasket().addProduct(product);
        if (status == Purchase.Status.CANCELLED) {
            purchase.cancel();
        } else {
            purchase.complete();
        }

        assertThrows(IllegalStateException.class, () -> completePurchase(register, checkoutType));

        assertAll("Inactive purchase is unchanged without taking payment",
                () -> assertEquals(List.of(), paymentAttempts),
                () -> assertSame(purchase, register.getCurrentPurchase()),
                () -> assertEquals(status, purchase.getStatus()),
                () -> assertEquals(List.of(), customer.getPurchaseHistory())
        );
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(CheckoutType.class)
    @DisplayName("An Invalid Receipt Is Rejected Before The Payer Is Used")
    void invalidReceiptDoesNotCallPayer(CheckoutType checkoutType) {
        List<Money> paymentAttempts = new ArrayList<>();
        Payer payer = amount -> {
            paymentAttempts.add(amount);
            return true;
        };
        CashRegister register = new CashRegister(clock, payer);
        Purchase purchase = register.startPurchase();
        Product invalidProduct = new Product("Invalid", new Money(Sek.SEK, -1.0), product.getCategory());
        purchase.getBasket().addProduct(invalidProduct);

        assertThrows(IllegalArgumentException.class, () -> completePurchase(register, checkoutType));

        assertAll("Invalid receipt preserves the unpaid purchase",
                () -> assertEquals(List.of(), paymentAttempts),
                () -> assertSame(purchase, register.getCurrentPurchase()),
                () -> assertEquals(Purchase.Status.ACTIVE, purchase.getStatus()),
                () -> assertEquals(List.of(), customer.getPurchaseHistory())
        );
    }

    private Receipt completePurchase(CashRegister register, CheckoutType checkoutType) {
        if (checkoutType == CheckoutType.ANONYMOUS) {
            return register.completePurchase();
        }
        if (checkoutType == CheckoutType.CUSTOMER) {
            return register.completePurchase(customer);
        }
        return register.completePurchase(customer, offer);
    }
}
