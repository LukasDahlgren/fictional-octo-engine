import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BarcodeScannerTest {

    @Test
    @DisplayName("En oläsbar kod ger FAILED_READ utan att söka efter en vara")
    void scanReturnsFailedReadWhenNoCodeIsRead() {
        UnreadableCodeReader codeReader = new UnreadableCodeReader();
        BarcodeScanner.ProductLookup productLookup = new BarcodeScanner.ProductLookup() {
            @Override
            public Product findByCode(String code) {
                throw new AssertionError("Product lookup must not be called when no code is read");
            }
        };
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productLookup);

        ScanResult result = scanner.scan();

        assertEquals(ScanResult.Status.FAILED_READ, result.getStatus());
        assertNull(result.getProduct());
        assertEquals(1, codeReader.readCalls);
    }

    private static class UnreadableCodeReader implements BarcodeScanner.CodeReader {
        private int readCalls;

        @Override
        public String readCode() {
            readCalls++;
            return null;
        }
    }
}
