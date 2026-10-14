package kassasystem;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CashRegisterTest {

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
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null);
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
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null);
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
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null);
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
