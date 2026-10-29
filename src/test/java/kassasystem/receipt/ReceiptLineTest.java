package kassasystem.receipt;

import kassasystem.money.Currency;
import kassasystem.money.Money;
import kassasystem.money.Sek;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReceiptLineTest {

    @ParameterizedTest
    @ValueSource(doubles = { 0, 0.06, 0.12, 0.25, 1 })
    @DisplayName("Constructor Stores The VAT Rate")
    void constructorStoresVatRate(double vatRate) {
        Money unitPrice = new Money(Sek.SEK, 125);

        ReceiptLine line = new ReceiptLine("Product", 1, unitPrice, vatRate);

        assertEquals(vatRate, line.getVatRate());
    }

    @ParameterizedTest
    @ValueSource(doubles = { -0.01, 1.01, Double.NaN, Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY })
    @DisplayName("Constructor Rejects Invalid VAT Rates")
    void constructorRejectsInvalidVatRate(double vatRate) {
        Money unitPrice = new Money(Sek.SEK, 125);

        assertThrows(IllegalArgumentException.class,
                () -> new ReceiptLine("Product", 1, unitPrice, vatRate));
    }

    @ParameterizedTest(name = "{0} units")
    @ValueSource(ints = {1, 2, 3, 4})
    @DisplayName("Constructor Stores Quantity")
    void constructorStoresQuantity(int quantity) {
        Money unitPrice = new Money(Sek.SEK, 12.50);

        ReceiptLine line = new ReceiptLine("Milk", quantity, unitPrice);

        assertEquals(quantity, line.getQuantity());
    }

    @ParameterizedTest(name = "Unit price {0}")
    @ValueSource(doubles = {0, 0.10, 0.125, 2.50, 12.50, 20})
    @DisplayName("Constructor Stores Unit Price")
    void constructorStoresUnitPrice(double amount) {
        Money unitPrice = new Money(Sek.SEK, amount);

        ReceiptLine line = new ReceiptLine("Milk", 1, unitPrice);

        assertAll("Stored unit price",
                () -> assertEquals(amount, line.getUnitPrice().getAmount()),
                () -> assertSame(Sek.SEK, line.getUnitPrice().getCurrency())
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n", " \t\n "})
    @DisplayName("Constructor Rejects Blank Product Name")
    void constructorRejectsBlankProductName(String productName) {
        Money unitPrice = new Money(Sek.SEK, 12.50);

        assertThrows(IllegalArgumentException.class,
                () -> new ReceiptLine(productName, 1, unitPrice));
    }

    @ParameterizedTest(name = "{0} units")
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    @DisplayName("Constructor Rejects Invalid Quantity")
    void constructorRejectsInvalidQuantity(int quantity) {
        Money unitPrice = new Money(Sek.SEK, 12.50);

        assertThrows(IllegalArgumentException.class,
                () -> new ReceiptLine("Milk", quantity, unitPrice));
    }

    @Test
    @DisplayName("Constructor Rejects Null Unit Price")
    void constructorRejectsNullUnitPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new ReceiptLine("Milk", 1, null));
    }

    @ParameterizedTest(name = "Product name: {0}")
    @ValueSource(strings = {"A", "Milk", "Milk – 3%", " Milk ",
            "Extra long product name that fits on the receipt"})
    @DisplayName("Constructor Preserves Product Name")
    void constructorPreservesProductName(String name) {
        Money unitPrice = new Money(Sek.SEK, 12.50);

        ReceiptLine line = new ReceiptLine(name, 1, unitPrice);

        assertEquals(name, line.getProductName());
    }

    @ParameterizedTest(name = "Unit price {0}, quantity {1}, total {2}")
    @CsvSource({"12.50, 1, 12.50", "12.50, 2, 25", "12.50, 3, 37.50",
            "0, 3, 0", "0.10, 3, 0.30", "0.125, 3, 0.375", "20, 1, 20", "2.50, 4, 10"})
    @DisplayName("Get Total Price Multiplies Unit Price By Quantity")
    void getTotalPriceMultipliesUnitPriceByQuantity(double amount, int quantity, double expected) {
        Money unitPrice = new Money(Sek.SEK, amount);
        ReceiptLine line = new ReceiptLine("Product", quantity, unitPrice);

        Money total = line.getTotalPrice();

        assertAll("Total price has the expected amount and currency",
                () -> assertEquals(expected, total.getAmount(), 0.000001),
                () -> assertSame(Sek.SEK, total.getCurrency())
        );
    }

    @Test
    @DisplayName("Constructor Accepts Maximum Quantity")
    void constructorAcceptsMaximumQuantity() {
        Money unitPrice = new Money(Sek.SEK, 1);

        ReceiptLine line = new ReceiptLine("Product", Integer.MAX_VALUE, unitPrice);

        assertEquals(Integer.MAX_VALUE, line.getQuantity());
    }

    @ParameterizedTest(name = "Unit price {0}")
    @ValueSource(doubles = {Double.MIN_VALUE, Double.MAX_VALUE})
    @DisplayName("Constructor Accepts Finite Price Boundaries")
    void constructorAcceptsFinitePriceBoundaries(double amount) {
        Money unitPrice = new Money(Sek.SEK, amount);

        ReceiptLine line = new ReceiptLine("Product", 1, unitPrice);

        assertEquals(amount, line.getUnitPrice().getAmount());
    }

    @Test
    @DisplayName("Get Total Price Handles Maximum Quantity")
    void getTotalPriceHandlesMaximumQuantity() {
        Money unitPrice = new Money(Sek.SEK, 1);
        ReceiptLine line = new ReceiptLine("Product", Integer.MAX_VALUE, unitPrice);

        Money total = line.getTotalPrice();

        assertEquals((double) Integer.MAX_VALUE, total.getAmount());
    }

    @ParameterizedTest(name = "Unit price {0}")
    @ValueSource(doubles = {Double.MIN_VALUE, Double.MAX_VALUE})
    @DisplayName("Get Total Price Handles Finite Price Boundaries")
    void getTotalPriceHandlesFinitePriceBoundaries(double amount) {
        Money unitPrice = new Money(Sek.SEK, amount);
        ReceiptLine line = new ReceiptLine("Product", 1, unitPrice);

        Money total = line.getTotalPrice();

        assertEquals(amount, total.getAmount());
    }

    @ParameterizedTest(name = "Unit price {0}")
    @ValueSource(doubles = {-Double.MIN_VALUE, -0.01, -12.50, -Double.MAX_VALUE})
    @DisplayName("Constructor Rejects Negative Unit Price")
    void constructorRejectsNegativeUnitPrice(double amount) {
        Money unitPrice = new Money(Sek.SEK, amount);

        assertThrows(IllegalArgumentException.class, () -> new ReceiptLine("Product", 1, unitPrice));
    }

    @ParameterizedTest(name = "Unit price {0}")
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    @DisplayName("Constructor Rejects Non Finite Unit Price")
    void constructorRejectsNonFiniteUnitPrice(double amount) {
        Money unitPrice = new Money(Sek.SEK, amount);

        assertThrows(IllegalArgumentException.class, () -> new ReceiptLine("Product", 1, unitPrice));
    }

    @Test
    @DisplayName("Constructor Rejects Missing Currency")
    void constructorRejectsMissingCurrency() {
        Money unitPrice = new Money(null, 12.50);

        assertThrows(IllegalArgumentException.class, () -> new ReceiptLine("Product", 1, unitPrice));
    }

    @Test
    @DisplayName("Constructor Rejects Non-SEK Currency")
    void constructorRejectsNonSekCurrency() {
        Money unitPrice = new Money(new Currency("EUR", "€"), 12.50);

        assertThrows(IllegalArgumentException.class, () -> new ReceiptLine("Product", 1, unitPrice));
    }

    @Test
    @DisplayName("Get Total Price Rejects Overflow")
    void getTotalPriceRejectsOverflow() {
        Money unitPrice = new Money(Sek.SEK, Double.MAX_VALUE);
        ReceiptLine line = new ReceiptLine("Product", 2, unitPrice);

        assertThrows(ArithmeticException.class, () -> line.getTotalPrice());
        assertAll("Overflow leaves the receipt line unchanged",
                () -> assertEquals("Product", line.getProductName()),
                () -> assertEquals(2, line.getQuantity()),
                () -> assertEquals(Double.MAX_VALUE, line.getUnitPrice().getAmount()),
                () -> assertSame(Sek.SEK, line.getUnitPrice().getCurrency())
        );
    }
}
