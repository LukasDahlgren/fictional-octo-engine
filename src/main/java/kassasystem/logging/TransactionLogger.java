package kassasystem.logging;

import kassasystem.receipt.Receipt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


public class TransactionLogger {

    private final Path logFile;

    public TransactionLogger(Path logFile) {
        if (logFile == null) {
            throw new IllegalArgumentException("Log file path must not be null");
        }
        this.logFile = logFile;
    }

    public void log(Receipt receipt) throws IOException {
        if(receipt == null) {
            throw new IllegalArgumentException("Receipt must not be null");
        }

        Files.writeString(
                logFile,
                receipt.toText(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }
}
