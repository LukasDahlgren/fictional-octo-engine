package kassasystem.logging;

import kassasystem.basket.Basket;
import kassasystem.receipt.Receipt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;

 class TransactionLoggerTest {

    @TempDir
    Path tempDir;

    @Test
     @DisplayName("Writes a receipt to the transaction log file")
     void writeReceiptToLogFile() throws IOException {
        Path logFile = tempDir.resolve("transaction.txt");
        Receipt receipt = new Receipt(new Basket(), Clock.systemUTC());
        TransactionLogger logger = new TransactionLogger(logFile);

        logger.log(receipt);

        assertEquals(receipt.toText(), Files.readString(logFile));
    }
}
