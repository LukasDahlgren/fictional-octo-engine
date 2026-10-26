package kassasystem.logging;

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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CashRegisterLoggingTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Logs a completed purchase")
    void logsCompletedPurchase() throws IOException {
        Path logFile = tempDir.resolve("transactions.txt");
        TransactionLogger logger = new TransactionLogger(logFile);
        CashRegister cashRegister =
                new CashRegister(logger, Clock.systemUTC());

        ProductCategory food =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product milk =
                new Product("Milk", new Money(Sek.SEK, 12.50), food);

        Purchase purchase = cashRegister.startPurchase();
        purchase.getBasket().addProduct(milk);

        Receipt receipt = cashRegister.completePurchase();

        assertEquals(receipt.toText(), Files.readString(logFile));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = Purchase.Status.class, names = {"CANCELLED", "COMPLETED"})
    @DisplayName("Checkout avvisar inaktiva köp utan att ändra köp eller transaktionslogg")
    void inactivePurchaseDoesNotChangeTransactionLog(Purchase.Status status) throws IOException {
        Path logFile = tempDir.resolve("transactions.txt");
        String previousLog = "Previous transaction\n";
        Files.writeString(logFile, previousLog);
        TransactionLogger logger = new TransactionLogger(logFile);
        CashRegister cashRegister = new CashRegister(logger, Clock.systemUTC());
        Purchase purchase = startInactivePurchase(cashRegister, status);

        assertThrows(IllegalStateException.class, () -> cashRegister.completePurchase());

        assertAll("Rejected checkout preserves the purchase and log",
                () -> assertSame(purchase, cashRegister.getCurrentPurchase()),
                () -> assertEquals(status, purchase.getStatus()),
                () -> assertEquals(new Money(Sek.SEK, 12.50), purchase.getBasket().getTotalPrice()),
                () -> assertEquals(previousLog, Files.readString(logFile))
        );
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = Purchase.Status.class, names = {"CANCELLED", "COMPLETED"})
    @DisplayName("Kundcheckout avvisar inaktiva köp utan att ändra logg eller köphistorik")
    void inactiveCustomerPurchasePreservesLogAndHistory(Purchase.Status status)
            throws IOException {
        Path logFile = tempDir.resolve("transactions.txt");
        String previousLog = "Previous transaction\n";
        Files.writeString(logFile, previousLog);
        TransactionLogger logger = new TransactionLogger(logFile);
        CashRegister cashRegister = new CashRegister(logger, Clock.systemUTC());
        Customer customer = new Customer("C001", "Testkund");
        Purchase previousPurchase = new Purchase();
        previousPurchase.complete();
        customer.addPurchase(previousPurchase);
        Purchase purchase = startInactivePurchase(cashRegister, status);

        assertThrows(IllegalStateException.class,
                () -> cashRegister.completePurchase(customer));

        assertAll("Rejected checkout preserves the purchase, log and existing history",
                () -> assertSame(purchase, cashRegister.getCurrentPurchase()),
                () -> assertEquals(status, purchase.getStatus()),
                () -> assertEquals(new Money(Sek.SEK, 12.50), purchase.getBasket().getTotalPrice()),
                () -> assertEquals(previousLog, Files.readString(logFile)),
                () -> assertEquals(List.of(previousPurchase), customer.getPurchaseHistory())
        );
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = Purchase.Status.class, names = {"CANCELLED", "COMPLETED"})
    @DisplayName("Kundcheckout med erbjudande avvisar inaktiva köp utan att ändra logg eller köphistorik")
    void inactiveCustomerPurchaseWithOfferPreservesLogAndHistory(Purchase.Status status)
            throws IOException {
        Path logFile = tempDir.resolve("transactions.txt");
        String previousLog = "Previous transaction\n";
        Files.writeString(logFile, previousLog);
        TransactionLogger logger = new TransactionLogger(logFile);
        CashRegister cashRegister = new CashRegister(logger, Clock.systemUTC());
        Customer customer = new Customer("C001", "Testkund");
        Purchase previousPurchase = new Purchase();
        previousPurchase.complete();
        customer.addPurchase(previousPurchase);
        Purchase purchase = startInactivePurchase(cashRegister, status);
        DiscountOffer offer = new DiscountOffer(20,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThrows(IllegalStateException.class,
                () -> cashRegister.completePurchase(customer, offer));

        assertAll("Rejected checkout preserves the purchase, log and existing history",
                () -> assertSame(purchase, cashRegister.getCurrentPurchase()),
                () -> assertEquals(status, purchase.getStatus()),
                () -> assertEquals(new Money(Sek.SEK, 12.50), purchase.getBasket().getTotalPrice()),
                () -> assertEquals(previousLog, Files.readString(logFile)),
                () -> assertEquals(List.of(previousPurchase), customer.getPurchaseHistory())
        );
    }

    private Purchase startInactivePurchase(CashRegister cashRegister, Purchase.Status status) {
        ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        Purchase purchase = cashRegister.startPurchase();
        purchase.getBasket().addProduct(milk);
        if (status == Purchase.Status.CANCELLED) {
            purchase.cancel();
        } else {
            purchase.complete();
        }
        return purchase;
    }
}
