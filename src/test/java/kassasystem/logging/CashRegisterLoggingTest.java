package kassasystem.logging;

import kassasystem.CashRegister;
import kassasystem.Purchase;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;
import kassasystem.receipt.Receipt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}