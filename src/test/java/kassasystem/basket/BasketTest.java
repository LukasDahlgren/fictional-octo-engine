package kassasystem.basket;

import kassasystem.money.Currency;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BasketTest {

    private final ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);

    private Basket basket;
    private Product milk;
    private Product bread;

    @BeforeEach
    void setUp() {
        basket = new Basket();
        milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
        bread = new Product("Bread", new Money(Sek.SEK, 20), food);
    }

    @Test
    @DisplayName("En ny korg är tom och har noll i antal och pris i SEK")
    void constructorCreatesEmptySekBasket() {
        assertEmptyBasket();
    }

    @Test
    @DisplayName("Konstruktorn använder angiven valuta")
    void constructorUsesCurrency() {
        Currency euro = new Currency("EUR", "€");
        Basket euroBasket = new Basket(euro);
        Product product = new Product("Milk", new Money(euro, 2), food);

        euroBasket.addProduct(product);

        assertAll("Basket uses the supplied currency",
                () -> assertEquals(2, euroBasket.getTotalPrice().getAmount(), 0.001),
                () -> assertSame(euro, euroBasket.getTotalPrice().getCurrency())
        );
    }

    @Test
    @DisplayName("Konstruktorn avvisar en saknad valuta")
    void constructorRejectsNullCurrency() {
        assertThrows(IllegalArgumentException.class, () -> new Basket(null));
    }

    @Test
    @DisplayName("En rad läggs till i korgen")
    void addItemAddsRow() {
        BasketItem item = new BasketItem(milk, 2);

        basket.addItem(item);

        assertAll("Added row is stored in a non-empty basket",
                () -> assertEquals(List.of(item), basket.getItems()),
                () -> assertFalse(basket.isEmpty())
        );
    }

    @Test
    @DisplayName("Samma vara grupperas på en rad med sammanlagt antal")
    void addItemMergesSameProduct() {
        basket.addItem(new BasketItem(milk, 2));

        basket.addItem(new BasketItem(milk, 3));

        assertEquals(1, basket.getItems().size());
        assertEquals(5, basket.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("En saknad rad avvisas")
    void addItemRejectsNull() {
        basket.addProduct(bread);

        assertThrows(IllegalArgumentException.class, () -> basket.addItem(null));
        assertAll("Basket remains unchanged after a missing row",
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertSame(bread, basket.getItems().get(0).getProduct()),
                () -> assertEquals(20, basket.getTotalPrice().getAmount(), 0.001)
        );
    }

    @Test
    @DisplayName("En annan valuta avvisas")
    void addItemRejectsDifferentCurrency() {
        Currency euro = new Currency("EUR", "€");
        Product product = new Product("Milk", new Money(euro, 2), food);
        BasketItem item = new BasketItem(product, 1);
        basket.addProduct(bread);

        assertThrows(IllegalArgumentException.class, () -> basket.addItem(item));
        assertAll("Basket remains unchanged after a different currency",
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertSame(bread, basket.getItems().get(0).getProduct()),
                () -> assertEquals(20, basket.getTotalPrice().getAmount(), 0.001)
        );
    }

    @Test
    @DisplayName("En vara utan valuta avvisas")
    void addItemRejectsMissingCurrency() {
        Product product = new Product("Milk", new Money(null, 2), food);
        BasketItem item = new BasketItem(product, 1);
        basket.addProduct(bread);

        assertThrows(IllegalArgumentException.class, () -> basket.addItem(item));
        assertAll("Basket remains unchanged after a missing currency",
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertSame(bread, basket.getItems().get(0).getProduct()),
                () -> assertEquals(20, basket.getTotalPrice().getAmount(), 0.001)
        );
    }

    @Test
    @DisplayName("Att lägga till en vara två gånger ger två enheter på en rad")
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
    @DisplayName("Att lägga till en saknad vara avvisas")
    void addProductRejectsNull() {
        basket.addProduct(bread);

        assertThrows(IllegalArgumentException.class, () -> basket.addProduct(null));
        assertAll("Basket remains unchanged after a missing product",
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertSame(bread, basket.getItems().get(0).getProduct()),
                () -> assertEquals(20, basket.getTotalPrice().getAmount(), 0.001)
        );
    }

    @Test
    @DisplayName("Listan med rader kan inte ändras utifrån")
    void getItemsReturnsUnmodifiableList() {
        BasketItem item = new BasketItem(milk, 1);
        basket.addItem(item);
        List<BasketItem> items = basket.getItems();

        assertThrows(UnsupportedOperationException.class, () -> items.remove(item));
        assertThrows(UnsupportedOperationException.class, () -> items.add(new BasketItem(bread, 1)));
        assertThrows(UnsupportedOperationException.class, () -> items.clear());
        assertEquals(1, basket.getItems().size());
        assertSame(item, basket.getItems().get(0));
    }

    @Test
    @DisplayName("Totalpriset summerar alla rader och behåller valutan")
    void getTotalPriceSumsRows() {
        basket.addItem(new BasketItem(milk, 2));
        basket.addItem(new BasketItem(bread, 3));

        Money total = basket.getTotalPrice();

        assertAll("Total price sums rows and keeps the currency",
                () -> assertEquals(85, total.getAmount(), 0.001),
                () -> assertSame(Sek.SEK, total.getCurrency())
        );
    }

    @Test
    @DisplayName("Totala antalet summerar enheterna på alla rader")
    void getTotalQuantitySumsUnits() {
        basket.addItem(new BasketItem(milk, 2));
        basket.addItem(new BasketItem(bread, 3));

        int quantity = basket.getTotalQuantity();

        assertEquals(5, quantity);
    }

    @Test
    @DisplayName("Att ta bort en rad tar bort alla dess enheter")
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
    @DisplayName("Att ta bort en saknad rad returnerar false")
    void removeItemReturnsFalseForMissingRow() {
        BasketItem item = new BasketItem(milk, 1);

        boolean removed = basket.removeItem(item);

        assertFalse(removed);
    }

    @Test
    @DisplayName("Att ta bort en vara minskar antalet med ett")
    void removeProductRemovesOneUnit() {
        basket.addItem(new BasketItem(milk, 3));

        boolean removed = basket.removeProduct(milk);

        assertAll("One unit is removed",
                () -> assertTrue(removed),
                () -> assertEquals(2, basket.getItems().get(0).getQuantity())
        );
    }

    @Test
    @DisplayName("Att ta bort sista enheten tar bort raden")
    void removeProductRemovesLastUnit() {
        basket.addProduct(milk);

        boolean removed = basket.removeProduct(milk);

        assertAll("Removing the last unit empties the basket",
                () -> assertTrue(removed),
                () -> assertTrue(basket.isEmpty())
        );
    }

    @Test
    @DisplayName("Att ta bort en saknad vara lämnar korgen oförändrad")
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
    @DisplayName("Flera angivna rader tas bort medan övriga finns kvar")
    void removeAllRemovesSelectedRows() {
        BasketItem milkItem = new BasketItem(milk, 2);
        BasketItem breadItem = new BasketItem(bread, 3);
        Product cheese = new Product("Cheese", new Money(Sek.SEK, 30), food);
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
    @DisplayName("Att ta bort saknade rader returnerar false")
    void removeAllReturnsFalseForMissingRows() {
        basket.addProduct(bread);
        BasketItem missingItem = new BasketItem(milk, 1);

        boolean removed = basket.removeAll(List.of(missingItem));

        assertAll("Missing rows leave the quantity unchanged",
                () -> assertFalse(removed),
                () -> assertEquals(1, basket.getTotalQuantity())
        );
    }

    @ParameterizedTest(name = "Saknad valuta: {0}")
    @ValueSource(booleans = {true, false})
    @DisplayName("Att lägga till en vara med saknad eller fel valuta lämnar korgen oförändrad")
    void addProductRejectsInvalidCurrency(boolean missingCurrency) {
        Basket expectedBasket = new Basket();
        expectedBasket.addItem(new BasketItem(bread, 1));

        Currency currency;
        if (missingCurrency) {
            currency = null;
        } else {
            currency = new Currency("EUR", "€");
        }
        Product invalidProduct = new Product("Invalid", new Money(currency, 2), food);
        basket.addProduct(bread);

        assertThrows(IllegalArgumentException.class, () -> basket.addProduct(invalidProduct));
        assertBasketEquals(expectedBasket, basket);
    }

    @Test
    @DisplayName("En vara grupperas även när den ligger efter en annan rad")
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
                () -> assertEquals(82.50, basket.getTotalPrice().getAmount(), 0.001)
        );
    }

    @Test
    @DisplayName("Olika varuobjekt med samma namn och pris grupperas inte")
    void addItemKeepsDistinctProductObjectsSeparate() {
        Product otherMilk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
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
    @DisplayName("Overflow vid gruppering avvisas utan att ändra korgen")
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
    @DisplayName("En ny rad med samma vara är inte den befintliga raden vid borttagning")
    void removeItemDoesNotRemoveDifferentRowWithSameProduct() {
        BasketItem storedItem = new BasketItem(milk, 2);
        BasketItem otherItem = new BasketItem(milk, 2);
        basket.addItem(storedItem);

        boolean removed = basket.removeItem(otherItem);

        assertAll("Different row leaves the stored row unchanged",
                () -> assertFalse(removed),
                () -> assertEquals(List.of(storedItem), basket.getItems()),
                () -> assertEquals(2, basket.getTotalQuantity()),
                () -> assertEquals(25, basket.getTotalPrice().getAmount(), 0.001)
        );
    }

    @Test
    @DisplayName("Att ta bort en saknad rad eller vara från en fylld korg ändrar ingenting")
    void removeNullInputsLeavesBasketUnchanged() {
        BasketItem item = new BasketItem(milk, 2);
        basket.addItem(item);

        boolean removedItem = basket.removeItem(null);
        boolean removedProduct = basket.removeProduct(null);

        assertAll("Missing inputs leave the basket unchanged",
                () -> assertFalse(removedItem),
                () -> assertFalse(removedProduct),
                () -> assertEquals(List.of(item), basket.getItems()),
                () -> assertEquals(2, basket.getTotalQuantity()),
                () -> assertEquals(25, basket.getTotalPrice().getAmount(), 0.001)
        );
    }

    @Test
    @DisplayName("En vara kan tas bort efter en annan rad och totalsummorna uppdateras")
    void removeProductFindsLaterRow() {
        BasketItem breadItem = new BasketItem(bread, 1);
        basket.addItem(breadItem);
        basket.addItem(new BasketItem(milk, 2));

        boolean firstRemoval = basket.removeProduct(milk);
        int quantityAfterFirstRemoval = basket.getTotalQuantity();
        double priceAfterFirstRemoval = basket.getTotalPrice().getAmount();
        boolean lastRemoval = basket.removeProduct(milk);

        assertAll("First removal leaves one unit in the later row",
                () -> assertTrue(firstRemoval),
                () -> assertEquals(2, quantityAfterFirstRemoval),
                () -> assertEquals(32.50, priceAfterFirstRemoval, 0.001)
        );
        assertAll("Last removal leaves only bread",
                () -> assertTrue(lastRemoval),
                () -> assertEquals(List.of(breadItem), basket.getItems()),
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertEquals(20, basket.getTotalPrice().getAmount(), 0.001)
        );
    }

    @Test
    @DisplayName("En vara med samma namn men annan identitet tas inte bort")
    void removeProductDoesNotMatchByName() {
        basket.addProduct(milk);
        Product otherMilk = new Product("Milk", new Money(Sek.SEK, 12.50), food);

        boolean removed = basket.removeProduct(otherMilk);

        assertAll("Matching name leaves the original product unchanged",
                () -> assertFalse(removed),
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertSame(milk, basket.getItems().get(0).getProduct())
        );
    }

    @Test
    @DisplayName("En blandning av befintliga och saknade rader tar bara bort befintliga rader")
    void removeAllHandlesMixedAndDuplicateRows() {
        BasketItem milkItem = new BasketItem(milk, 2);
        BasketItem breadItem = new BasketItem(bread, 1);
        BasketItem missingItem = new BasketItem(milk, 3);
        basket.addItem(milkItem);
        basket.addItem(breadItem);

        boolean removed = basket.removeAll(List.of(milkItem, missingItem, milkItem));

        assertAll("Only the existing selected row is removed",
                () -> assertTrue(removed),
                () -> assertEquals(List.of(breadItem), basket.getItems()),
                () -> assertEquals(1, basket.getTotalQuantity()),
                () -> assertEquals(20, basket.getTotalPrice().getAmount(), 0.001)
        );
    }

    @Test
    @DisplayName("En tom lista med rader ändrar inte en fylld korg")
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
    @DisplayName("När alla rader tas bort blir korgen tom och totalsummorna noll")
    void removeAllEmptiesBasket() {
        BasketItem milkItem = new BasketItem(milk, 2);
        BasketItem breadItem = new BasketItem(bread, 1);
        basket.addItem(milkItem);
        basket.addItem(breadItem);

        boolean removed = basket.removeAll(List.of(milkItem, breadItem));

        assertTrue(removed);
        assertEmptyBasket();
        assertFalse(basket.removeProduct(milk));
    }

    @Test
    @DisplayName("Att ta bort sista raden återställer korgens totalsummor")
    void removeItemEmptiesBasket() {
        BasketItem item = new BasketItem(milk, 2);
        basket.addItem(item);

        boolean removed = basket.removeItem(item);

        assertTrue(removed);
        assertEmptyBasket();
        assertFalse(basket.removeItem(item));
    }

    @Test
    @DisplayName("Borttagning från en redan tom korg returnerar false")
    void removalsFromEmptyBasketReturnFalse() {
        BasketItem missingItem = new BasketItem(milk, 1);

        assertFalse(basket.removeProduct(milk));
        assertFalse(basket.removeAll(List.of(missingItem)));
        assertFalse(basket.removeAll(List.of()));
        assertAll("Basket remains empty",
                () -> assertTrue(basket.isEmpty()),
                () -> assertEquals(0, basket.getTotalQuantity())
        );
    }

    @Test
    @DisplayName("Summan av flera raders antal kan vara precis största heltalet")
    void getTotalQuantityAcceptsMaximumTotal() {
        basket.addItem(new BasketItem(milk, Integer.MAX_VALUE - 1));
        basket.addProduct(bread);

        int total = basket.getTotalQuantity();

        assertEquals(Integer.MAX_VALUE, total);
    }

    @Test
    @DisplayName("Overflow i korgens totalantal rapporteras utan att ändra raderna")
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

        basket.removeItem(breadItem);

        assertEquals(Integer.MAX_VALUE, basket.getTotalQuantity());
    }

    @ParameterizedTest(name = "Fylld korg: {0}")
    @ValueSource(booleans = {false, true})
    @DisplayName("En saknad lista med rader avvisas utan att ändra korgen")
    void removeAllRejectsNullList(boolean populated) {
        Basket expectedBasket = new Basket();
        if (populated) {
            expectedBasket.addItem(new BasketItem(milk, 2));
            basket.addItem(new BasketItem(milk, 2));
        }

        assertThrows(IllegalArgumentException.class, () -> basket.removeAll(null));

        assertBasketEquals(expectedBasket, basket);
    }

    private void assertEmptyBasket() {
        assertAll("Empty basket",
                () -> assertTrue(basket.isEmpty()),
                () -> assertTrue(basket.getItems().isEmpty()),
                () -> assertEquals(0, basket.getTotalQuantity()),
                () -> assertEquals(0, basket.getTotalPrice().getAmount(), 0.001),
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
                () -> assertEquals(expected.getTotalPrice().getAmount(),
                        actual.getTotalPrice().getAmount(), 0.001, "Total price"),
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
