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
import static org.junit.jupiter.api.Assertions.assertThrows;

 class TransactionLoggerTest {

    @TempDir
    Path tempDir;

    @Test
     @DisplayName("Write Receipt To Log File")
     void writeReceiptToLogFile() throws IOException {
        Path logFile = tempDir.resolve("transaction.txt");
        Receipt receipt = new Receipt(new Basket(), Clock.systemUTC());
        TransactionLogger logger = new TransactionLogger(logFile);

        logger.log(receipt);

        assertEquals(receipt.toText(), Files.readString(logFile));
    }

    @Test
     @DisplayName("Rejects Null Receipt")
     void rejectsNullReceipt() {
        TransactionLogger logger =
                new TransactionLogger(tempDir.resolve("transactions.txt"));

        assertThrows(
                IllegalArgumentException.class,
                () -> logger.log(null)
        );
    }

     @Test
     @DisplayName("Rejects Null Log File Path")
     void rejectsNullLogFilePath() {
         assertThrows(
                 IllegalArgumentException.class,
                 () -> new TransactionLogger(null)
         );
     }

}
