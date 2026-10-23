package kassasystem;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;
import kassasystem.receipt.Receipt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CashRegisterTest {

    private final ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);

    @Test
    @DisplayName("Kassan avvisar en saknad klocka")
    void constructorRejectsNullClock() {
        assertThrows(IllegalArgumentException.class, () -> new CashRegister(null));
    }

    @Test
    @DisplayName("Kvittot får tid och tidszon från kassans angivna klocka")
    void completePurchaseUsesSuppliedClockForReceiptTimestamp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-20T10:15:30Z"),
                ZoneId.of("Europe/Stockholm"));
        CashRegister register = new CashRegister(clock);
        register.startPurchase();

        Receipt receipt = register.completePurchase();

        LocalDateTime expectedTimestamp = LocalDateTime.of(2026, 10, 20, 12, 15, 30);
        assertEquals(expectedTimestamp, receipt.getTimestamp());
        assertTrue(receipt.toText().contains("Datum: 2026-10-20 12:15\n"));
    }

    @Test
    @DisplayName("Kassan startar ett aktivt köp med en tom korg")
    void startPurchaseCreatesCurrentActivePurchase() {
        CashRegister register = new CashRegister();

        Purchase purchase = register.startPurchase();

        assertSame(purchase, register.getCurrentPurchase());
        assertEquals(Purchase.Status.ACTIVE, purchase.getStatus());
        assertTrue(purchase.getBasket().isEmpty());
    }

    @Test
    @DisplayName("Ett nytt köp kan inte skriva över kassans pågående köp")
    void startPurchaseRejectsExistingCurrentPurchase() {
        CashRegister register = new CashRegister();
        Purchase purchase = register.startPurchase();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        purchase.getBasket().addProduct(milk);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.startPurchase());

        assertEquals("Cannot start a purchase while another purchase is current", exception.getMessage());
        assertSame(purchase, register.getCurrentPurchase());
        assertEquals(Purchase.Status.ACTIVE, purchase.getStatus());
        assertEquals(1, purchase.getBasket().getTotalQuantity());
        assertSame(milk, purchase.getBasket().getItems().get(0).getProduct());
    }

    @Test
    @DisplayName("Ett slutfört köp frigör kassan")
    void completePurchaseCompletesCurrentPurchaseAndFreesRegister() {
        CashRegister register = new CashRegister();
        Purchase purchase = register.startPurchase();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        purchase.getBasket().addProduct(milk);

        register.completePurchase();

        assertEquals(Purchase.Status.COMPLETED, purchase.getStatus());
        assertNull(register.getCurrentPurchase());
        assertEquals(1, purchase.getBasket().getTotalQuantity());
        assertSame(milk, purchase.getBasket().getItems().get(0).getProduct());

        Purchase nextPurchase = register.startPurchase();

        assertNotSame(purchase, nextPurchase);
        assertSame(nextPurchase, register.getCurrentPurchase());
        assertEquals(Purchase.Status.ACTIVE, nextPurchase.getStatus());
        assertTrue(nextPurchase.getBasket().isEmpty());
    }

    @Test
    @DisplayName("Ett slutfört köp returnerar ett kvitto med köpets varor, antal och totalpris")
    void completePurchaseReturnsReceiptWithPurchaseContents() {
        CashRegister register = new CashRegister();
        Purchase purchase = register.startPurchase();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        Product bread = new Product("Bread", new Money(Sek.SEK, 20.00), food);
        purchase.getBasket().addProduct(milk);
        purchase.getBasket().addProduct(milk);
        purchase.getBasket().addProduct(bread);

        Receipt receipt = register.completePurchase();

        assertEquals(2, receipt.getItems().size());
        assertEquals("Milk", receipt.getItems().get(0).getProductName());
        assertEquals(2, receipt.getItems().get(0).getQuantity());
        assertEquals(12.50, receipt.getItems().get(0).getUnitPrice().getAmount(), 0.001);
        assertEquals("Bread", receipt.getItems().get(1).getProductName());
        assertEquals(1, receipt.getItems().get(1).getQuantity());
        assertEquals(20.00, receipt.getItems().get(1).getUnitPrice().getAmount(), 0.001);
        assertEquals(45.00, receipt.getTotalPrice().getAmount(), 0.001);
        assertEquals(Sek.SEK, receipt.getTotalPrice().getCurrency());
        assertEquals(Purchase.Status.COMPLETED, purchase.getStatus());
        assertNull(register.getCurrentPurchase());
    }

    @Test
    @DisplayName("Det returnerade kvittot behåller köpets innehåll när den gamla korgen ändras")
    void returnedReceiptKeepsSnapshotWhenPurchaseBasketChanges() {
        CashRegister register = new CashRegister();
        Purchase purchase = register.startPurchase();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        purchase.getBasket().addProduct(milk);
        Receipt receipt = register.completePurchase();

        purchase.getBasket().addProduct(milk);

        assertEquals(2, purchase.getBasket().getTotalQuantity());
        assertEquals(1, receipt.getItems().size());
        assertEquals("Milk", receipt.getItems().get(0).getProductName());
        assertEquals(1, receipt.getItems().get(0).getQuantity());
        assertEquals(12.50, receipt.getTotalPrice().getAmount(), 0.001);
    }

    @Test
    @DisplayName("Ett fel vid kvittoskapandet behåller köpet aktivt i kassan")
    void completePurchaseKeepsPurchaseActiveWhenReceiptCreationFails() {
        CashRegister register = new CashRegister();
        Purchase purchase = register.startPurchase();
        Product invalidProduct = new Product("Milk", new Money(Sek.SEK, -1.00), food);
        purchase.getBasket().addProduct(invalidProduct);

        assertThrows(IllegalArgumentException.class, () -> register.completePurchase());

        assertSame(purchase, register.getCurrentPurchase());
        assertEquals(Purchase.Status.ACTIVE, purchase.getStatus());
        assertEquals(1, purchase.getBasket().getTotalQuantity());
        assertSame(invalidProduct, purchase.getBasket().getItems().get(0).getProduct());
    }

    @Test
    @DisplayName("Kassan kan inte slutföra när det saknas ett pågående köp")
    void completePurchaseRejectsMissingCurrentPurchase() {
        CashRegister register = new CashRegister();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.completePurchase());

        assertEquals("Cannot complete a purchase when there is no current purchase", exception.getMessage());
        assertNull(register.getCurrentPurchase());
    }

    @Test
    @DisplayName("Ett misslyckat slutförande behåller kassans pågående köp")
    void completePurchaseKeepsCurrentPurchaseWhenCompletionFails() {
        CashRegister register = new CashRegister();
        Purchase purchase = register.startPurchase();
        purchase.cancel();

        assertThrows(IllegalStateException.class, () -> register.completePurchase());

        assertSame(purchase, register.getCurrentPurchase());
        assertEquals(Purchase.Status.CANCELLED, purchase.getStatus());
    }

    @Test
    @DisplayName("Ett avbrutet köp frigör kassan")
    void cancelPurchaseCancelsCurrentPurchaseAndFreesRegister() {
        CashRegister register = new CashRegister();
        Purchase purchase = register.startPurchase();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        purchase.getBasket().addProduct(milk);

        register.cancelPurchase();

        assertEquals(Purchase.Status.CANCELLED, purchase.getStatus());
        assertNull(register.getCurrentPurchase());
        assertEquals(1, purchase.getBasket().getTotalQuantity());
        assertSame(milk, purchase.getBasket().getItems().get(0).getProduct());

        Purchase nextPurchase = register.startPurchase();

        assertNotSame(purchase, nextPurchase);
        assertSame(nextPurchase, register.getCurrentPurchase());
        assertEquals(Purchase.Status.ACTIVE, nextPurchase.getStatus());
        assertTrue(nextPurchase.getBasket().isEmpty());
    }

    @Test
    @DisplayName("Kassan kan inte avbryta när det saknas ett pågående köp")
    void cancelPurchaseRejectsMissingCurrentPurchase() {
        CashRegister register = new CashRegister();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.cancelPurchase());

        assertEquals("Cannot cancel a purchase when there is no current purchase", exception.getMessage());
        assertNull(register.getCurrentPurchase());
    }

    @Test
    @DisplayName("Ett misslyckat avbrytande behåller kassans pågående köp")
    void cancelPurchaseKeepsCurrentPurchaseWhenCancellationFails() {
        CashRegister register = new CashRegister();
        Purchase purchase = register.startPurchase();
        purchase.complete();

        assertThrows(IllegalStateException.class, () -> register.cancelPurchase());

        assertSame(purchase, register.getCurrentPurchase());
        assertEquals(Purchase.Status.COMPLETED, purchase.getStatus());
    }
}
