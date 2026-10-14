import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BarcodeScannerTest {

    @Test
    @DisplayName("En oläsbar kod ger FAILED_READ utan att söka efter en vara")
    void scanReturnsFailedReadWhenNoCodeIsRead() {
        UnreadableCodeReader codeReader = new UnreadableCodeReader();
        ProductCatalog productCatalog = new ProductCatalog() {
            @Override
            public Product findByEAn(EAN ean) {
                throw new AssertionError("Product lookup must not be called when no code is read");
            }
        };
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);

        ScanResult result = scanner.scan();

        assertEquals(ScanResult.Status.FAILED_READ, result.getStatus());
        assertNull(result.getProduct());
        assertEquals(1, codeReader.readCalls);
    }

    @Test
    @DisplayName("SC-04: En tom kod ger INVALID_CODE utan att söka efter en vara")
    void scanReturnsInvalidCodeWhenCodeIsEmpty() {
        EmptyCodeReader codeReader = new EmptyCodeReader();
        ProductCatalog productCatalog = new ProductCatalog() {
            @Override
            public Product findByEAn(EAN ean) {
                throw new AssertionError("Product lookup must not be called when the code is empty");
            }
        };
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);

        ScanResult result = scanner.scan();

        assertEquals(ScanResult.Status.INVALID_CODE, result.getStatus());
        assertNull(result.getProduct());
        assertEquals(1, codeReader.readCalls);
    }

    @Test
    @DisplayName("SC-02: En okänd kod ger UNKNOWN_PRODUCT utan vara")
    void scanReturnsUnknownProductWhenLookupFindsNoProduct() {
        FixedCodeReader codeReader = new FixedCodeReader("4006381333931");
        FixedProductCatalog productCatalog = new FixedProductCatalog(null);
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);

        ScanResult result = scanner.scan();

        assertEquals(ScanResult.Status.UNKNOWN_PRODUCT, result.getStatus());
        assertNull(result.getProduct());
        assertEquals(1, codeReader.readCalls);
        assertEquals(1, productCatalog.lookupCalls);
        assertNotNull(productCatalog.receivedEan);
        assertEquals("4006381333931", productCatalog.receivedEan.getCode());
    }

    @Test
    @DisplayName("SC-01: En matchande kod ger FOUND och exakt samma vara")
    void scanReturnsFoundWithMatchingProduct() {
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null);
        FixedCodeReader codeReader = new FixedCodeReader("0123456789012");
        FixedProductCatalog productCatalog = new FixedProductCatalog(milk);
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);

        ScanResult result = scanner.scan();

        assertEquals(ScanResult.Status.FOUND, result.getStatus());
        assertSame(milk, result.getProduct());
        assertEquals(1, codeReader.readCalls);
        assertEquals(1, productCatalog.lookupCalls);
        assertNotNull(productCatalog.receivedEan);
        assertEquals("0123456789012", productCatalog.receivedEan.getCode());
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

    private static class FixedProductCatalog extends ProductCatalog {
        private final Product product;
        private int lookupCalls;
        private EAN receivedEan;

        private FixedProductCatalog(Product product) {
            this.product = product;
        }

        @Override
        public Product findByEAn(EAN ean) {
            lookupCalls++;
            receivedEan = ean;
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
