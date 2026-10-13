import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReceiptLineTest {

    @ParameterizedTest(name = "{0} enheter")
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("Radens totalpris är styckepriset gånger antalet")
    void getTotalPriceMultipliesUnitPriceByQuantity(int quantity) {
        Money unitPrice = new Money(Sek.SEK, 12.50);
        ReceiptLine line = new ReceiptLine("Milk", quantity, unitPrice);

        Money total = line.getTotalPrice();

        assertEquals("Milk", line.getProductName());
        assertEquals(quantity, line.getQuantity());
        assertEquals(12.50, line.getUnitPrice().getAmount(), 0.001);
        assertEquals(12.50 * quantity, total.getAmount(), 0.001);
        assertSame(Sek.SEK, total.getCurrency());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("Konstruktorn avvisar ett saknat eller tomt varunamn")
    void constructorRejectsBlankProductName(String productName) {
        Money unitPrice = new Money(Sek.SEK, 12.50);

        assertThrows(IllegalArgumentException.class,
                () -> new ReceiptLine(productName, 1, unitPrice));
    }

    @ParameterizedTest(name = "{0} enheter")
    @ValueSource(ints = {0, -1})
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
}
