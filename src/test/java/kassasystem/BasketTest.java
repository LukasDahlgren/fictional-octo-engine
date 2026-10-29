package kassasystem;

import java.math.BigDecimal;

import kassasystem.money.Currency;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;
import kassasystem.scanning.EAN;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BasketTest {

    private final ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);

    private Basket basket;
    private Product milk;
    private Product bread;

    @BeforeEach
    void setUp() {
        basket = new Basket();
        milk = new Product("Milk", new Money(Sek.SEK, new BigDecimal("12.50")), food);
        bread = new Product("Bread", new Money(Sek.SEK, new BigDecimal("20")), food);
    }

    @Test
    @DisplayName("Constructor Creates Empty SEK Basket")
    void constructorCreatesEmptySekBasket() {
        assertEmptyBasket();
    }

    @Test
    @DisplayName("Constructor Uses Currency")
    void constructorUsesCurrency() {
        Currency euro = new Currency("EUR", "€");
        Basket euroBasket = new Basket(euro);

        assertSame(euro, euroBasket.getTotalPrice().getCurrency());
    }

    @Test
    @DisplayName("Constructor Rejects Null Currency")
    void constructorRejectsNullCurrency() {
        assertThrows(IllegalArgumentException.class, () -> new Basket(null));
    }

    @Test
    @DisplayName("Add Item Adds Row")
    void addItemAddsRow() {
        BasketItem item = new BasketItem(milk, 2);

        basket.addItem(item);

        assertEquals(List.of(item), basket.getItems());
    }

    @Test
    @DisplayName("Is Empty Returns False After Adding Row")
    void isEmptyReturnsFalseAfterAddingRow() {
        BasketItem item = new BasketItem(milk, 2);

        basket.addItem(item);

        assertFalse(basket.isEmpty());
    }

    @Test
    @DisplayName("Add Item Merges Products With Same EAN")
    void addItemMergesProductsWithSameEan() {
        Product otherMilk = new Product("Milk", new Money(Sek.SEK, new BigDecimal("12.50")), food,
                new EAN(milk.getEan().getCode()));
        basket.addItem(new BasketItem(milk, 2));

        basket.addItem(new BasketItem(otherMilk, 3));

        assertEquals(1, basket.getItems().size());
        assertEquals(5, basket.getItems().get(0).getQuantity());
        assertEquals(0, new BigDecimal("62.50").compareTo(basket.getTotalPrice().getAmount()));
    }

    @Test
    @DisplayName("Add Item Rejects Null")
    void addItemRejectsNull() {
        basket.addProduct(bread);

        assertThrows(IllegalArgumentException.class, () -> basket.addItem(null));
        assertAll("Basket remains unchanged after a missing row",
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertSame(bread, basket.getItems().get(0).getProduct()),
                () -> assertEquals(0, new BigDecimal("20").compareTo(basket.getTotalPrice().getAmount()))
        );
    }

    @Test
    @DisplayName("Add Item Rejects Different Currency")
    void addItemRejectsDifferentCurrency() {
        Currency euro = new Currency("EUR", "€");
        Product product = new Product("Milk", new Money(euro, new BigDecimal("2")), food);
        BasketItem item = new BasketItem(product, 1);
        basket.addProduct(bread);

        assertThrows(IllegalArgumentException.class, () -> basket.addItem(item));
        assertAll("Basket remains unchanged after a different currency",
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertSame(bread, basket.getItems().get(0).getProduct()),
                () -> assertEquals(0, new BigDecimal("20").compareTo(basket.getTotalPrice().getAmount()))
        );
    }

    @Test
    @DisplayName("Add Item Rejects Missing Currency")
    void addItemRejectsMissingCurrency() {
        Product product = new Product("Milk", new Money(null, new BigDecimal("2")), food);
        BasketItem item = new BasketItem(product, 1);
        basket.addProduct(bread);

        assertThrows(IllegalArgumentException.class, () -> basket.addItem(item));
        assertAll("Basket remains unchanged after a missing currency",
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertSame(bread, basket.getItems().get(0).getProduct()),
                () -> assertEquals(0, new BigDecimal("20").compareTo(basket.getTotalPrice().getAmount()))
        );
    }

    @Test
    @DisplayName("Add Product Adds One Unit At A Time")
    void addProductAddsOneUnitAtATime() {
        basket.addProduct(milk);

        basket.addProduct(milk);

        assertEquals(1, basket.getItems().size());
        assertAll("Product is stored with two units",
                () -> assertSame(milk, basket.getItems().get(0).getProduct()),
                () -> assertEquals(2, basket.getTotalQuantity())
        );
    }

    @Test
    @DisplayName("Add Product Rejects Null")
    void addProductRejectsNull() {
        basket.addProduct(bread);

        assertThrows(IllegalArgumentException.class, () -> basket.addProduct(null));
        assertAll("Basket remains unchanged after a missing product",
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertSame(bread, basket.getItems().get(0).getProduct()),
                () -> assertEquals(0, new BigDecimal("20").compareTo(basket.getTotalPrice().getAmount()))
        );
    }

    @Test
    @DisplayName("Get Items Rejects External Removal")
    void getItemsRejectsExternalRemoval() {
        BasketItem item = new BasketItem(milk, 1);
        basket.addItem(item);
        List<BasketItem> items = basket.getItems();

        assertThrows(UnsupportedOperationException.class, () -> items.remove(item));
        assertEquals(List.of(item), basket.getItems());
    }

    @Test
    @DisplayName("Get Items Rejects External Addition")
    void getItemsRejectsExternalAddition() {
        BasketItem item = new BasketItem(milk, 1);
        basket.addItem(item);
        List<BasketItem> items = basket.getItems();

        assertThrows(UnsupportedOperationException.class, () -> items.add(new BasketItem(bread, 1)));
        assertEquals(List.of(item), basket.getItems());
    }

    @Test
    @DisplayName("Get Items Rejects External Clear")
    void getItemsRejectsExternalClear() {
        BasketItem item = new BasketItem(milk, 1);
        basket.addItem(item);
        List<BasketItem> items = basket.getItems();

        assertThrows(UnsupportedOperationException.class, () -> items.clear());
        assertEquals(List.of(item), basket.getItems());
    }

    @Test
    @DisplayName("Get Total Price Sums Rows")
    void getTotalPriceSumsRows() {
        basket.addItem(new BasketItem(milk, 2));
        basket.addItem(new BasketItem(bread, 3));

        Money total = basket.getTotalPrice();

        assertAll("Total price sums rows and keeps the currency",
                () -> assertEquals(0, new BigDecimal("85").compareTo(total.getAmount())),
                () -> assertSame(Sek.SEK, total.getCurrency())
        );
    }

    @Test
    @DisplayName("Get Total Quantity Sums Units")
    void getTotalQuantitySumsUnits() {
        basket.addItem(new BasketItem(milk, 2));
        basket.addItem(new BasketItem(bread, 3));

        int quantity = basket.getTotalQuantity();

        assertEquals(5, quantity);
    }

    @Test
    @DisplayName("Remove Item Removes Whole Row")
    void removeItemRemovesWholeRow() {
        BasketItem item = new BasketItem(milk, 3);
        basket.addItem(item);
        basket.addProduct(bread);

        boolean removed = basket.removeItem(item);

        assertAll("Whole row is removed and the other product remains",
                () -> assertTrue(removed),
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertSame(bread, basket.getItems().get(0).getProduct())
        );
    }

    @Test
    @DisplayName("Remove Item Returns False For Missing Row")
    void removeItemReturnsFalseForMissingRow() {
        BasketItem item = new BasketItem(milk, 1);

        boolean removed = basket.removeItem(item);

        assertFalse(removed);
    }

    @Test
    @DisplayName("Remove Product With Same EAN Removes One Unit")
    void removeProductWithSameEanRemovesOneUnit() {
        Product otherMilk = new Product("Milk", new Money(Sek.SEK, new BigDecimal("12.50")), food,
                new EAN(milk.getEan().getCode()));
        basket.addItem(new BasketItem(milk, 3));

        boolean removed = basket.removeProduct(otherMilk);

        assertAll("One unit is removed",
                () -> assertTrue(removed),
                () -> assertEquals(2, basket.getItems().get(0).getQuantity())
        );
    }

    @Test
    @DisplayName("Remove Product With Same EAN Removes Last Unit")
    void removeProductWithSameEanRemovesLastUnit() {
        Product otherMilk = new Product("Milk", new Money(Sek.SEK, new BigDecimal("12.50")), food,
                new EAN(milk.getEan().getCode()));
        basket.addProduct(milk);

        boolean removed = basket.removeProduct(otherMilk);

        assertAll("Removing the last unit empties the basket",
                () -> assertTrue(removed),
                () -> assertTrue(basket.isEmpty())
        );
    }

    @Test
    @DisplayName("Remove Product Returns False For Missing Product")
    void removeProductReturnsFalseForMissingProduct() {
        basket.addProduct(bread);

        boolean removed = basket.removeProduct(milk);

        assertAll("Missing product leaves the basket unchanged",
                () -> assertFalse(removed),
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertSame(bread, basket.getItems().get(0).getProduct())
        );
    }

    @Test
    @DisplayName("Remove All Removes Selected Rows")
    void removeAllRemovesSelectedRows() {
        BasketItem milkItem = new BasketItem(milk, 2);
        BasketItem breadItem = new BasketItem(bread, 3);
        Product cheese = new Product("Cheese", new Money(Sek.SEK, new BigDecimal("30")), food);
        BasketItem cheeseItem = new BasketItem(cheese, 1);
        basket.addItem(milkItem);
        basket.addItem(breadItem);
        basket.addItem(cheeseItem);

        boolean removed = basket.removeAll(List.of(milkItem, breadItem));

        assertAll("Only selected rows are removed",
                () -> assertTrue(removed),
                () -> assertEquals(List.of(cheeseItem), basket.getItems())
        );
    }

    @Test
    @DisplayName("Remove All Returns False For Missing Rows")
    void removeAllReturnsFalseForMissingRows() {
        basket.addProduct(bread);
        BasketItem missingItem = new BasketItem(milk, 1);

        boolean removed = basket.removeAll(List.of(missingItem));

        assertAll("Missing rows leave the quantity unchanged",
                () -> assertFalse(removed),
                () -> assertEquals(1, basket.getTotalQuantity())
        );
    }

    @Test
    @DisplayName("Add Product Rejects Missing Currency")
    void addProductRejectsMissingCurrency() {
        Basket expectedBasket = new Basket();
        expectedBasket.addItem(new BasketItem(bread, 1));
        Product invalidProduct = new Product("Invalid", new Money(null, new BigDecimal("2")), food);
        basket.addProduct(bread);

        assertThrows(IllegalArgumentException.class, () -> basket.addProduct(invalidProduct));
        assertBasketEquals(expectedBasket, basket);
    }

    @Test
    @DisplayName("Add Product Rejects Different Currency")
    void addProductRejectsDifferentCurrency() {
        Basket expectedBasket = new Basket();
        expectedBasket.addItem(new BasketItem(bread, 1));
        Currency euro = new Currency("EUR", "€");
        Product invalidProduct = new Product("Invalid", new Money(euro, new BigDecimal("2")), food);
        basket.addProduct(bread);

        assertThrows(IllegalArgumentException.class, () -> basket.addProduct(invalidProduct));
        assertBasketEquals(expectedBasket, basket);
    }

    @Test
    @DisplayName("Add Item Merges Product In Later Row")
    void addItemMergesProductInLaterRow() {
        BasketItem breadItem = new BasketItem(bread, 1);
        BasketItem milkItem = new BasketItem(milk, 2);
        basket.addItem(breadItem);
        basket.addItem(milkItem);

        basket.addItem(new BasketItem(milk, 3));

        assertAll("Later row is merged and totals are updated",
                () -> assertEquals(List.of(breadItem, milkItem), basket.getItems()),
                () -> assertEquals(1, breadItem.getQuantity()),
                () -> assertEquals(5, milkItem.getQuantity()),
                () -> assertEquals(6, basket.getTotalQuantity()),
                () -> assertEquals(0, new BigDecimal("82.50").compareTo(basket.getTotalPrice().getAmount()))
        );
    }

    @Test
    @DisplayName("Add Item Keeps Distinct Product Objects Separate")
    void addItemKeepsDistinctProductObjectsSeparate() {
        Product otherMilk = new Product("Milk", new Money(Sek.SEK, new BigDecimal("12.50")), food);
        BasketItem first = new BasketItem(milk, 2);
        BasketItem second = new BasketItem(otherMilk, 3);
        basket.addItem(first);

        basket.addItem(second);

        assertAll("Distinct product objects remain on separate rows",
                () -> assertEquals(List.of(first, second), basket.getItems()),
                () -> assertEquals(2, first.getQuantity()),
                () -> assertEquals(3, second.getQuantity()),
                () -> assertEquals(5, basket.getTotalQuantity())
        );
    }

    @Test
    @DisplayName("Add Item Rejects Merged Quantity Overflow")
    void addItemRejectsMergedQuantityOverflow() {
        BasketItem item = new BasketItem(milk, Integer.MAX_VALUE);
        basket.addItem(item);

        assertThrows(ArithmeticException.class, () -> basket.addProduct(milk));
        assertAll("Overflow leaves the row unchanged",
                () -> assertEquals(List.of(item), basket.getItems()),
                () -> assertEquals(Integer.MAX_VALUE, item.getQuantity())
        );
    }

    @Test
    @DisplayName("Remove Item Does Not Remove Different Row With Same Product")
    void removeItemDoesNotRemoveDifferentRowWithSameProduct() {
        BasketItem storedItem = new BasketItem(milk, 2);
        BasketItem otherItem = new BasketItem(milk, 2);
        basket.addItem(storedItem);

        boolean removed = basket.removeItem(otherItem);

        assertAll("Different row leaves the stored row unchanged",
                () -> assertFalse(removed),
                () -> assertEquals(List.of(storedItem), basket.getItems()),
                () -> assertEquals(2, basket.getTotalQuantity()),
                () -> assertEquals(0, new BigDecimal("25").compareTo(basket.getTotalPrice().getAmount()))
        );
    }

    @Test
    @DisplayName("Remove Null Item Leaves Basket Unchanged")
    void removeNullItemLeavesBasketUnchanged() {
        BasketItem item = new BasketItem(milk, 2);
        basket.addItem(item);

        boolean removed = basket.removeItem(null);

        assertAll("Missing input leaves the basket unchanged",
                () -> assertFalse(removed),
                () -> assertEquals(List.of(item), basket.getItems()),
                () -> assertEquals(2, basket.getTotalQuantity()),
                () -> assertEquals(0, new BigDecimal("25").compareTo(basket.getTotalPrice().getAmount()))
        );
    }

    @Test
    @DisplayName("Remove Null Product Leaves Basket Unchanged")
    void removeNullProductLeavesBasketUnchanged() {
        BasketItem item = new BasketItem(milk, 2);
        basket.addItem(item);

        boolean removed = basket.removeProduct(null);

        assertAll("Missing input leaves the basket unchanged",
                () -> assertFalse(removed),
                () -> assertEquals(List.of(item), basket.getItems()),
                () -> assertEquals(2, basket.getTotalQuantity()),
                () -> assertEquals(0, new BigDecimal("25").compareTo(basket.getTotalPrice().getAmount()))
        );
    }

    @Test
    @DisplayName("Remove Product Decreases Quantity In Later Row")
    void removeProductDecreasesQuantityInLaterRow() {
        BasketItem breadItem = new BasketItem(bread, 1);
        BasketItem milkItem = new BasketItem(milk, 2);
        basket.addItem(breadItem);
        basket.addItem(milkItem);

        boolean removed = basket.removeProduct(milk);

        assertAll("One unit remains in the later row",
                () -> assertTrue(removed),
                () -> assertEquals(List.of(breadItem, milkItem), basket.getItems()),
                () -> assertEquals(1, milkItem.getQuantity()),
                () -> assertEquals(2, basket.getTotalQuantity()),
                () -> assertEquals(0, new BigDecimal("32.50").compareTo(basket.getTotalPrice().getAmount()))
        );
    }

    @Test
    @DisplayName("Remove Product Removes Last Unit In Later Row")
    void removeProductRemovesLastUnitInLaterRow() {
        BasketItem breadItem = new BasketItem(bread, 1);
        basket.addItem(breadItem);
        basket.addItem(new BasketItem(milk, 1));

        boolean removed = basket.removeProduct(milk);

        assertAll("Only bread remains",
                () -> assertTrue(removed),
                () -> assertEquals(List.of(breadItem), basket.getItems()),
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertEquals(0, new BigDecimal("20").compareTo(basket.getTotalPrice().getAmount()))
        );
    }

    @Test
    @DisplayName("Remove Product Does Not Match By Name")
    void removeProductDoesNotMatchByName() {
        basket.addProduct(milk);
        Product otherMilk = new Product("Milk", new Money(Sek.SEK, new BigDecimal("12.50")), food);

        boolean removed = basket.removeProduct(otherMilk);

        assertAll("Matching name leaves the original product unchanged",
                () -> assertFalse(removed),
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertSame(milk, basket.getItems().get(0).getProduct())
        );
    }

    @Test
    @DisplayName("Remove All Handles Existing And Missing Rows")
    void removeAllHandlesExistingAndMissingRows() {
        BasketItem milkItem = new BasketItem(milk, 2);
        BasketItem breadItem = new BasketItem(bread, 1);
        BasketItem missingItem = new BasketItem(milk, 3);
        basket.addItem(milkItem);
        basket.addItem(breadItem);

        boolean removed = basket.removeAll(List.of(milkItem, missingItem));

        assertAll("Only the existing selected row is removed",
                () -> assertTrue(removed),
                () -> assertEquals(List.of(breadItem), basket.getItems()),
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertEquals(0, new BigDecimal("20").compareTo(basket.getTotalPrice().getAmount()))
        );
    }

    @Test
    @DisplayName("Remove All Handles Duplicate Rows")
    void removeAllHandlesDuplicateRows() {
        BasketItem milkItem = new BasketItem(milk, 2);
        BasketItem breadItem = new BasketItem(bread, 1);
        basket.addItem(milkItem);
        basket.addItem(breadItem);

        boolean removed = basket.removeAll(List.of(milkItem, milkItem));

        assertAll("Duplicate selection removes only the selected row",
                () -> assertTrue(removed),
                () -> assertEquals(List.of(breadItem), basket.getItems()),
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertEquals(0, new BigDecimal("20").compareTo(basket.getTotalPrice().getAmount()))
        );
    }

    @Test
    @DisplayName("Remove All Handles Empty List")
    void removeAllHandlesEmptyList() {
        BasketItem item = new BasketItem(milk, 2);
        basket.addItem(item);

        boolean removed = basket.removeAll(List.of());

        assertAll("Empty selection leaves the basket unchanged",
                () -> assertFalse(removed),
                () -> assertEquals(List.of(item), basket.getItems()),
                () -> assertEquals(2, basket.getTotalQuantity())
        );
    }

    @Test
    @DisplayName("Remove All Empties Basket")
    void removeAllEmptiesBasket() {
        BasketItem milkItem = new BasketItem(milk, 2);
        BasketItem breadItem = new BasketItem(bread, 1);
        basket.addItem(milkItem);
        basket.addItem(breadItem);

        boolean removed = basket.removeAll(List.of(milkItem, breadItem));

        assertTrue(removed);
        assertEmptyBasket();
    }

    @Test
    @DisplayName("Remove Item Empties Basket")
    void removeItemEmptiesBasket() {
        BasketItem item = new BasketItem(milk, 2);
        basket.addItem(item);

        boolean removed = basket.removeItem(item);

        assertTrue(removed);
        assertEmptyBasket();
    }

    @Test
    @DisplayName("Remove Product From Empty Basket Returns False")
    void removeProductFromEmptyBasketReturnsFalse() {
        boolean removed = basket.removeProduct(milk);

        assertAll("Product removal leaves the basket empty",
                () -> assertFalse(removed),
                () -> assertTrue(basket.isEmpty()),
                () -> assertEquals(0, basket.getTotalQuantity())
        );
    }

    @Test
    @DisplayName("Remove All From Empty Basket Returns False")
    void removeAllFromEmptyBasketReturnsFalse() {
        BasketItem missingItem = new BasketItem(milk, 1);

        boolean removed = basket.removeAll(List.of(missingItem));

        assertAll("Row removal leaves the basket empty",
                () -> assertFalse(removed),
                () -> assertTrue(basket.isEmpty()),
                () -> assertEquals(0, basket.getTotalQuantity())
        );
    }

    @Test
    @DisplayName("Remove All With Empty List From Empty Basket Returns False")
    void removeAllWithEmptyListFromEmptyBasketReturnsFalse() {
        boolean removed = basket.removeAll(List.of());

        assertAll("Empty selection leaves the basket empty",
                () -> assertFalse(removed),
                () -> assertTrue(basket.isEmpty()),
                () -> assertEquals(0, basket.getTotalQuantity())
        );
    }

    @Test
    @DisplayName("Remove Product Returns False After Removing All Rows")
    void removeProductReturnsFalseAfterRemovingAllRows() {
        BasketItem milkItem = new BasketItem(milk, 2);
        BasketItem breadItem = new BasketItem(bread, 1);
        basket.addItem(milkItem);
        basket.addItem(breadItem);
        basket.removeAll(List.of(milkItem, breadItem));

        boolean removed = basket.removeProduct(milk);

        assertFalse(removed);
    }

    @Test
    @DisplayName("Remove Item Returns False For Previously Removed Row")
    void removeItemReturnsFalseForPreviouslyRemovedRow() {
        BasketItem item = new BasketItem(milk, 2);
        basket.addItem(item);
        basket.removeItem(item);

        boolean removed = basket.removeItem(item);

        assertFalse(removed);
    }

    @Test
    @DisplayName("Get Total Quantity Accepts Maximum Total")
    void getTotalQuantityAcceptsMaximumTotal() {
        basket.addItem(new BasketItem(milk, Integer.MAX_VALUE - 1));
        basket.addProduct(bread);

        int total = basket.getTotalQuantity();

        assertEquals(Integer.MAX_VALUE, total);
    }

    @Test
    @DisplayName("Get Total Quantity Rejects Overflow")
    void getTotalQuantityRejectsOverflow() {
        BasketItem milkItem = new BasketItem(milk, Integer.MAX_VALUE);
        BasketItem breadItem = new BasketItem(bread, 1);
        basket.addItem(milkItem);
        basket.addItem(breadItem);

        assertThrows(ArithmeticException.class, () -> basket.getTotalQuantity());
        assertAll("Quantity overflow leaves both rows unchanged",
                () -> assertEquals(List.of(milkItem, breadItem), basket.getItems()),
                () -> assertEquals(Integer.MAX_VALUE, milkItem.getQuantity()),
                () -> assertEquals(1, breadItem.getQuantity())
        );
    }

    @Test
    @DisplayName("Get Total Quantity Recovers After Removing Overflowing Row")
    void getTotalQuantityRecoversAfterRemovingOverflowingRow() {
        basket.addItem(new BasketItem(milk, Integer.MAX_VALUE));
        BasketItem breadItem = new BasketItem(bread, 1);
        basket.addItem(breadItem);
        assertThrows(ArithmeticException.class, () -> basket.getTotalQuantity());

        basket.removeItem(breadItem);

        assertEquals(Integer.MAX_VALUE, basket.getTotalQuantity());
    }

    @Test
    @DisplayName("Remove All Rejects Null List In Empty Basket")
    void removeAllRejectsNullListInEmptyBasket() {
        assertThrows(IllegalArgumentException.class, () -> basket.removeAll(null));

        assertEmptyBasket();
    }

    @Test
    @DisplayName("Remove All Rejects Null List In Populated Basket")
    void removeAllRejectsNullListInPopulatedBasket() {
        Basket expectedBasket = new Basket();
        expectedBasket.addItem(new BasketItem(milk, 2));
        basket.addItem(new BasketItem(milk, 2));

        assertThrows(IllegalArgumentException.class, () -> basket.removeAll(null));

        assertBasketEquals(expectedBasket, basket);
    }

    private void assertEmptyBasket() {
        assertAll("Empty basket",
                () -> assertTrue(basket.isEmpty()),
                () -> assertTrue(basket.getItems().isEmpty()),
                () -> assertEquals(0, basket.getTotalQuantity()),
                () -> assertEquals(0, new BigDecimal("0").compareTo(basket.getTotalPrice().getAmount())),
                () -> assertSame(Sek.SEK, basket.getTotalPrice().getCurrency())
        );
    }

    private void assertBasketEquals(Basket expected, Basket actual) {
        List<BasketItem> expectedItems = expected.getItems();
        List<BasketItem> actualItems = actual.getItems();
        assertEquals(expectedItems.size(), actualItems.size(), "Number of basket rows");

        assertAll("Basket totals",
                () -> assertEquals(expected.isEmpty(), actual.isEmpty(), "Empty basket"),
                () -> assertEquals(expected.getTotalQuantity(), actual.getTotalQuantity(), "Total quantity"),
                () -> assertEquals(0, expected.getTotalPrice().getAmount()
                        .compareTo(actual.getTotalPrice().getAmount()), "Total price"),
                () -> assertSame(expected.getTotalPrice().getCurrency(),
                        actual.getTotalPrice().getCurrency(), "Currency")
        );

        for (int i = 0; i < expectedItems.size(); i++) {
            BasketItem expectedItem = expectedItems.get(i);
            BasketItem actualItem = actualItems.get(i);

            assertAll("Basket row " + i,
                    () -> assertSame(expectedItem.getProduct(), actualItem.getProduct(), "Product"),
                    () -> assertEquals(expectedItem.getQuantity(), actualItem.getQuantity(), "Quantity")
            );
        }
    }
}
