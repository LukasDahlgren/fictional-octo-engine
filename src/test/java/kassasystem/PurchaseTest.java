package kassasystem;

import kassasystem.basket.Basket;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurchaseTest {

    private final ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);

    @Test
    @DisplayName("Nya köp börjar aktiva och har egna tomma korgar")
    void newPurchasesStartActiveWithIndependentBaskets() {
        Purchase firstPurchase = new Purchase();
        Purchase secondPurchase = new Purchase();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);

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
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
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
    @DisplayName("Ett aktivt köp slutförs med samma korg och varor")
    void completeChangesStatusAndKeepsBasketItems() {
        Basket basket = new Basket();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        basket.addProduct(milk);
        Purchase purchase = new Purchase(basket);

        purchase.complete();

        assertEquals(Purchase.Status.COMPLETED, purchase.getStatus());
        assertSame(basket, purchase.getBasket());
        assertEquals(1, basket.getTotalQuantity());
        assertSame(milk, basket.getItems().get(0).getProduct());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = Purchase.Status.class, names = {"COMPLETED", "CANCELLED"})
    @DisplayName("Bara aktiva köp kan slutföras")
    void completeRejectsInactivePurchase(Purchase.Status status) {
        Purchase purchase = createPurchaseWithStatus(status);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> purchase.complete());

        assertEquals("Only active purchases can be completed", exception.getMessage());
        assertEquals(status, purchase.getStatus());
    }

    @Test
    @DisplayName("Ett aktivt köp avbryts med samma korg och varor")
    void cancelChangesStatusAndKeepsBasketItems() {
        Basket basket = new Basket();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        basket.addProduct(milk);
        Purchase purchase = new Purchase(basket);

        purchase.cancel();

        assertEquals(Purchase.Status.CANCELLED, purchase.getStatus());
        assertSame(basket, purchase.getBasket());
        assertEquals(1, basket.getTotalQuantity());
        assertSame(milk, basket.getItems().get(0).getProduct());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = Purchase.Status.class, names = {"COMPLETED", "CANCELLED"})
    @DisplayName("Slutförda och avbrutna köp kan inte avbrytas")
    void cancelRejectsFinishedPurchase(Purchase.Status status) {
        Purchase purchase = createPurchaseWithStatus(status);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> purchase.cancel());

        assertEquals("Only active purchases can be cancelled", exception.getMessage());
        assertEquals(status, purchase.getStatus());
    }

    private Purchase createPurchaseWithStatus(Purchase.Status status) {
        Purchase purchase = new Purchase();
        if (status == Purchase.Status.COMPLETED) {
            purchase.complete();
        } else if (status == Purchase.Status.CANCELLED) {
            purchase.cancel();
        }
        return purchase;
    }
}
