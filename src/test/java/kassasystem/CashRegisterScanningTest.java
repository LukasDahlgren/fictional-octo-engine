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
}
