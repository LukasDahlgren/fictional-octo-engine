package kassasystem.receipt;

import kassasystem.basket.Basket;
import kassasystem.basket.BasketItem;
import kassasystem.money.Currency;
import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecieptTest {

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

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-01-15T10:30:00Z"),
                ZoneId.of("Europe/Stockholm"));
    }

    @Test
    @DisplayName("Totalpriset behåller köpets summa när antalet i korgen ändras")
    void getTotalPriceKeepsSnapshotAfterQuantityChange() {
        Product cheapMilk = new Product("Milk", new Money(Sek.SEK, 2.50), food);
        Product cheapBread = new Product("Bread", new Money(Sek.SEK, 3), food);
        basket.addItem(new BasketItem(cheapMilk, 2));
        basket.addProduct(cheapBread);
        Receipt receipt = new Receipt(basket, fixedClock());

        basket.addProduct(cheapMilk);

        Money total = receipt.getTotalPrice();
        assertAll("Receipt keeps the original total",
                () -> assertEquals(8, total.getAmount(), 0.001),
                () -> assertSame(Sek.SEK, total.getCurrency())
        );
    }

    @Test
    @DisplayName("Totalpriset behåller köpets summa när en rad tas bort ur korgen")
    void getTotalPriceKeepsSnapshotAfterRowRemoval() {
        Product cheapMilk = new Product("Milk", new Money(Sek.SEK, 2.50), food);
        Product cheapBread = new Product("Bread", new Money(Sek.SEK, 3), food);
        basket.addItem(new BasketItem(cheapMilk, 2));
        basket.addProduct(cheapBread);
        Receipt receipt = new Receipt(basket, fixedClock());

        basket.removeProduct(cheapBread);

        Money total = receipt.getTotalPrice();
        assertAll("Receipt keeps the original total",
                () -> assertEquals(8, total.getAmount(), 0.001),
                () -> assertSame(Sek.SEK, total.getCurrency())
        );
    }

    @Test
    @DisplayName("En tom korg ger ett kvitto med noll i totalpris")
    void getTotalPriceReturnsZeroForEmptyBasket() {
        Receipt receipt = new Receipt(new Basket(), fixedClock());

        Money total = receipt.getTotalPrice();

        assertAll("Empty receipt total",
                () -> assertEquals(0, total.getAmount(), 0.001),
                () -> assertSame(Sek.SEK, total.getCurrency())
        );
    }

    @Test
    @DisplayName("Kvittot visar köptid, varor i kolumner och totalpris")
    void toTextFormatsPurchaseSnapshot() {
        basket.addItem(new BasketItem(milk, 2));
        basket.addProduct(bread);
        Receipt receipt = new Receipt(basket, fixedClock());
        String expected = "Kvitto\n"
                + "Datum: 2026-01-15 11:30\n"
                + "Vara" + " ".repeat(17) + "Antal  Styckpris      Summa\n"
                + "Milk" + " ".repeat(21) + "2" + " ".repeat(6) + "12.50"
                + " ".repeat(6) + "25.00\n"
                + "Bread" + " ".repeat(20) + "1" + " ".repeat(6) + "20.00"
                + " ".repeat(6) + "20.00\n"
                + "Totalt: 45.00 SEK\n";

        String text = receipt.toText();

        assertEquals(expected, text);
    }

    @Test
    @DisplayName("Kvittots belopp använder decimalpunkt även med svenskt språk")
    void toTextUsesDecimalPointRegardlessOfLocale() {
        basket.addItem(new BasketItem(milk, 2));
        Receipt receipt = new Receipt(basket, fixedClock());
        Locale originalLocale = Locale.getDefault();
        String text;

        try {
            Locale.setDefault(Locale.forLanguageTag("sv-SE"));
            text = receipt.toText();
        } finally {
            Locale.setDefault(originalLocale);
        }

        assertAll("Receipt formats amounts with decimal points",
                () -> assertTrue(text.contains("12.50")),
                () -> assertTrue(text.contains("25.00")),
                () -> assertTrue(text.contains("Totalt: 25.00 SEK\n"))
        );
    }

    @Test
    @DisplayName("Kvittotexten ändras inte när antalet i korgen ändras")
    void toTextKeepsSnapshotAfterQuantityChange() {
        basket.addProduct(milk);
        Receipt receipt = new Receipt(basket, fixedClock());
        String originalText = receipt.toText();

        basket.addProduct(milk);

        String text = receipt.toText();

        assertEquals(originalText, text);
    }

    @Test
    @DisplayName("Kvittotexten ändras inte när en rad tas bort ur korgen")
    void toTextKeepsSnapshotAfterRowRemoval() {
        basket.addProduct(milk);
        Receipt receipt = new Receipt(basket, fixedClock());
        String originalText = receipt.toText();

        basket.removeProduct(milk);

        String text = receipt.toText();

        assertEquals(originalText, text);
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
    @DisplayName("Långa varunamn skrivs ut utan att förkortas")
    void toTextPreservesLongProductNames() {
        String longName = "Extra long product name that fits on the receipt";
        basket.addProduct(new Product(longName, new Money(Sek.SEK, 12.50), food));
        Receipt receipt = new Receipt(basket, fixedClock());

        String[] lines = receipt.toText().split("\n");

        assertTrue(lines[3].startsWith(longName + " "));
    }

    @Test
    @DisplayName("Beloppen ligger i samma kolumner efter ett långt varunamn")
    void toTextAlignsPricesAfterLongProductName() {
        String longName = "Extra long product name that fits on the receipt";
        basket.addProduct(new Product(longName, new Money(Sek.SEK, 12.50), food));
        basket.addProduct(new Product("Milk", new Money(Sek.SEK, 12.50), food));
        Receipt receipt = new Receipt(basket, fixedClock());

        String[] lines = receipt.toText().split("\n");
        String longNameRow = lines[3];
        String milkRow = lines[4];
        int firstUnitPriceColumn = longNameRow.indexOf("12.50");
        int firstTotalColumn = longNameRow.lastIndexOf("12.50");
        int secondUnitPriceColumn = milkRow.indexOf("12.50");
        int secondTotalColumn = milkRow.lastIndexOf("12.50");

        assertAll("Both rows contain a unit price and a row total",
                () -> assertTrue(firstUnitPriceColumn >= 0),
                () -> assertTrue(firstTotalColumn > firstUnitPriceColumn),
                () -> assertTrue(secondUnitPriceColumn >= 0),
                () -> assertTrue(secondTotalColumn > secondUnitPriceColumn)
        );
        assertAll("Unit prices and row totals are aligned",
                () -> assertEquals(firstUnitPriceColumn, secondUnitPriceColumn),
                () -> assertEquals(firstTotalColumn, secondTotalColumn)
        );
    }

    @Test
    @DisplayName("Kvittoraderna behåller köpets antal när korgen ändras")
    void getItemsKeepsSnapshotAfterQuantityChange() {
        basket.addItem(new BasketItem(milk, 2));
        basket.addProduct(bread);
        Receipt receipt = new Receipt(basket, fixedClock());

        basket.addProduct(milk);

        List<ReceiptLine> items = receipt.getItems();

        assertEquals(2, items.size());
        assertAll("Receipt keeps the original rows",
                () -> assertReceiptLine(items.get(0), "Milk", 2, 12.50, 25),
                () -> assertReceiptLine(items.get(1), "Bread", 1, 20, 20)
        );
    }

    @Test
    @DisplayName("Kvittoraderna behåller en vara som tas bort ur korgen")
    void getItemsKeepsSnapshotAfterRowRemoval() {
        basket.addItem(new BasketItem(milk, 2));
        basket.addProduct(bread);
        Receipt receipt = new Receipt(basket, fixedClock());

        basket.removeProduct(bread);

        List<ReceiptLine> items = receipt.getItems();

        assertEquals(2, items.size());
        assertAll("Receipt keeps the original rows",
                () -> assertReceiptLine(items.get(0), "Milk", 2, 12.50, 25),
                () -> assertReceiptLine(items.get(1), "Bread", 1, 20, 20)
        );
    }

    @Test
    @DisplayName("Kvittots lista med rader kan inte ändras utifrån")
    void getItemsReturnsUnmodifiableList() {
        basket.addItem(new BasketItem(milk, 2));
        Receipt receipt = new Receipt(basket, fixedClock());

        List<ReceiptLine> items = receipt.getItems();

        assertThrows(UnsupportedOperationException.class, () -> items.clear());
        assertEquals(1, receipt.getItems().size());
        assertReceiptLine(receipt.getItems().get(0), "Milk", 2, 12.50, 25);
    }

    @Test
    @DisplayName("Ett kvitto från en tom korg returnerar en tom lista")
    void getItemsReturnsEmptyListForEmptyBasket() {
        Receipt receipt = new Receipt(basket, fixedClock());

        List<ReceiptLine> items = receipt.getItems();

        assertTrue(items.isEmpty());
    }

    @Test
    @DisplayName("Kvittot sparar köpets tid från den angivna klockan och tidszonen")
    void constructorCapturesTimestampInClockTimeZone() {
        Clock clock = fixedClock();
        LocalDateTime expectedTimestamp = LocalDateTime.of(2026, 1, 15, 11, 30);

        Receipt receipt = new Receipt(basket, clock);

        assertEquals(expectedTimestamp, receipt.getTimestamp());
    }

    @Test
    @DisplayName("Konstruktorn avvisar en saknad korg")
    void constructorRejectsNullBasket() {
        Basket basket = null;
        Clock clock = fixedClock();

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, clock));
    }

    @Test
    @DisplayName("Konstruktorn avvisar en saknad klocka")
    void constructorRejectsNullClock() {
        Clock clock = null;

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, clock));
    }

    private void assertReceiptLine(ReceiptLine line, String expectedName, int expectedQuantity,
            double expectedUnitPrice, double expectedTotal) {
        assertAll("Receipt line for " + expectedName,
                () -> assertEquals(expectedName, line.getProductName()),
                () -> assertEquals(expectedQuantity, line.getQuantity()),
                () -> assertEquals(expectedUnitPrice, line.getUnitPrice().getAmount(), 0.001),
                () -> assertSame(Sek.SEK, line.getUnitPrice().getCurrency()),
                () -> assertEquals(expectedTotal, line.getTotalPrice().getAmount(), 0.001),
                () -> assertSame(Sek.SEK, line.getTotalPrice().getCurrency())
        );
    }
}
