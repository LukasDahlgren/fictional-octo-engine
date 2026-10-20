package kassasystem;

import kassasystem.basket.Basket;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCatalog;
import kassasystem.scanning.BarcodeScanner;
import kassasystem.scanning.EAN;
import kassasystem.scanning.ScanResult;
import kassasystem.testdoubles.FixedCodeReader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CashRegisterScanningIntegrationTest {

    @Test
    @DisplayName("Två scanningar via den riktiga produktkatalogen ger en varurad med antal två och rätt totalpris")
    void scanProductTwiceWithRealCatalogGroupsProductInBasket() {
        EAN ean = new EAN("0123456789012");
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null, ean);
        ProductCatalog productCatalog = new ProductCatalog();
        productCatalog.add(milk);
        FixedCodeReader codeReader = new FixedCodeReader("0123456789012");
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);
        CashRegister register = new CashRegister();
        Purchase purchase = register.startPurchase();

        ScanResult firstResult = register.scanProduct(scanner);
        ScanResult secondResult = register.scanProduct(scanner);

        Basket basket = purchase.getBasket();
        assertEquals(ScanResult.Status.FOUND, firstResult.getStatus());
        assertEquals(ScanResult.Status.FOUND, secondResult.getStatus());
        assertSame(milk, firstResult.getProduct());
        assertSame(milk, secondResult.getProduct());
        assertEquals(1, basket.getItems().size());
        assertSame(milk, basket.getItems().get(0).getProduct());
        assertEquals(2, basket.getItems().get(0).getQuantity());
        assertEquals(25.00, basket.getTotalPrice().getAmount(), 0.001);
        assertEquals(Sek.SEK, basket.getTotalPrice().getCurrency());
    }
}
