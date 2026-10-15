package kassasystem;

import kassasystem.basket.Basket;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.scanning.BarcodeScanner;
import kassasystem.scanning.EAN;
import kassasystem.scanning.ScanResult;
import kassasystem.testdoubles.FixedCodeReader;
import kassasystem.testdoubles.FixedProductCatalog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CashRegisterScanningTest {

    @Test
    @DisplayName("En lyckad scanning lägger till en vara i det pågående köpet")
    void scanProductAddsFoundProductToCurrentPurchase() {
        EAN ean = new EAN("0123456789012");
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null, ean);
        FixedCodeReader codeReader = new FixedCodeReader("0123456789012");
        FixedProductCatalog productCatalog = new FixedProductCatalog(milk);
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);
        CashRegister register = new CashRegister();
        Purchase purchase = register.startPurchase();

        ScanResult result = register.scanProduct(scanner);

        Basket basket = purchase.getBasket();
        assertEquals(ScanResult.Status.FOUND, result.getStatus());
        assertNotNull(productCatalog.getReceivedEan());
        assertEquals("0123456789012", productCatalog.getReceivedEan().getCode());
        assertSame(purchase, register.getCurrentPurchase());
        assertEquals(1, basket.getItems().size());
        assertSame(milk, basket.getItems().get(0).getProduct());
        assertEquals(1, basket.getItems().get(0).getQuantity());
        assertEquals(12.50, basket.getTotalPrice().getAmount(), 0.001);
        assertEquals(Sek.SEK, basket.getTotalPrice().getCurrency());
    }

    @Test
    @DisplayName("En oläsbar kod lämnar det pågående köpet och dess varukorg oförändrade")
    void scanProductKeepsBasketUnchangedWhenCodeCannotBeRead() {
        EAN ean = new EAN("0123456789012");
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null, ean);
        FixedCodeReader codeReader = new FixedCodeReader(null);
        FixedProductCatalog productCatalog = new FixedProductCatalog(milk);
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);
        CashRegister register = new CashRegister();
        Purchase purchase = register.startPurchase();
        Basket basket = purchase.getBasket();
        basket.addProduct(milk);

        ScanResult result = register.scanProduct(scanner);

        assertEquals(ScanResult.Status.FAILED_READ, result.getStatus());
        assertNull(result.getProduct());
        assertSame(purchase, register.getCurrentPurchase());
        assertEquals(Purchase.Status.ACTIVE, purchase.getStatus());
        assertEquals(1, basket.getItems().size());
        assertSame(milk, basket.getItems().get(0).getProduct());
        assertEquals(1, basket.getItems().get(0).getQuantity());
        assertEquals(12.50, basket.getTotalPrice().getAmount(), 0.001);
        assertEquals(Sek.SEK, basket.getTotalPrice().getCurrency());
        assertEquals(1, codeReader.getReadCalls());
        assertEquals(0, productCatalog.getLookupCalls());
    }

    @Test
    @DisplayName("En ogiltig EAN-kod lämnar det pågående köpet och dess varukorg oförändrade")
    void scanProductKeepsBasketUnchangedWhenCodeIsInvalid() {
        EAN ean = new EAN("0123456789012");
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null, ean);
        FixedCodeReader codeReader = new FixedCodeReader("123");
        FixedProductCatalog productCatalog = new FixedProductCatalog(milk);
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);
        CashRegister register = new CashRegister();
        Purchase purchase = register.startPurchase();
        Basket basket = purchase.getBasket();
        basket.addProduct(milk);

        ScanResult result = register.scanProduct(scanner);

        assertEquals(ScanResult.Status.INVALID_CODE, result.getStatus());
        assertNull(result.getProduct());
        assertSame(purchase, register.getCurrentPurchase());
        assertEquals(Purchase.Status.ACTIVE, purchase.getStatus());
        assertEquals(1, basket.getItems().size());
        assertSame(milk, basket.getItems().get(0).getProduct());
        assertEquals(1, basket.getItems().get(0).getQuantity());
        assertEquals(12.50, basket.getTotalPrice().getAmount(), 0.001);
        assertEquals(Sek.SEK, basket.getTotalPrice().getCurrency());
        assertEquals(1, codeReader.getReadCalls());
        assertEquals(0, productCatalog.getLookupCalls());
    }

    @Test
    @DisplayName("En okänd vara lämnar det pågående köpet och dess varukorg oförändrade")
    void scanProductKeepsBasketUnchangedWhenProductIsUnknown() {
        EAN ean = new EAN("0123456789012");
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null, ean);
        FixedCodeReader codeReader = new FixedCodeReader("4006381333931");
        FixedProductCatalog productCatalog = new FixedProductCatalog(null);
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);
        CashRegister register = new CashRegister();
        Purchase purchase = register.startPurchase();
        Basket basket = purchase.getBasket();
        basket.addProduct(milk);

        ScanResult result = register.scanProduct(scanner);

        assertEquals(ScanResult.Status.UNKNOWN_PRODUCT, result.getStatus());
        assertNull(result.getProduct());
        assertSame(purchase, register.getCurrentPurchase());
        assertEquals(Purchase.Status.ACTIVE, purchase.getStatus());
        assertEquals(1, basket.getItems().size());
        assertSame(milk, basket.getItems().get(0).getProduct());
        assertEquals(1, basket.getItems().get(0).getQuantity());
        assertEquals(12.50, basket.getTotalPrice().getAmount(), 0.001);
        assertEquals(Sek.SEK, basket.getTotalPrice().getCurrency());
        assertEquals(1, codeReader.getReadCalls());
        assertEquals(1, productCatalog.getLookupCalls());
        assertNotNull(productCatalog.getReceivedEan());
        assertEquals("4006381333931", productCatalog.getReceivedEan().getCode());
    }
}
