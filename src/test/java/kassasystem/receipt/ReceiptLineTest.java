package kassasystem.receipt;

import kassasystem.money.Currency;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReceiptLineTest {

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.06", "0.12", "0.25", "1"})
    @DisplayName("Constructor Stores The VAT Rate")
    void constructorStoresVatRate(BigDecimal vatRate) {
        Money unitPrice = money("125");

        ReceiptLine line = new ReceiptLine("Product", 1, unitPrice, vatRate);

        assertEquals(vatRate, line.getVatRate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "1.01"})
    @DisplayName("Constructor Rejects Invalid VAT Rates")
    void constructorRejectsInvalidVatRate(BigDecimal vatRate) {
        Money unitPrice = money("125");

        assertThrows(IllegalArgumentException.class,
                () -> new ReceiptLine("Product", 1, unitPrice, vatRate));
    }

    @Test
    @DisplayName("Constructor Rejects Null VAT Rate")
    void constructorRejectsNullVatRate() {
        assertThrows(IllegalArgumentException.class,
                () -> new ReceiptLine("Product", 1, money("125"), null));
    }

    @ParameterizedTest(name = "{0} units")
    @ValueSource(ints = {1, 2, 3, 4})
    @DisplayName("Constructor Stores Quantity")
    void constructorStoresQuantity(int quantity) {
        ReceiptLine line = new ReceiptLine("Milk", quantity, money("12.50"));

        assertEquals(quantity, line.getQuantity());
    }

    @ParameterizedTest(name = "Unit price {0}")
    @ValueSource(strings = {"0", "0.10", "0.125", "2.50", "12.50", "20"})
    @DisplayName("Constructor Stores Unit Price")
    void constructorStoresUnitPrice(BigDecimal amount) {
        ReceiptLine line = new ReceiptLine("Milk", 1, new Money(Sek.SEK, amount));

        assertEquals(0, amount.compareTo(line.getUnitPrice().getAmount()));
        assertSame(Sek.SEK, line.getUnitPrice().getCurrency());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n", " \t\n "})
    @DisplayName("Constructor Rejects Blank Product Name")
    void constructorRejectsBlankProductName(String productName) {
        assertThrows(IllegalArgumentException.class,
                () -> new ReceiptLine(productName, 1, money("12.50")));
    }

    @ParameterizedTest(name = "{0} units")
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    @DisplayName("Constructor Rejects Invalid Quantity")
    void constructorRejectsInvalidQuantity(int quantity) {
        assertThrows(IllegalArgumentException.class,
                () -> new ReceiptLine("Milk", quantity, money("12.50")));
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
        ReceiptLine line = new ReceiptLine(name, 1, money("12.50"));

        assertEquals(name, line.getProductName());
    }

    @ParameterizedTest(name = "Unit price {0}, quantity {1}, total {2}")
    @CsvSource({"12.50, 1, 12.50", "12.50, 2, 25.00", "12.50, 3, 37.50",
            "0, 3, 0", "0.10, 3, 0.30", "0.125, 3, 0.375", "20, 1, 20", "2.50, 4, 10.00"})
    @DisplayName("Get Total Price Multiplies Unit Price By Quantity")
    void getTotalPriceMultipliesUnitPriceByQuantity(
            BigDecimal amount, int quantity, BigDecimal expected) {
        ReceiptLine line = new ReceiptLine("Product", quantity, new Money(Sek.SEK, amount));

        Money total = line.getTotalPrice();

        assertAll("Total price has the expected amount and currency",
                () -> assertEquals(0, expected.compareTo(total.getAmount())),
                () -> assertSame(Sek.SEK, total.getCurrency())
        );
    }

    @Test
    @DisplayName("Constructor Accepts Maximum Quantity")
    void constructorAcceptsMaximumQuantity() {
        ReceiptLine line = new ReceiptLine("Product", Integer.MAX_VALUE, money("1"));

        assertEquals(Integer.MAX_VALUE, line.getQuantity());
    }

    @Test
    @DisplayName("Get Total Price Handles Maximum Quantity")
    void getTotalPriceHandlesMaximumQuantity() {
        ReceiptLine line = new ReceiptLine("Product", Integer.MAX_VALUE, money("1"));

        assertEquals(BigDecimal.valueOf(Integer.MAX_VALUE), line.getTotalPrice().getAmount());
    }

    @Test
    @DisplayName("Get Total Price Handles Amounts Beyond Double Range")
    void getTotalPriceHandlesLargeAmount() {
        BigDecimal amount = new BigDecimal("1E+400");
        ReceiptLine line = new ReceiptLine("Product", 2, new Money(Sek.SEK, amount));

        assertEquals(amount.multiply(BigDecimal.valueOf(2)), line.getTotalPrice().getAmount());
    }

    @ParameterizedTest(name = "Unit price {0}")
    @ValueSource(strings = {"-0.01", "-12.50", "-1E+400"})
    @DisplayName("Constructor Rejects Negative Unit Price")
    void constructorRejectsNegativeUnitPrice(BigDecimal amount) {
        Money unitPrice = new Money(Sek.SEK, amount);

        assertThrows(IllegalArgumentException.class,
                () -> new ReceiptLine("Product", 1, unitPrice));
    }

    @Test
    @DisplayName("Constructor Rejects Missing Currency")
    void constructorRejectsMissingCurrency() {
        Money unitPrice = new Money(null, new BigDecimal("12.50"));

        assertThrows(IllegalArgumentException.class,
                () -> new ReceiptLine("Product", 1, unitPrice));
    }

    @Test
    @DisplayName("Constructor Rejects Non-SEK Currency")
    void constructorRejectsNonSekCurrency() {
        Money unitPrice = new Money(new Currency("EUR", "€"), new BigDecimal("12.50"));

        assertThrows(IllegalArgumentException.class,
                () -> new ReceiptLine("Product", 1, unitPrice));
    }

    private Money money(String amount) {
        return new Money(Sek.SEK, new BigDecimal(amount));
    }
}
