import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BasketItemTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product("Milk", new Money(Sek.SEK, 12.50), null);
    }

    @Test
    @DisplayName("Konstruktorn sparar varan")
    void constructorSetsProduct() {
        BasketItem item = new BasketItem(product, 2);

        assertSame(product, item.getProduct());
    }

    @ParameterizedTest(name = "{0} stycken")
    @ValueSource(ints = {1, 2, 5})
    @DisplayName("Konstruktorn sparar antalet")
    void constructorSetsQuantity(int quantity) {
        BasketItem item = new BasketItem(product, quantity);

        assertEquals(quantity, item.getQuantity());
    }

    @Test
    @DisplayName("Konstruktorn avvisar en saknad vara")
    void constructorRejectsNullProduct() {
        assertThrows(IllegalArgumentException.class, () -> new BasketItem(null, 1));
    }

    @ParameterizedTest(name = "{0} stycken")
    @ValueSource(ints = {0, -1, -5})
    @DisplayName("Konstruktorn avvisar antal under ett")
    void constructorRejectsInvalidQuantity(int quantity) {
        assertThrows(IllegalArgumentException.class, () -> new BasketItem(product, quantity));
    }

    @Test
    @DisplayName("Antalet ökas med ett")
    void increaseQuantityAddsOne() {
        BasketItem item = new BasketItem(product, 2);

        item.increaseQuantity();

        assertEquals(3, item.getQuantity());
    }

    @ParameterizedTest(name = "Öka med {0}")
    @ValueSource(ints = {1, 2, 5})
    @DisplayName("Antalet ökas med det angivna värdet")
    void increaseQuantityAddsAmount(int amount) {
        BasketItem item = new BasketItem(product, 2);

        item.increaseQuantity(amount);

        assertEquals(2 + amount, item.getQuantity());
    }

    @ParameterizedTest(name = "Öka med {0}")
    @ValueSource(ints = {0, -1})
    @DisplayName("Ökning under ett avvisas utan att ändra antalet")
    void increaseQuantityRejectsInvalidAmount(int amount) {
        BasketItem item = new BasketItem(product, 2);

        assertThrows(IllegalArgumentException.class, () -> item.increaseQuantity(amount));
        assertEquals(2, item.getQuantity());
    }

    @Test
    @DisplayName("För stort antal avvisas utan att ändra antalet")
    void increaseQuantityRejectsOverflow() {
        BasketItem item = new BasketItem(product, Integer.MAX_VALUE);

        assertThrows(ArithmeticException.class, () -> item.increaseQuantity());
        assertEquals(Integer.MAX_VALUE, item.getQuantity());
    }

    @Test
    @DisplayName("Antalet minskas med ett")
    void decreaseQuantityRemovesOne() {
        BasketItem item = new BasketItem(product, 3);

        item.decreaseQuantity();

        assertEquals(2, item.getQuantity());
    }

    @Test
    @DisplayName("Antalet minskas inte under ett")
    void decreaseQuantityKeepsLastUnit() {
        BasketItem item = new BasketItem(product, 1);

        item.decreaseQuantity();

        assertEquals(1, item.getQuantity());
    }

    @Test
    @DisplayName("Radens pris är styckpriset gånger antalet med samma valuta")
    void getTotalPriceMultipliesPriceByQuantity() {
        BasketItem item = new BasketItem(product, 3);

        Money total = item.getTotalPrice();

        assertEquals(37.50, total.getAmount(), 0.001);
        assertSame(Sek.SEK, total.getCurrency());
    }
}
