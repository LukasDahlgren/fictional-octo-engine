package kassasystem;

import kassasystem.basket.Basket;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;
import kassasystem.scanning.BarcodeScanner;
import kassasystem.scanning.EAN;
import kassasystem.scanning.ScanResult;
import kassasystem.testdoubles.FixedCodeReader;
import kassasystem.testdoubles.FixedProductCatalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CashRegisterScanningTest {

    private final ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);

    private CashRegister register;
    private Product milk;
    private FixedProductCatalog productCatalog;

    @BeforeEach
    void setUp() {
        register = new CashRegister();
        EAN ean = new EAN("0123456789012");
        milk = new Product("Milk", new Money(Sek.SEK, 12.50), food, ean);
        productCatalog = new FixedProductCatalog(milk);
    }

    @Test
    @DisplayName("En lyckad scanning lägger till en vara i det pågående köpet")
    void scanProductAddsFoundProductToCurrentPurchase() {
        FixedCodeReader codeReader = new FixedCodeReader("0123456789012");
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);
        Purchase purchase = register.startPurchase();

        ScanResult result = register.scanProduct(scanner);

        assertAll("Found product is added to the current purchase",
                () -> assertEquals(ScanResult.Status.FOUND, result.getStatus()),
                () -> assertSame(milk, result.getProduct()),
                () -> assertSame(purchase, register.getCurrentPurchase()),
                () -> assertSingleMilkBasket(purchase.getBasket())
        );
    }

    @Test
    @DisplayName("En oläsbar kod lämnar det pågående köpet och dess varukorg oförändrade")
    void scanProductKeepsBasketUnchangedWhenCodeCannotBeRead() {
        FixedCodeReader codeReader = new FixedCodeReader(null);
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);
        Purchase purchase = register.startPurchase();
        Basket basket = purchase.getBasket();
        basket.addProduct(milk);

        ScanResult result = register.scanProduct(scanner);

        assertAll("Failed scan preserves the active purchase",
                () -> assertEquals(ScanResult.Status.FAILED_READ, result.getStatus()),
                () -> assertNull(result.getProduct()),
                () -> assertPurchasePreserved(purchase, basket, Purchase.Status.ACTIVE)
        );
    }

    @Test
    @DisplayName("En ogiltig EAN-kod lämnar det pågående köpet och dess varukorg oförändrade")
    void scanProductKeepsBasketUnchangedWhenCodeIsInvalid() {
        FixedCodeReader codeReader = new FixedCodeReader("123");
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);
        Purchase purchase = register.startPurchase();
        Basket basket = purchase.getBasket();
        basket.addProduct(milk);

        ScanResult result = register.scanProduct(scanner);

        assertAll("Failed scan preserves the active purchase",
                () -> assertEquals(ScanResult.Status.INVALID_CODE, result.getStatus()),
                () -> assertNull(result.getProduct()),
                () -> assertPurchasePreserved(purchase, basket, Purchase.Status.ACTIVE)
        );
    }

    @Test
    @DisplayName("En okänd vara lämnar det pågående köpet och dess varukorg oförändrade")
    void scanProductKeepsBasketUnchangedWhenProductIsUnknown() {
        FixedCodeReader codeReader = new FixedCodeReader("4006381333931");
        productCatalog = new FixedProductCatalog(null);
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);
        Purchase purchase = register.startPurchase();
        Basket basket = purchase.getBasket();
        basket.addProduct(milk);

        ScanResult result = register.scanProduct(scanner);

        assertAll("Failed scan preserves the active purchase",
                () -> assertEquals(ScanResult.Status.UNKNOWN_PRODUCT, result.getStatus()),
                () -> assertNull(result.getProduct()),
                () -> assertPurchasePreserved(purchase, basket, Purchase.Status.ACTIVE)
        );
    }

    @Test
    @DisplayName("Utan ett pågående köp avvisas scanning innan kodläsaren används")
    void scanProductRejectsMissingCurrentPurchaseWithoutReadingCode() {
        FixedCodeReader codeReader = new FixedCodeReader("0123456789012");
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);

        assertThrows(IllegalStateException.class, () -> register.scanProduct(scanner));

        assertAll("Missing purchase is rejected before reading a code",
                () -> assertEquals(0, codeReader.getReadCalls()),
                () -> assertNull(register.getCurrentPurchase())
        );
    }

    @Test
    @DisplayName("Ett slutfört köp avvisar scanning innan kodläsaren används och behåller varukorgen")
    void scanProductRejectsCompletedPurchaseWithoutReadingCode() {
        FixedCodeReader codeReader = new FixedCodeReader("0123456789012");
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);
        Purchase purchase = register.startPurchase();
        Basket basket = purchase.getBasket();
        basket.addProduct(milk);
        register.completePurchase();

        assertThrows(IllegalStateException.class, () -> register.scanProduct(scanner));

        assertAll("Completed purchase is preserved without reading a code",
                () -> assertEquals(0, codeReader.getReadCalls()),
                () -> assertPurchasePreserved(purchase, basket, Purchase.Status.COMPLETED)
        );
    }

    @Test
    @DisplayName("Ett avbrutet köp avvisar scanning innan kodläsaren används och behåller varukorgen")
    void scanProductRejectsCancelledPurchaseWithoutReadingCode() {
        FixedCodeReader codeReader = new FixedCodeReader("0123456789012");
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);
        Purchase purchase = register.startPurchase();
        Basket basket = purchase.getBasket();
        basket.addProduct(milk);
        register.cancelPurchase();

        assertThrows(IllegalStateException.class, () -> register.scanProduct(scanner));

        assertAll("Cancelled purchase is preserved without reading a code",
                () -> assertEquals(0, codeReader.getReadCalls()),
                () -> assertPurchasePreserved(purchase, basket, Purchase.Status.CANCELLED)
        );
    }

    @Test
    @DisplayName("Ett scannerfel skickas vidare utan att ändra det pågående köpet eller varukorgen")
    void scanProductPreservesPurchaseWhenScannerThrows() {
        IllegalStateException failure = new IllegalStateException("Scanner failed");
        FixedCodeReader codeReader = new FixedCodeReader("0123456789012");
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog) {
            @Override
            public ScanResult scan() {
                throw failure;
            }
        };
        Purchase purchase = register.startPurchase();
        Basket basket = purchase.getBasket();
        basket.addProduct(milk);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.scanProduct(scanner));

        assertAll("Scanner failure preserves the active purchase",
                () -> assertSame(failure, exception),
                () -> assertPurchasePreserved(purchase, basket, Purchase.Status.ACTIVE)
        );
    }

    private void assertPurchasePreserved(Purchase purchase, Basket basket, Purchase.Status expectedStatus) {
        if (expectedStatus == Purchase.Status.ACTIVE) {
            assertSame(purchase, register.getCurrentPurchase());
        } else {
            assertNull(register.getCurrentPurchase());
        }
        assertAll("Purchase status and basket are preserved",
                () -> assertEquals(expectedStatus, purchase.getStatus()),
                () -> assertSame(basket, purchase.getBasket()),
                () -> assertSingleMilkBasket(purchase.getBasket())
        );
    }

    private void assertSingleMilkBasket(Basket basket) {
        assertEquals(1, basket.getItems().size(), "Number of basket rows");

        assertAll("Basket contains one milk",
                () -> assertSame(milk, basket.getItems().get(0).getProduct()),
                () -> assertEquals(1, basket.getItems().get(0).getQuantity()),
                () -> assertEquals(12.50, basket.getTotalPrice().getAmount(), 0.001),
                () -> assertEquals(Sek.SEK, basket.getTotalPrice().getCurrency())
        );
    }
}
