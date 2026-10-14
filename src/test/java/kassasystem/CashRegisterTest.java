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
        assertTrue(register.getPausedPurchases().isEmpty());
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
    @DisplayName("Ett pausat köp sparas med sina varor och frigör kassan")
    void pausePurchaseStoresPurchaseAndFreesRegister() {
        CashRegister register = new CashRegister();
        Purchase purchase = register.startPurchase();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null);
        purchase.getBasket().addProduct(milk);

        register.pausePurchase();

        assertNull(register.getCurrentPurchase());
        assertEquals(Purchase.Status.PAUSED, purchase.getStatus());
        assertEquals(1, register.getPausedPurchases().size());
        assertSame(purchase, register.getPausedPurchases().get(0));
        assertEquals(1, purchase.getBasket().getTotalQuantity());
        assertSame(milk, purchase.getBasket().getItems().get(0).getProduct());
    }

    @Test
    @DisplayName("Kassan kan inte pausa när det saknas ett pågående köp")
    void pausePurchaseRejectsMissingCurrentPurchase() {
        CashRegister register = new CashRegister();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> register.pausePurchase());

        assertEquals("Cannot pause a purchase when there is no current purchase", exception.getMessage());
        assertNull(register.getCurrentPurchase());
        assertTrue(register.getPausedPurchases().isEmpty());
    }

    @Test
    @DisplayName("Kassan kan starta och pausa fler köp utan att förlora tidigare köp")
    void startAndPauseAnotherPurchaseKeepsEarlierPausedPurchase() {
        CashRegister register = new CashRegister();
        Purchase firstPurchase = register.startPurchase();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null);
        firstPurchase.getBasket().addProduct(milk);
        register.pausePurchase();

        Purchase secondPurchase = register.startPurchase();
        register.pausePurchase();

        assertNotSame(firstPurchase, secondPurchase);
        assertNotSame(firstPurchase.getBasket(), secondPurchase.getBasket());
        assertNull(register.getCurrentPurchase());
        assertEquals(2, register.getPausedPurchases().size());
        assertSame(firstPurchase, register.getPausedPurchases().get(0));
        assertSame(secondPurchase, register.getPausedPurchases().get(1));
        assertEquals(Purchase.Status.PAUSED, firstPurchase.getStatus());
        assertEquals(Purchase.Status.PAUSED, secondPurchase.getStatus());
        assertEquals(1, firstPurchase.getBasket().getTotalQuantity());
        assertTrue(secondPurchase.getBasket().isEmpty());
    }
}
