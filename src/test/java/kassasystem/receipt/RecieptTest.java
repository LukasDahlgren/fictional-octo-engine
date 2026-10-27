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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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

    @Test
    @DisplayName("Kvittot behåller namn, pris, antal och text när produkt och korg ändras")
    void receiptCopiesValuesFromMutableProductAndBasket() {
        // Product currently has no setters, but subclasses can expose changing values.
        class MutableProduct extends Product {
            private String currentName = "Milk";
            private Money currentPrice = new Money(Sek.SEK, 12.50);

            MutableProduct() {
                super("Milk", new Money(Sek.SEK, 12.50), food);
            }

            @Override
            public String getName() {
                return currentName;
            }

            @Override
            public Money getPrice() {
                return currentPrice;
            }
        }
        MutableProduct product = new MutableProduct();
        BasketItem row = new BasketItem(product, 2);
        basket.addItem(row);
        Receipt receipt = new Receipt(basket, fixedClock(), new Money(Sek.SEK, 5), "Rabatt");
        String originalText = receipt.toText();

        product.currentName = "Changed name";
        product.currentPrice = new Money(Sek.SEK, 99);
        row.increaseQuantity();
        basket.removeItem(row);
        basket.addProduct(bread);

        assertEquals(1, receipt.getItems().size());
        assertReceiptLine(receipt.getItems().get(0), "Milk", 2, 12.50, 25);
        assertEquals(new Money(Sek.SEK, 25), receipt.getSubtotal());
        assertEquals(new Money(Sek.SEK, 5), receipt.getDiscountAmount());
        assertEquals(new Money(Sek.SEK, 20), receipt.getTotalPrice());
        assertEquals(originalText, receipt.toText());
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-01-15T10:30:00Z"),
                ZoneId.of("Europe/Stockholm"));
    }

    @Test
    @DisplayName("Kvittot bevarar exakt det slutbelopp som checkout skickar in")
    void constructorPreservesSuppliedTotal() {
        basket.addProduct(milk);
        Money total = new Money(Sek.SEK, 0.10);

        Receipt receipt = new Receipt(basket, total, fixedClock());

        assertEquals(total, receipt.getTotalPrice());
        assertEquals(new Money(Sek.SEK, 12.50), receipt.getSubtotal());
        assertEquals(12.40, receipt.getDiscountAmount().getAmount(), 0.000001);
        assertEquals("Rabatt", receipt.getDiscountLabel());
        assertTrue(receipt.toText().endsWith("Rabatt: -12.40 SEK\nTotalt: 0.10 SEK\n"));
    }

    @ParameterizedTest
    @ValueSource(doubles = { -1, 12.51, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY })
    @DisplayName("Kvittot avvisar ogiltiga slutbelopp från checkout")
    void constructorRejectsInvalidSuppliedTotal(double amount) {
        basket.addProduct(milk);
        Money total = new Money(Sek.SEK, amount);

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, total, fixedClock()));
    }

    @Test
    @DisplayName("Kvittot avvisar ett saknat slutbelopp från checkout")
    void constructorRejectsNullSuppliedTotal() {
        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, null, fixedClock()));
    }

    @Test
    @DisplayName("Kvittot avvisar ett slutbelopp i annan valuta än SEK")
    void constructorRejectsNonSekSuppliedTotal() {
        Money total = new Money(new Currency("EUR", "€"), 0);

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, total, fixedClock()));
    }

    @Test
    @DisplayName("Kvittot avvisar ett slutbelopp utan valuta")
    void constructorRejectsSuppliedTotalWithoutCurrency() {
        Money total = new Money(null, 0);

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, total, fixedClock()));
    }

    @Test
    @DisplayName("Kvittot visar delsumman, angiven rabatt och slutpriset med ursprungliga varupriser")
    void toTextShowsSuppliedDiscount() {
        basket.addItem(new BasketItem(milk, 2));
        basket.addProduct(bread);
        Receipt receipt = new Receipt(basket, fixedClock(),
                new Money(Sek.SEK, 4.50), "Medlemsrabatt (10%)");

        String expected = "Kvitto\n"
                + "Datum: 2026-01-15 11:30\n"
                + "Vara" + " ".repeat(17) + "Antal  Styckpris      Summa\n"
                + "Milk" + " ".repeat(21) + "2" + " ".repeat(6) + "12.50"
                + " ".repeat(6) + "25.00\n"
                + "Bread" + " ".repeat(20) + "1" + " ".repeat(6) + "20.00"
                + " ".repeat(6) + "20.00\n"
                + "Delsumma: 45.00 SEK\n"
                + "Medlemsrabatt (10%): -4.50 SEK\n"
                + "Totalt: 40.50 SEK\n";

        assertEquals(expected, receipt.toText());
        assertAll("Receipt stores the checkout amounts",
                () -> assertEquals(new Money(Sek.SEK, 45), receipt.getSubtotal()),
                () -> assertEquals(new Money(Sek.SEK, 4.50), receipt.getDiscountAmount()),
                () -> assertEquals("Medlemsrabatt (10%)", receipt.getDiscountLabel()),
                () -> assertEquals(new Money(Sek.SEK, 40.50), receipt.getTotalPrice())
        );
    }

    @Test
    @DisplayName("Nollrabatt behåller kvittots vanliga format")
    void zeroDiscountKeepsOriginalFormat() {
        basket.addProduct(milk);
        Receipt original = new Receipt(basket, fixedClock());
        Receipt withZeroDiscount = new Receipt(basket, fixedClock(),
                new Money(Sek.SEK, 0), "Kampanjrabatt");

        assertEquals(original.toText(), withZeroDiscount.toText());
        assertEquals(original.getSubtotal(), original.getTotalPrice());
        assertEquals(new Money(Sek.SEK, 0), original.getDiscountAmount());
    }

    @Test
    @DisplayName("Rabatt på hela delsumman ger noll i slutpris")
    void fullDiscountReturnsZeroTotal() {
        basket.addProduct(milk);
        Receipt receipt = new Receipt(basket, fixedClock(),
                new Money(Sek.SEK, 12.50), "Kampanjrabatt (100%)");

        assertEquals(new Money(Sek.SEK, 0), receipt.getTotalPrice());
        assertTrue(receipt.toText().endsWith(
                "Kampanjrabatt (100%): -12.50 SEK\nTotalt: 0.00 SEK\n"));
    }

    @Test
    @DisplayName("Rabatterat kvitto behåller belopp och text när korgen ändras")
    void discountedReceiptKeepsSnapshot() {
        basket.addProduct(milk);
        Receipt receipt = new Receipt(basket, fixedClock(),
                new Money(Sek.SEK, 2.50), "Kampanjrabatt");
        String originalText = receipt.toText();

        basket.removeProduct(milk);
        basket.addProduct(bread);

        assertEquals(originalText, receipt.toText());
        assertEquals(new Money(Sek.SEK, 12.50), receipt.getSubtotal());
        assertEquals(new Money(Sek.SEK, 10), receipt.getTotalPrice());
    }

    @ParameterizedTest
    @ValueSource(doubles = { -1, 12.51, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY })
    @DisplayName("Kvittot avvisar negativa, för stora och icke ändliga rabatter")
    void constructorRejectsInvalidDiscountAmount(double amount) {
        basket.addProduct(milk);
        Money discount = new Money(Sek.SEK, amount);

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, fixedClock(), discount, "Kampanjrabatt"));
    }

    @Test
    @DisplayName("Kvittot avvisar en saknad rabatt")
    void constructorRejectsNullDiscount() {
        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, fixedClock(), null, "Kampanjrabatt"));
    }

    @Test
    @DisplayName("Kvittot avvisar en rabatt i annan valuta än SEK")
    void constructorRejectsNonSekDiscount() {
        basket.addProduct(milk);
        Money discount = new Money(new Currency("EUR", "€"), 1);

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, fixedClock(), discount, "Kampanjrabatt"));
    }

    @Test
    @DisplayName("Kvittot avvisar en rabatt utan valuta")
    void constructorRejectsDiscountWithoutCurrency() {
        Money discount = new Money(null, 0);

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, fixedClock(), discount, "Kampanjrabatt"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "Rabatt\nTotalt", "Rabatt\rTotalt" })
    @DisplayName("En positiv rabatt kräver en beskrivning på en rad")
    void constructorRejectsInvalidDiscountLabel(String label) {
        basket.addProduct(milk);

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, fixedClock(), new Money(Sek.SEK, 1), label));
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
    @DisplayName("Kvittot visar delsumma, rabatt och totalpris")
    void toTextShowsAppliedDiscount() {
        Product product = new Product(
                "Testprodukt",
                new Money(Sek.SEK, 100.0),
                food
        );
        basket.addProduct(product);

        Money discountedTotal = new Money(Sek.SEK, 80.0);
        Receipt receipt = new Receipt(
                basket,
                discountedTotal,
                fixedClock()
        );

        String text = receipt.toText();

        assertAll(
                "Kvittot visar hur totalpriset har beräknats",
                () -> assertTrue(text.contains("Delsumma: 100.00 SEK\n")),
                () -> assertTrue(text.contains("Rabatt: -20.00 SEK\n")),
                () -> assertTrue(text.contains("Totalt: 80.00 SEK\n"))
        );
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
