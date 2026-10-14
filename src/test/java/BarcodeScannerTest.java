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

    @Test
    @DisplayName("SC-04: En tom kod ger INVALID_CODE utan att söka efter en vara")
    void scanReturnsInvalidCodeWhenCodeIsEmpty() {
        EmptyCodeReader codeReader = new EmptyCodeReader();
        BarcodeScanner.ProductLookup productLookup = new BarcodeScanner.ProductLookup() {
            @Override
            public Product findByCode(String code) {
                throw new AssertionError("Product lookup must not be called when the code is empty");
            }
        };
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productLookup);

        ScanResult result = scanner.scan();

        assertEquals(ScanResult.Status.INVALID_CODE, result.getStatus());
        assertNull(result.getProduct());
        assertEquals(1, codeReader.readCalls);
    }

    @Test
    @DisplayName("SC-02: En okänd kod ger UNKNOWN_PRODUCT utan vara")
    void scanReturnsUnknownProductWhenLookupFindsNoProduct() {
        FixedCodeReader codeReader = new FixedCodeReader("unknown");
        FixedProductLookup productLookup = new FixedProductLookup(null);
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productLookup);

        ScanResult result = scanner.scan();

        assertEquals(ScanResult.Status.UNKNOWN_PRODUCT, result.getStatus());
        assertNull(result.getProduct());
        assertEquals(1, codeReader.readCalls);
        assertEquals(1, productLookup.lookupCalls);
        assertEquals("unknown", productLookup.receivedCode);
    }

    @Test
    @DisplayName("SC-01: En matchande kod ger FOUND och exakt samma vara")
    void scanReturnsFoundWithMatchingProduct() {
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null);
        FixedCodeReader codeReader = new FixedCodeReader("000123");
        FixedProductLookup productLookup = new FixedProductLookup(milk);
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productLookup);

        ScanResult result = scanner.scan();

        assertEquals(ScanResult.Status.FOUND, result.getStatus());
        assertSame(milk, result.getProduct());
        assertEquals(1, codeReader.readCalls);
        assertEquals(1, productLookup.lookupCalls);
        assertEquals("000123", productLookup.receivedCode);
    }

    private static class FixedCodeReader implements BarcodeScanner.CodeReader {
        private final String code;
        private int readCalls;

        private FixedCodeReader(String code) {
            this.code = code;
        }

        @Override
        public String readCode() {
            readCalls++;
            return code;
        }
    }

    private static class FixedProductLookup implements BarcodeScanner.ProductLookup {
        private final Product product;
        private int lookupCalls;
        private String receivedCode;

        private FixedProductLookup(Product product) {
            this.product = product;
        }

        @Override
        public Product findByCode(String code) {
            lookupCalls++;
            receivedCode = code;
            return product;
        }
    }

    private static class EmptyCodeReader implements BarcodeScanner.CodeReader {
        private int readCalls;

        @Override
        public String readCode() {
            readCalls++;
            return "";
        }
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
