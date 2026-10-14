package kassasystem;

import kassasystem.basket.Basket;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurchaseTest {

    @Test
    @DisplayName("Nya köp börjar aktiva och har egna tomma korgar")
    void newPurchasesStartActiveWithIndependentBaskets() {
        Purchase firstPurchase = new Purchase();
        Purchase secondPurchase = new Purchase();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null);

        firstPurchase.getBasket().addProduct(milk);

        assertEquals(Purchase.Status.ACTIVE, firstPurchase.getStatus());
        assertEquals(Purchase.Status.ACTIVE, secondPurchase.getStatus());
        assertNotSame(firstPurchase.getBasket(), secondPurchase.getBasket());
        assertEquals(1, firstPurchase.getBasket().getTotalQuantity());
        assertTrue(secondPurchase.getBasket().isEmpty());
    }

    @Test
    @DisplayName("Ett köp behåller den angivna korgen och dess varor")
    void constructorKeepsSuppliedBasketAndItsItems() {
        Basket basket = new Basket();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null);
        basket.addProduct(milk);

        Purchase purchase = new Purchase(basket);

        assertSame(basket, purchase.getBasket());
        assertEquals(Purchase.Status.ACTIVE, purchase.getStatus());
        assertEquals(1, purchase.getBasket().getTotalQuantity());
        assertSame(milk, purchase.getBasket().getItems().get(0).getProduct());
    }

    @Test
    @DisplayName("Ett köp kan inte skapas utan en korg")
    void constructorRejectsMissingBasket() {
        assertThrows(IllegalArgumentException.class, () -> new Purchase(null));
    }

    @Test
    @DisplayName("Ett aktivt köp kan pausas utan att ändra korgen")
    void pauseChangesStatusAndKeepsBasketItems() {
        Basket basket = new Basket();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null);
        basket.addProduct(milk);
        Purchase purchase = new Purchase(basket);

        purchase.pause();

        assertEquals(Purchase.Status.PAUSED, purchase.getStatus());
        assertSame(basket, purchase.getBasket());
        assertEquals(1, basket.getTotalQuantity());
        assertSame(milk, basket.getItems().get(0).getProduct());
    }

    @Test
    @DisplayName("Ett redan pausat köp kan inte pausas igen")
    void pauseRejectsAlreadyPausedPurchase() {
        Purchase purchase = new Purchase();
        purchase.pause();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> purchase.pause());

        assertEquals("Only an active purchase can be paused", exception.getMessage());
        assertEquals(Purchase.Status.PAUSED, purchase.getStatus());
    }
}
