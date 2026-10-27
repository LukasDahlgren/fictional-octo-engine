package kassasystem.scanning;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;
import kassasystem.testdoubles.FixedCodeReader;
import kassasystem.testdoubles.FixedProductCatalog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BarcodeScannerTest {

    private final ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);

    @Test
    @DisplayName("Konstruktorn avvisar en saknad kodläsare")
    void constructorRejectsMissingCodeReader() {
        ProductCatalog productCatalog = new FixedProductCatalog(null);

        assertThrows(IllegalArgumentException.class,
                () -> new BarcodeScanner(null, productCatalog));
    }

    @Test
    @DisplayName("Konstruktorn avvisar en saknad produktkatalog")
    void constructorRejectsMissingProductCatalog() {
        FixedCodeReader codeReader = new FixedCodeReader("4006381333931");

        assertThrows(IllegalArgumentException.class,
                () -> new BarcodeScanner(codeReader, null));
    }

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

        assertAll("Unreadable code returns failure after one read",
                () -> assertEquals(ScanResult.Status.FAILED_READ, result.getStatus()),
                () -> assertNull(result.getProduct()),
                () -> assertEquals(1, codeReader.getReadCalls())
        );
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

        assertAll("Invalid code returns failure without a catalog lookup",
                () -> assertEquals(ScanResult.Status.INVALID_CODE, result.getStatus()),
                () -> assertNull(result.getProduct()),
                () -> assertEquals(1, codeReader.getReadCalls()),
                () -> assertEquals(0, productCatalog.getLookupCalls())
        );
    }

    @Test
    @DisplayName("SC-02: En okänd kod ger UNKNOWN_PRODUCT utan vara")
    void scanReturnsUnknownProductWhenLookupFindsNoProduct() {
        FixedCodeReader codeReader = new FixedCodeReader("4006381333931");
        FixedProductCatalog productCatalog = new FixedProductCatalog(null);
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);

        ScanResult result = scanner.scan();

        assertAll("Unknown code is looked up once and returns no product",
                () -> assertEquals(ScanResult.Status.UNKNOWN_PRODUCT, result.getStatus()),
                () -> assertNull(result.getProduct()),
                () -> assertEquals(1, codeReader.getReadCalls()),
                () -> assertEquals(1, productCatalog.getLookupCalls()),
                () -> assertEquals(new EAN("4006381333931"), productCatalog.getReceivedEan())
        );
    }

    @Test
    @DisplayName("SC-01: En matchande kod ger FOUND och exakt samma vara")
    void scanReturnsFoundWithMatchingProduct() {
        EAN ean = new EAN("0123456789012");
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food, ean);
        FixedCodeReader codeReader = new FixedCodeReader("0123456789012");
        FixedProductCatalog productCatalog = new FixedProductCatalog(milk);
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);

        ScanResult result = scanner.scan();

        assertAll("Matching code is looked up once and returns the product",
                () -> assertEquals(ScanResult.Status.FOUND, result.getStatus()),
                () -> assertSame(milk, result.getProduct()),
                () -> assertEquals(1, codeReader.getReadCalls()),
                () -> assertEquals(1, productCatalog.getLookupCalls()),
                () -> assertEquals(ean, productCatalog.getReceivedEan())
        );
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

        assertAll("Reader failure is propagated without a catalog lookup",
                () -> assertSame(failure, exception),
                () -> assertEquals(0, productCatalog.getLookupCalls())
        );
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

        assertAll("Catalog failure is propagated after one read",
                () -> assertSame(failure, exception),
                () -> assertEquals(1, codeReader.getReadCalls())
        );
    }
}
