package kassasystem;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class BasketItemTest {

    private final ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product("Milk", new Money(Sek.SEK, 12.50), food);
    }

    @Test
    @DisplayName("Constructor Sets Product")
    void constructorSetsProduct() {
        BasketItem item = new BasketItem(product, 2);

        assertSame(product, item.getProduct());
    }

    @ParameterizedTest(name = "{0} items")
    @ValueSource(ints = {1, 2, 5, Integer.MAX_VALUE})
    @DisplayName("Constructor Sets Quantity")
    void constructorSetsQuantity(int quantity) {
        BasketItem item = new BasketItem(product, quantity);

        assertEquals(quantity, item.getQuantity());
    }

    @Test
    @DisplayName("Constructor Rejects Null Product")
    void constructorRejectsNullProduct() {
        assertThrows(IllegalArgumentException.class, () -> new BasketItem(null, 1));
    }

    @ParameterizedTest(name = "{0} items")
    @ValueSource(ints = {0, -1, -5, Integer.MIN_VALUE})
    @DisplayName("Constructor Rejects Invalid Quantity")
    void constructorRejectsInvalidQuantity(int quantity) {
        assertThrows(IllegalArgumentException.class, () -> new BasketItem(product, quantity));
    }

    @Test
    @DisplayName("Increase Quantity Adds One")
    void increaseQuantityAddsOne() {
        BasketItem item = new BasketItem(product, 2);

        item.increaseQuantity();

        assertEquals(3, item.getQuantity());
    }

    @ParameterizedTest(name = "Increase by {0}")
    @ValueSource(ints = {1, 2, 5})
    @DisplayName("Increase Quantity Adds Amount")
    void increaseQuantityAddsAmount(int amount) {
        BasketItem item = new BasketItem(product, 2);

        item.increaseQuantity(amount);

        assertEquals(2 + amount, item.getQuantity());
    }

    @ParameterizedTest(name = "Increase by {0}")
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    @DisplayName("Increase Quantity Rejects Invalid Amount")
    void increaseQuantityRejectsInvalidAmount(int amount) {
        BasketItem item = new BasketItem(product, 2);

        assertThrows(IllegalArgumentException.class, () -> item.increaseQuantity(amount));
        assertEquals(2, item.getQuantity());
    }

    @Test
    @DisplayName("Increase Quantity Rejects Overflow")
    void increaseQuantityRejectsOverflow() {
        BasketItem item = new BasketItem(product, Integer.MAX_VALUE);

        assertThrows(ArithmeticException.class, () -> item.increaseQuantity());
        assertEquals(Integer.MAX_VALUE, item.getQuantity());
    }

    @ParameterizedTest(name = "{0} items before decrease")
    @ValueSource(ints = {2, 3, Integer.MAX_VALUE})
    @DisplayName("Decrease Quantity Removes One")
    void decreaseQuantityRemovesOne(int quantity) {
        BasketItem item = new BasketItem(product, quantity);

        item.decreaseQuantity();

        assertEquals(quantity - 1, item.getQuantity());
    }

    @Test
    @DisplayName("Decrease Quantity Rejects Last Unit Without Changing Quantity")
    void decreaseQuantityRejectsLastUnit() {
        BasketItem item = new BasketItem(product, 1);

        assertThrows(IllegalStateException.class, () -> item.decreaseQuantity());

        assertEquals(1, item.getQuantity());
    }

    @Test
    @DisplayName("Get Total Price Multiplies Price By Quantity")
    void getTotalPriceMultipliesPriceByQuantity() {
        BasketItem item = new BasketItem(product, 3);

        Money total = item.getTotalPrice();

        assertAll("Row total multiplies unit price by quantity and keeps the currency",
                () -> assertEquals(37.50, total.getAmount(), 0.001),
                () -> assertSame(Sek.SEK, total.getCurrency())
        );
    }

    @Test
    @DisplayName("Increase Quantity Accepts Maximum Result")
    void increaseQuantityAcceptsMaximumResult() {
        BasketItem item = new BasketItem(product, Integer.MAX_VALUE - 2);

        item.increaseQuantity(2);

        assertEquals(Integer.MAX_VALUE, item.getQuantity());
    }

    @Test
    @DisplayName("Increase Quantity With Amount Rejects Overflow")
    void increaseQuantityWithAmountRejectsOverflow() {
        BasketItem item = new BasketItem(product, 2);

        assertThrows(ArithmeticException.class, () -> item.increaseQuantity(Integer.MAX_VALUE));
        assertAll("Overflow leaves the row unchanged",
                () -> assertEquals(2, item.getQuantity()),
                () -> assertSame(product, item.getProduct()),
                () -> assertEquals(25, item.getTotalPrice().getAmount(), 0.001)
        );
    }

    @ParameterizedTest(name = "Unit price {0}, quantity {1}, total {2}")
    @CsvSource({"0, 3, 0", "0.10, 3, 0.30", "12.50, 1, 12.50"})
    @DisplayName("Get Total Price Handles Price And Quantity Classes")
    void getTotalPriceHandlesPriceAndQuantityClasses(double price, int quantity, double expected) {
        Product pricedProduct = new Product("Product", new Money(Sek.SEK, price), food);
        BasketItem item = new BasketItem(pricedProduct, quantity);

        Money total = item.getTotalPrice();

        assertAll("Row total has the expected amount and currency",
                () -> assertEquals(expected, total.getAmount(), 0.000001),
                () -> assertSame(Sek.SEK, total.getCurrency())
        );
    }

    @Test
    @DisplayName("Get Total Price Updates After Quantity Increase")
    void getTotalPriceUpdatesAfterQuantityIncrease() {
        BasketItem item = new BasketItem(product, 1);

        item.increaseQuantity(2);

        Money total = item.getTotalPrice();
        assertEquals(37.50, total.getAmount(), 0.001);
    }

    @Test
    @DisplayName("Get Total Price Updates After Quantity Decrease")
    void getTotalPriceUpdatesAfterQuantityDecrease() {
        BasketItem item = new BasketItem(product, 3);

        item.decreaseQuantity();

        Money total = item.getTotalPrice();
        assertEquals(25, total.getAmount(), 0.001);
    }
}
