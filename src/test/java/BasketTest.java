import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BasketTest {

    private Basket basket;
    private Product milk;
    private Product bread;

    @BeforeEach
    void setUp() {
        basket = new Basket();
        milk = new Product("Milk", new Money(Sek.SEK, 12.50), null);
        bread = new Product("Bread", new Money(Sek.SEK, 20), null);
    }

    @Test
    @DisplayName("En ny korg är tom och har noll i antal och pris i SEK")
    void constructorCreatesEmptySekBasket() {
        assertTrue(basket.isEmpty());
        assertTrue(basket.getItems().isEmpty());
        assertEquals(0, basket.getTotalQuantity());
        assertEquals(0, basket.getTotalPrice().getAmount(), 0.001);
        assertSame(Sek.SEK, basket.getTotalPrice().getCurrency());
    }

    @Test
    @DisplayName("Konstruktorn använder angiven valuta")
    void constructorUsesCurrency() {
        Currency euro = new Currency("EUR", "€");
        Basket euroBasket = new Basket(euro);
        Product product = new Product("Milk", new Money(euro, 2), null);

        euroBasket.addProduct(product);

        assertEquals(2, euroBasket.getTotalPrice().getAmount(), 0.001);
        assertSame(euro, euroBasket.getTotalPrice().getCurrency());
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

        assertEquals(List.of(item), basket.getItems());
        assertFalse(basket.isEmpty());
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
        assertThrows(IllegalArgumentException.class, () -> basket.addItem(null));
        assertTrue(basket.isEmpty());
    }

    @Test
    @DisplayName("En annan valuta avvisas")
    void addItemRejectsDifferentCurrency() {
        Currency euro = new Currency("EUR", "€");
        Product product = new Product("Milk", new Money(euro, 2), null);
        BasketItem item = new BasketItem(product, 1);

        assertThrows(IllegalArgumentException.class, () -> basket.addItem(item));
        assertTrue(basket.isEmpty());
    }

    @Test
    @DisplayName("En vara utan valuta avvisas")
    void addItemRejectsMissingCurrency() {
        Product product = new Product("Milk", new Money(null, 2), null);
        BasketItem item = new BasketItem(product, 1);

        assertThrows(IllegalArgumentException.class, () -> basket.addItem(item));
        assertTrue(basket.isEmpty());
    }

    @Test
    @DisplayName("Att lägga till en vara två gånger ger två enheter på en rad")
    void addProductAddsOneUnitAtATime() {
        basket.addProduct(milk);

        basket.addProduct(milk);

        assertEquals(1, basket.getItems().size());
        assertSame(milk, basket.getItems().get(0).getProduct());
        assertEquals(2, basket.getTotalQuantity());
    }

    @Test
    @DisplayName("Att lägga till en saknad vara avvisas")
    void addProductRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> basket.addProduct(null));
        assertTrue(basket.isEmpty());
    }

    @Test
    @DisplayName("Listan med rader kan inte ändras utifrån")
    void getItemsReturnsUnmodifiableList() {
        BasketItem item = new BasketItem(milk, 1);
        basket.addItem(item);
        List<BasketItem> items = basket.getItems();

        assertThrows(UnsupportedOperationException.class, () -> items.remove(item));
        assertEquals(1, basket.getItems().size());
    }

    @Test
    @DisplayName("Totalpriset summerar alla rader och behåller valutan")
    void getTotalPriceSumsRows() {
        basket.addItem(new BasketItem(milk, 2));
        basket.addItem(new BasketItem(bread, 3));

        Money total = basket.getTotalPrice();

        assertEquals(85, total.getAmount(), 0.001);
        assertSame(Sek.SEK, total.getCurrency());
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

        assertTrue(removed);
        assertEquals(1, basket.getTotalQuantity());
        assertSame(bread, basket.getItems().get(0).getProduct());
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

        assertTrue(removed);
        assertEquals(2, basket.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("Att ta bort sista enheten tar bort raden")
    void removeProductRemovesLastUnit() {
        basket.addProduct(milk);

        boolean removed = basket.removeProduct(milk);

        assertTrue(removed);
        assertTrue(basket.isEmpty());
    }

    @Test
    @DisplayName("Att ta bort en saknad vara lämnar korgen oförändrad")
    void removeProductReturnsFalseForMissingProduct() {
        basket.addProduct(bread);

        boolean removed = basket.removeProduct(milk);

        assertFalse(removed);
        assertEquals(1, basket.getTotalQuantity());
        assertSame(bread, basket.getItems().get(0).getProduct());
    }

    @Test
    @DisplayName("Flera angivna rader tas bort medan övriga finns kvar")
    void removeAllRemovesSelectedRows() {
        BasketItem milkItem = new BasketItem(milk, 2);
        BasketItem breadItem = new BasketItem(bread, 3);
        Product cheese = new Product("Cheese", new Money(Sek.SEK, 30), null);
        BasketItem cheeseItem = new BasketItem(cheese, 1);
        basket.addItem(milkItem);
        basket.addItem(breadItem);
        basket.addItem(cheeseItem);

        boolean removed = basket.removeAll(List.of(milkItem, breadItem));

        assertTrue(removed);
        assertEquals(List.of(cheeseItem), basket.getItems());
    }

    @Test
    @DisplayName("Att ta bort saknade rader returnerar false")
    void removeAllReturnsFalseForMissingRows() {
        basket.addProduct(bread);
        BasketItem missingItem = new BasketItem(milk, 1);

        boolean removed = basket.removeAll(List.of(missingItem));

        assertFalse(removed);
        assertEquals(1, basket.getTotalQuantity());
    }
}
