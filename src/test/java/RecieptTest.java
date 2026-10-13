import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecieptTest {

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-01-15T10:30:00Z"),
                ZoneId.of("Europe/Stockholm"));
    }

    @Test
    @DisplayName("Totalpriset behåller köpets summa och valuta när korgen ändras")
    void getTotalPriceReturnsPurchaseTotal() {
        Basket basket = new Basket();
        Product milk = new Product("Milk", new Money(Sek.SEK, 2.50), null);
        Product bread = new Product("Bread", new Money(Sek.SEK, 3), null);
        basket.addItem(new BasketItem(milk, 2));
        basket.addProduct(bread);
        Receipt receipt = new Receipt(basket, fixedClock());

        basket.addProduct(milk);
        basket.removeProduct(bread);
        Money total = receipt.getTotalPrice();

        assertEquals(8, total.getAmount(), 0.001);
        assertSame(Sek.SEK, total.getCurrency());
    }

    @Test
    @DisplayName("En tom korg ger ett kvitto med noll i totalpris")
    void getTotalPriceReturnsZeroForEmptyBasket() {
        Receipt receipt = new Receipt(new Basket(), fixedClock());

        Money total = receipt.getTotalPrice();

        assertEquals(0, total.getAmount(), 0.001);
        assertSame(Sek.SEK, total.getCurrency());
    }

    @Test
    @DisplayName("Kvittot visar köptid, varor i kolumner och totalpris oberoende av datorns språk")
    void toTextFormatsPurchaseSnapshot() {
        Basket basket = new Basket();
        Product milk = new Product("Milk", new Money(Sek.SEK, 12.50), null);
        Product bread = new Product("Bread", new Money(Sek.SEK, 20), null);
        basket.addItem(new BasketItem(milk, 2));
        basket.addProduct(bread);
        Receipt receipt = new Receipt(basket, fixedClock());
        basket.addProduct(milk);
        basket.removeProduct(bread);
        String expected = "Kvitto\n"
                + "Datum: 2026-01-15 11:30\n"
                + "Vara" + " ".repeat(17) + "Antal  Styckpris      Summa\n"
                + "Milk" + " ".repeat(21) + "2" + " ".repeat(6) + "12.50"
                + " ".repeat(6) + "25.00\n"
                + "Bread" + " ".repeat(20) + "1" + " ".repeat(6) + "20.00"
                + " ".repeat(6) + "20.00\n"
                + "Totalt: 45.00 SEK\n";
        Locale originalLocale = Locale.getDefault();
        String text;

        try {
            Locale.setDefault(Locale.forLanguageTag("sv-SE"));
            text = receipt.toText();
        } finally {
            Locale.setDefault(originalLocale);
        }

        assertEquals(expected, text);
    }

    @Test
    @DisplayName("Ett tomt kvitto visar datum och nollsumma i SEK")
    void toTextFormatsEmptyReceipt() {
        Receipt receipt = new Receipt(new Basket(), fixedClock());
        String expected = "Kvitto\n"
                + "Datum: 2026-01-15 11:30\n"
                + "Vara" + " ".repeat(17) + "Antal  Styckpris      Summa\n"
                + "Totalt: 0.00 SEK\n";

        String text = receipt.toText();

        assertEquals(expected, text);
    }

    @Test
    @DisplayName("Kvittot avvisar en korg med annan valuta än SEK")
    void constructorRejectsNonSekBasket() {
        Basket basket = new Basket(new Currency("EUR", "€"));
        Clock clock = fixedClock();

        assertThrows(IllegalArgumentException.class, () -> new Receipt(basket, clock));
    }

    @Test
    @DisplayName("Långa varunamn behålls och beloppen ligger i samma kolumn")
    void toTextWidensNameColumnForLongProductNames() {
        String longName = "Extra long product name that fits on the receipt";
        Basket basket = new Basket();
        basket.addProduct(new Product(longName, new Money(Sek.SEK, 12.50), null));
        basket.addProduct(new Product("Milk", new Money(Sek.SEK, 12.50), null));
        Receipt receipt = new Receipt(basket, fixedClock());

        String[] lines = receipt.toText().split("\n");

        assertTrue(lines[3].startsWith(longName + " "));
        assertTrue(lines[4].startsWith("Milk "));
        assertEquals(lines[3].indexOf("12.50"), lines[4].indexOf("12.50"));
        assertEquals(lines[3].lastIndexOf("12.50"), lines[4].lastIndexOf("12.50"));
        assertEquals("Totalt: 25.00 SEK", lines[5]);
    }

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
