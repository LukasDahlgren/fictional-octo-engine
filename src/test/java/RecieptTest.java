import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecieptTest {

    @Test
    @DisplayName("Kvittot returnerar varorna och antalen från köpet även om korgen ändras")
    void getItemsReturnsPurchaseSnapshot() {
        Basket basket = new Basket();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null);
        Product bread = new Product("Bread", new Money(Sek.SEK, 20), null);
        basket.addItem(new BasketItem(milk, 2));
        basket.addProduct(bread);
        Receipt receipt = new Receipt(basket, Clock.systemUTC());

        basket.addProduct(milk);
        basket.removeProduct(bread);

        List<ReceiptLine> items = receipt.getItems();

        assertEquals(2, items.size());
        assertEquals("Milk", items.get(0).getProductName());
        assertEquals(2, items.get(0).getQuantity());
        assertEquals(12.50, items.get(0).getUnitPrice().getAmount(), 0.001);
        assertEquals(25, items.get(0).getTotalPrice().getAmount(), 0.001);
        assertSame(Sek.SEK, items.get(0).getUnitPrice().getCurrency());
        assertEquals("Bread", items.get(1).getProductName());
        assertEquals(1, items.get(1).getQuantity());
        assertEquals(20, items.get(1).getUnitPrice().getAmount(), 0.001);
    }

    @Test
    @DisplayName("Kvittots lista med rader kan inte ändras utifrån")
    void getItemsReturnsUnmodifiableList() {
        Basket basket = new Basket();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null);
        basket.addItem(new BasketItem(milk, 2));
        Receipt receipt = new Receipt(basket, Clock.systemUTC());

        List<ReceiptLine> items = receipt.getItems();

        assertThrows(UnsupportedOperationException.class, () -> items.clear());
        assertEquals(1, receipt.getItems().size());
        assertEquals(2, receipt.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("Ett kvitto från en tom korg returnerar en tom lista")
    void getItemsReturnsEmptyListForEmptyBasket() {
        Basket basket = new Basket();
        Receipt receipt = new Receipt(basket, Clock.systemUTC());

        List<ReceiptLine> items = receipt.getItems();

        assertTrue(items.isEmpty());
    }

    @Test
    @DisplayName("Kvittot sparar köpets tid från den angivna klockan och tidszonen")
    void constructorCapturesTimestampInClockTimeZone() {
        Basket basket = new Basket();
        Clock clock = Clock.fixed(
                Instant.parse("2026-01-15T10:30:00Z"),
                ZoneId.of("Europe/Stockholm"));
        LocalDateTime expectedTimestamp = LocalDateTime.of(2026, 1, 15, 11, 30);

        Receipt receipt = new Receipt(basket, clock);

        assertEquals(expectedTimestamp, receipt.getTimestamp());
    }

    @Test
    @DisplayName("Konstruktorn avvisar en saknad korg")
    void constructorRejectsNullBasket() {
        Basket basket = null;
        Clock clock = Clock.systemUTC();

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, clock));
    }

    @Test
    @DisplayName("Konstruktorn avvisar en saknad klocka")
    void constructorRejectsNullClock() {
        Basket basket = new Basket();
        Clock clock = null;

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, clock));
    }
}
