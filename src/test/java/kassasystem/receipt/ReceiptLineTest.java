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

    @ParameterizedTest(name = "{0} enheter")
    @ValueSource(ints = {1, 2, 3, 4})
    @DisplayName("Konstruktorn sparar antalet")
    void constructorStoresQuantity(int quantity) {
        Money unitPrice = new Money(Sek.SEK, 12.50);

        ReceiptLine line = new ReceiptLine("Milk", quantity, unitPrice);

        assertEquals(quantity, line.getQuantity());
    }

    @ParameterizedTest(name = "Styckpris {0}")
    @ValueSource(doubles = {0, 0.10, 0.125, 2.50, 12.50, 20})
    @DisplayName("Konstruktorn sparar styckeprisets belopp och valuta")
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
    @DisplayName("Konstruktorn avvisar ett saknat eller tomt varunamn")
    void constructorRejectsBlankProductName(String productName) {
        Money unitPrice = new Money(Sek.SEK, 12.50);

        assertThrows(IllegalArgumentException.class,
                () -> new ReceiptLine(productName, 1, unitPrice));
    }

    @ParameterizedTest(name = "{0} enheter")
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    @DisplayName("Konstruktorn avvisar antal under ett")
    void constructorRejectsInvalidQuantity(int quantity) {
        Money unitPrice = new Money(Sek.SEK, 12.50);

        assertThrows(IllegalArgumentException.class,
                () -> new ReceiptLine("Milk", quantity, unitPrice));
    }

    @Test
    @DisplayName("Konstruktorn avvisar ett saknat styckepris")
    void constructorRejectsNullUnitPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new ReceiptLine("Milk", 1, null));
    }

    @ParameterizedTest(name = "Varunamn: {0}")
    @ValueSource(strings = {"A", "Milk", "Mjölk – 3%", " Milk ",
            "Extra long product name that fits on the receipt"})
    @DisplayName("Giltiga varunamn sparas utan att ändras eller förkortas")
    void constructorPreservesProductName(String name) {
        Money unitPrice = new Money(Sek.SEK, 12.50);

        ReceiptLine line = new ReceiptLine(name, 1, unitPrice);

        assertEquals(name, line.getProductName());
    }

    @ParameterizedTest(name = "Styckpris {0}, antal {1}, total {2}")
    @CsvSource({"12.50, 1, 12.50", "12.50, 2, 25", "12.50, 3, 37.50",
            "0, 3, 0", "0.10, 3, 0.30", "0.125, 3, 0.375", "20, 1, 20", "2.50, 4, 10"})
    @DisplayName("Totalpriset är styckepriset gånger antalet utan förtida avrundning")
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
    @DisplayName("Största positiva heltalet accepteras som antal")
    void constructorAcceptsMaximumQuantity() {
        Money unitPrice = new Money(Sek.SEK, 1);

        ReceiptLine line = new ReceiptLine("Product", Integer.MAX_VALUE, unitPrice);

        assertEquals(Integer.MAX_VALUE, line.getQuantity());
    }

    @ParameterizedTest(name = "Styckpris {0}")
    @ValueSource(doubles = {Double.MIN_VALUE, Double.MAX_VALUE})
    @DisplayName("Minsta och största positiva ändliga styckpriset accepteras")
    void constructorAcceptsFinitePriceBoundaries(double amount) {
        Money unitPrice = new Money(Sek.SEK, amount);

        ReceiptLine line = new ReceiptLine("Product", 1, unitPrice);

        assertEquals(amount, line.getUnitPrice().getAmount());
    }

    @Test
    @DisplayName("Totalpriset beräknas för största tillåtna antal")
    void getTotalPriceHandlesMaximumQuantity() {
        Money unitPrice = new Money(Sek.SEK, 1);
        ReceiptLine line = new ReceiptLine("Product", Integer.MAX_VALUE, unitPrice);

        Money total = line.getTotalPrice();

        assertEquals((double) Integer.MAX_VALUE, total.getAmount());
    }

    @ParameterizedTest(name = "Styckpris {0}")
    @ValueSource(doubles = {Double.MIN_VALUE, Double.MAX_VALUE})
    @DisplayName("Totalpriset behåller ändliga prisgränser för en enhet")
    void getTotalPriceHandlesFinitePriceBoundaries(double amount) {
        Money unitPrice = new Money(Sek.SEK, amount);
        ReceiptLine line = new ReceiptLine("Product", 1, unitPrice);

        Money total = line.getTotalPrice();

        assertEquals(amount, total.getAmount());
    }

    @ParameterizedTest(name = "Styckpris {0}")
    @ValueSource(doubles = {-Double.MIN_VALUE, -0.01, -12.50, -Double.MAX_VALUE})
    @DisplayName("Negativa styckpriser avvisas")
    void constructorRejectsNegativeUnitPrice(double amount) {
        Money unitPrice = new Money(Sek.SEK, amount);

        assertThrows(IllegalArgumentException.class, () -> new ReceiptLine("Product", 1, unitPrice));
    }

    @ParameterizedTest(name = "Styckpris {0}")
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    @DisplayName("NaN och oändliga styckpriser avvisas")
    void constructorRejectsNonFiniteUnitPrice(double amount) {
        Money unitPrice = new Money(Sek.SEK, amount);

        assertThrows(IllegalArgumentException.class, () -> new ReceiptLine("Product", 1, unitPrice));
    }

    @Test
    @DisplayName("Ett styckpris utan valuta avvisas")
    void constructorRejectsMissingCurrency() {
        Money unitPrice = new Money(null, 12.50);

        assertThrows(IllegalArgumentException.class, () -> new ReceiptLine("Product", 1, unitPrice));
    }

    @Test
    @DisplayName("Ett styckpris i annan valuta än SEK avvisas")
    void constructorRejectsNonSekCurrency() {
        Money unitPrice = new Money(new Currency("EUR", "€"), 12.50);

        assertThrows(IllegalArgumentException.class, () -> new ReceiptLine("Product", 1, unitPrice));
    }

    @Test
    @DisplayName("Overflow i radpriset rapporteras utan att ändra radens värden")
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
