package kassasystem;

import kassasystem.basket.Basket;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCatalog;
import kassasystem.scanning.BarcodeScanner;
import kassasystem.scanning.EAN;
import kassasystem.scanning.ScanResult;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CashRegisterScanningTest {

    @Test
    @DisplayName("En lyckad scanning lägger till en vara i det pågående köpet")
    void scanProductAddsFoundProductToCurrentPurchase() {
        EAN ean = new EAN("0123456789012");
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null, ean);
        BarcodeScanner.CodeReader codeReader = new BarcodeScanner.CodeReader() {
            @Override
            public String readCode() {
                return "0123456789012";
            }
        };
        ProductCatalog productCatalog = new ProductCatalog() {
            @Override
            public Product findByEAn(EAN scannedEan) {
                if (scannedEan.getCode().equals("0123456789012")) {
                    return milk;
                }
                return null;
            }
        };
        BarcodeScanner scanner = new BarcodeScanner(codeReader, productCatalog);
        CashRegister register = new CashRegister();
        Purchase purchase = register.startPurchase();

        ScanResult result = register.scanProduct(scanner);

        Basket basket = purchase.getBasket();
        assertEquals(ScanResult.Status.FOUND, result.getStatus());
        assertSame(purchase, register.getCurrentPurchase());
        assertEquals(1, basket.getItems().size());
        assertSame(milk, basket.getItems().get(0).getProduct());
        assertEquals(1, basket.getItems().get(0).getQuantity());
        assertEquals(12.50, basket.getTotalPrice().getAmount(), 0.001);
        assertEquals(Sek.SEK, basket.getTotalPrice().getCurrency());
    }
}
