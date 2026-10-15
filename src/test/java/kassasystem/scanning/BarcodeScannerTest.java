package kassasystem.scanning;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCatalog;
import kassasystem.testdoubles.FixedCodeReader;
import kassasystem.testdoubles.FixedProductCatalog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BarcodeScannerTest {

    @Test
    @DisplayName("En oläsbar kod ger FAILED_READ utan att söka efter en vara")
    void scanReturnsFailedReadWhenNoCodeIsRead() {
        FixedCodeReader codeReader = new FixedCodeReader(null);
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
        assertEquals(1, codeReader.getReadCalls());
    }

    @ParameterizedTest(name = "Ogiltig kod: [{0}]")
    @ValueSource(strings = {
            "", " ", "\t", "\n", "123", "40063813339310",
            "40063813339A1", "4006381333932", "４００６３８１３３３９３１"
    })
    @DisplayName("Ogiltiga EAN-koder ger INVALID_CODE utan att söka efter en vara")
    void scanReturnsInvalidCodeWithoutProductLookup(String code) {
        FixedCodeReader codeReader = new FixedCodeReader(code);
        FixedProductCatalog productCatalog = new FixedProductCatalog(null);
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);

        ScanResult result = scanner.scan();

        assertEquals(ScanResult.Status.INVALID_CODE, result.getStatus());
        assertNull(result.getProduct());
        assertEquals(1, codeReader.getReadCalls());
        assertEquals(0, productCatalog.getLookupCalls());
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
        assertEquals(1, codeReader.getReadCalls());
        assertEquals(1, productCatalog.getLookupCalls());
        assertNotNull(productCatalog.getReceivedEan());
        assertEquals("4006381333931", productCatalog.getReceivedEan().getCode());
    }

    @Test
    @DisplayName("SC-01: En matchande kod ger FOUND och exakt samma vara")
    void scanReturnsFoundWithMatchingProduct() {
        EAN ean = new EAN("0123456789012");
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null, ean);
        FixedCodeReader codeReader = new FixedCodeReader("0123456789012");
        FixedProductCatalog productCatalog = new FixedProductCatalog(milk);
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);

        ScanResult result = scanner.scan();

        assertEquals(ScanResult.Status.FOUND, result.getStatus());
        assertSame(milk, result.getProduct());
        assertEquals(1, codeReader.getReadCalls());
        assertEquals(1, productCatalog.getLookupCalls());
        assertNotNull(productCatalog.getReceivedEan());
        assertEquals("0123456789012", productCatalog.getReceivedEan().getCode());
    }

    @Test
    @DisplayName("Ett fel från kodläsaren skickas vidare utan att söka efter en vara")
    void scanPropagatesCodeReaderFailureWithoutProductLookup() {
        IllegalArgumentException failure = new IllegalArgumentException("Reader failed");
        BarcodeScanner.CodeReader codeReader = new BarcodeScanner.CodeReader() {
            @Override
            public String readCode() {
                throw failure;
            }
        };
        FixedProductCatalog productCatalog = new FixedProductCatalog(null);
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scanner.scan());

        assertSame(failure, exception);
        assertEquals(0, productCatalog.getLookupCalls());
    }

    @Test
    @DisplayName("Ett fel från varukatalogen skickas vidare")
    void scanPropagatesProductCatalogFailure() {
        FixedCodeReader codeReader = new FixedCodeReader("4006381333931");
        IllegalArgumentException failure = new IllegalArgumentException("Catalog failed");
        ProductCatalog productCatalog = new ProductCatalog() {
            @Override
            public Product findByEAn(EAN ean) {
                throw failure;
            }
        };
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> scanner.scan());

        assertSame(failure, exception);
        assertEquals(1, codeReader.getReadCalls());
    }
}
