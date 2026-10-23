package kassasystem;

import kassasystem.basket.Basket;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurchaseTest {

    private final ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);

    @Test
    @DisplayName("Ett nytt köp börjar aktivt med en tom korg")
    void newPurchaseStartsActiveWithEmptyBasket() {
        Purchase purchase = new Purchase();

        assertAll("New purchase starts active and empty",
                () -> assertEquals(Purchase.Status.ACTIVE, purchase.getStatus()),
                () -> assertTrue(purchase.getBasket().isEmpty())
        );
    }

    @Test
    @DisplayName("Nya köp har oberoende korgar")
    void newPurchasesHaveIndependentBaskets() {
        Purchase firstPurchase = new Purchase();
        Purchase secondPurchase = new Purchase();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);

        firstPurchase.getBasket().addProduct(milk);

        assertAll("Adding to one purchase leaves the other basket empty",
                () -> assertNotSame(firstPurchase.getBasket(), secondPurchase.getBasket()),
                () -> assertEquals(1, firstPurchase.getBasket().getTotalQuantity()),
                () -> assertTrue(secondPurchase.getBasket().isEmpty())
        );
    }

    @Test
    @DisplayName("Ett köp med angiven korg börjar aktivt")
    void constructorWithSuppliedBasketStartsActive() {
        Basket basket = new Basket();

        Purchase purchase = new Purchase(basket);

        assertEquals(Purchase.Status.ACTIVE, purchase.getStatus());
    }

    @Test
    @DisplayName("Ett köp behåller den angivna korgen och dess varor")
    void constructorKeepsSuppliedBasketAndItsItems() {
        Basket basket = new Basket();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        basket.addProduct(milk);

        Purchase purchase = new Purchase(basket);

        assertBasketPreserved(purchase, basket, milk);
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

        assertAll("Completed purchase keeps its basket and contents",
                () -> assertEquals(Purchase.Status.COMPLETED, purchase.getStatus()),
                () -> assertBasketPreserved(purchase, basket, milk)
        );
    }

    @Test
    @DisplayName("Ett slutfört köp kan inte slutföras igen")
    void completeRejectsCompletedPurchase() {
        Basket basket = new Basket();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        basket.addProduct(milk);
        Purchase purchase = new Purchase(basket);
        purchase.complete();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> purchase.complete());

        assertAll("Rejected transition preserves the purchase",
                () -> assertEquals("Only active purchases can be completed", exception.getMessage()),
                () -> assertEquals(Purchase.Status.COMPLETED, purchase.getStatus()),
                () -> assertBasketPreserved(purchase, basket, milk)
        );
    }

    @Test
    @DisplayName("Ett avbrutet köp kan inte slutföras")
    void completeRejectsCancelledPurchase() {
        Basket basket = new Basket();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        basket.addProduct(milk);
        Purchase purchase = new Purchase(basket);
        purchase.cancel();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> purchase.complete());

        assertAll("Rejected transition preserves the purchase",
                () -> assertEquals("Only active purchases can be completed", exception.getMessage()),
                () -> assertEquals(Purchase.Status.CANCELLED, purchase.getStatus()),
                () -> assertBasketPreserved(purchase, basket, milk)
        );
    }

    @Test
    @DisplayName("Ett aktivt köp avbryts med samma korg och varor")
    void cancelChangesStatusAndKeepsBasketItems() {
        Basket basket = new Basket();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        basket.addProduct(milk);
        Purchase purchase = new Purchase(basket);

        purchase.cancel();

        assertAll("Cancelled purchase keeps its basket and contents",
                () -> assertEquals(Purchase.Status.CANCELLED, purchase.getStatus()),
                () -> assertBasketPreserved(purchase, basket, milk)
        );
    }

    @Test
    @DisplayName("Ett slutfört köp kan inte avbrytas")
    void cancelRejectsCompletedPurchase() {
        Basket basket = new Basket();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        basket.addProduct(milk);
        Purchase purchase = new Purchase(basket);
        purchase.complete();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> purchase.cancel());

        assertAll("Rejected transition preserves the purchase",
                () -> assertEquals("Only active purchases can be cancelled", exception.getMessage()),
                () -> assertEquals(Purchase.Status.COMPLETED, purchase.getStatus()),
                () -> assertBasketPreserved(purchase, basket, milk)
        );
    }

    @Test
    @DisplayName("Ett avbrutet köp kan inte avbrytas igen")
    void cancelRejectsCancelledPurchase() {
        Basket basket = new Basket();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        basket.addProduct(milk);
        Purchase purchase = new Purchase(basket);
        purchase.cancel();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> purchase.cancel());

        assertAll("Rejected transition preserves the purchase",
                () -> assertEquals("Only active purchases can be cancelled", exception.getMessage()),
                () -> assertEquals(Purchase.Status.CANCELLED, purchase.getStatus()),
                () -> assertBasketPreserved(purchase, basket, milk)
        );
    }

    private void assertBasketPreserved(Purchase purchase, Basket expectedBasket, Product expectedProduct) {
        Basket actualBasket = purchase.getBasket();
        assertSame(expectedBasket, actualBasket, "Basket identity");
        assertEquals(1, actualBasket.getItems().size(), "Number of basket rows");

        assertAll("Basket contents are preserved",
                () -> assertSame(expectedProduct, actualBasket.getItems().get(0).getProduct(), "Product"),
                () -> assertEquals(1, actualBasket.getItems().get(0).getQuantity(), "Row quantity"),
                () -> assertEquals(1, actualBasket.getTotalQuantity(), "Total quantity")
        );
    }
}
