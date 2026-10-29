package kassasystem.receipt;

import java.math.BigDecimal;

import kassasystem.Basket;
import kassasystem.BasketItem;
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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceiptTest{

    private final ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);

    private Basket basket;
    private Product milk;
    private Product bread;

    @BeforeEach
    void setUp() {
        basket = new Basket();
        milk = new Product("Milk", new Money(Sek.SEK, new BigDecimal("12.50")), food);
        bread = new Product("Bread", new Money(Sek.SEK, new BigDecimal("20")), food);
    }

    @Test
    @DisplayName("Receipt Copies Values From Mutable Product And Basket")
    void receiptCopiesValuesFromMutableProductAndBasket() {
        // Product currently has no setters, but subclasses can expose changing values.
        class MutableProduct extends Product {
            private String currentName = "Milk";
            private Money currentPrice = new Money(Sek.SEK, new BigDecimal("12.50"));

            MutableProduct() {
                super("Milk", new Money(Sek.SEK, new BigDecimal("12.50")), food);
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
        Receipt receipt = new Receipt(basket, fixedClock(), new Money(Sek.SEK, new BigDecimal("5")), "Discount");
        String originalText = receipt.toText();

        product.currentName = "Changed name";
        product.currentPrice = new Money(Sek.SEK, new BigDecimal("99"));
        row.increaseQuantity();
        basket.removeItem(row);
        basket.addProduct(bread);

        assertEquals(1, receipt.getItems().size());
        assertReceiptLine(receipt.getItems().get(0), "Milk", 2,
                new BigDecimal("12.50"), new BigDecimal("25"));
        assertEquals(new Money(Sek.SEK, new BigDecimal("25")), receipt.getSubtotal());
        assertEquals(new Money(Sek.SEK, new BigDecimal("5")), receipt.getDiscountAmount());
        assertEquals(new Money(Sek.SEK, new BigDecimal("20")), receipt.getTotalPrice());
        assertEquals(originalText, receipt.toText());
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-01-15T10:30:00Z"),
                ZoneId.of("Europe/Stockholm"));
    }

    @Test
    @DisplayName("Constructor Preserves Supplied Total")
    void constructorPreservesSuppliedTotal() {
        basket.addProduct(milk);
        Money total = new Money(Sek.SEK, new BigDecimal("0.10"));

        Receipt receipt = new Receipt(basket, total, fixedClock());

        assertEquals(total, receipt.getTotalPrice());
        assertEquals(new Money(Sek.SEK, new BigDecimal("12.50")), receipt.getSubtotal());
        assertEquals(0, new BigDecimal("12.40").compareTo(receipt.getDiscountAmount().getAmount()));
        assertEquals("Discount", receipt.getDiscountLabel());
        assertTrue(receipt.toText().contains("Discount: -12.40 SEK\nTotal: 0.10 SEK\n"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "12.51"})
    @DisplayName("Constructor Rejects Invalid Supplied Total")
    void constructorRejectsInvalidSuppliedTotal(BigDecimal amount) {
        basket.addProduct(milk);
        Money total = new Money(Sek.SEK, amount);

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, total, fixedClock()));
    }

    @Test
    @DisplayName("Constructor Rejects Null Basket With Supplied Total")
    void constructorRejectsNullBasketWithSuppliedTotal() {
        Money total = new Money(Sek.SEK, new BigDecimal("0"));
        Clock clock = fixedClock();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Receipt(null, total, clock));

        assertEquals("Basket cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor Rejects Null Supplied Total")
    void constructorRejectsNullSuppliedTotal() {
        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, null, fixedClock()));
    }

    @Test
    @DisplayName("Constructor Rejects Non-SEK Supplied Total")
    void constructorRejectsNonSekSuppliedTotal() {
        Money total = new Money(new Currency("EUR", "€"), new BigDecimal("0"));

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, total, fixedClock()));
    }

    @Test
    @DisplayName("Constructor Rejects Supplied Total Without Currency")
    void constructorRejectsSuppliedTotalWithoutCurrency() {
        Money total = new Money(null, new BigDecimal("0"));

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, total, fixedClock()));
    }

    @Test
    @DisplayName("To Text Shows Supplied Discount")
    void toTextShowsSuppliedDiscount() {
        basket.addItem(new BasketItem(milk, 2));
        basket.addProduct(bread);
        Receipt receipt = new Receipt(basket, fixedClock(),
                new Money(Sek.SEK, new BigDecimal("4.50")), "Membership discount (10%)");

        String expected = "Receipt\n"
                + "Date: 2026-01-15 11:30\n"
                + String.format(Locale.ROOT, "%-20s %5s %10s %10s\n", "Product", "Quantity", "Unit price", "Line total")
                + "Milk" + " ".repeat(21) + "2" + " ".repeat(6) + "12.50"
                + " ".repeat(6) + "25.00\n"
                + "Bread" + " ".repeat(20) + "1" + " ".repeat(6) + "20.00"
                + " ".repeat(6) + "20.00\n"
                + "Subtotal: 45.00 SEK\n"
                + "Membership discount (10%): -4.50 SEK\n"
                + "Total: 40.50 SEK\n"
                + "VAT included:\n"
                + "6 %: 2.29 SEK\n";

        assertEquals(expected, receipt.toText());
        assertAll("Receipt stores the checkout amounts",
                () -> assertEquals(new Money(Sek.SEK, new BigDecimal("45")), receipt.getSubtotal()),
                () -> assertEquals(new Money(Sek.SEK, new BigDecimal("4.50")), receipt.getDiscountAmount()),
                () -> assertEquals("Membership discount (10%)", receipt.getDiscountLabel()),
                () -> assertEquals(new Money(Sek.SEK, new BigDecimal("40.50")), receipt.getTotalPrice())
        );
    }

    @Test
    @DisplayName("Zero Discount Keeps Original Format")
    void zeroDiscountKeepsOriginalFormat() {
        basket.addProduct(milk);
        Receipt original = new Receipt(basket, fixedClock());
        Receipt withZeroDiscount = new Receipt(basket, fixedClock(),
                new Money(Sek.SEK, new BigDecimal("0")), "Promotional discount");

        assertEquals(original.toText(), withZeroDiscount.toText());
        assertEquals(original.getSubtotal(), original.getTotalPrice());
        assertEquals(new Money(Sek.SEK, new BigDecimal("0")), original.getDiscountAmount());
    }

    @Test
    @DisplayName("Full Discount Returns Zero Total")
    void fullDiscountReturnsZeroTotal() {
        basket.addProduct(milk);
        Receipt receipt = new Receipt(basket, fixedClock(),
                new Money(Sek.SEK, new BigDecimal("12.50")), "Promotional discount (100%)");

        assertEquals(new Money(Sek.SEK, new BigDecimal("0")), receipt.getTotalPrice());
        assertTrue(receipt.toText().endsWith(
                "Promotional discount (100%): -12.50 SEK\nTotal: 0.00 SEK\n"
                + "VAT included:\n6 %: 0.00 SEK\n"));
    }

    @Test
    @DisplayName("Discounted Receipt Keeps Snapshot")
    void discountedReceiptKeepsSnapshot() {
        basket.addProduct(milk);
        Receipt receipt = new Receipt(basket, fixedClock(),
                new Money(Sek.SEK, new BigDecimal("2.50")), "Promotional discount");
        String originalText = receipt.toText();

        basket.removeProduct(milk);
        basket.addProduct(bread);

        assertEquals(originalText, receipt.toText());
        assertEquals(new Money(Sek.SEK, new BigDecimal("12.50")), receipt.getSubtotal());
        assertEquals(new Money(Sek.SEK, new BigDecimal("10")), receipt.getTotalPrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "12.51"})
    @DisplayName("Constructor Rejects Invalid Discount Amount")
    void constructorRejectsInvalidDiscountAmount(BigDecimal amount) {
        basket.addProduct(milk);
        Money discount = new Money(Sek.SEK, amount);

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, fixedClock(), discount, "Promotional discount"));
    }

    @Test
    @DisplayName("Constructor Rejects Null Discount")
    void constructorRejectsNullDiscount() {
        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, fixedClock(), null, "Promotional discount"));
    }

    @Test
    @DisplayName("Constructor Rejects Non-SEK Discount")
    void constructorRejectsNonSekDiscount() {
        basket.addProduct(milk);
        Money discount = new Money(new Currency("EUR", "€"), new BigDecimal("1"));

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, fixedClock(), discount, "Promotional discount"));
    }

    @Test
    @DisplayName("Constructor Rejects Discount Without Currency")
    void constructorRejectsDiscountWithoutCurrency() {
        Money discount = new Money(null, new BigDecimal("0"));

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, fixedClock(), discount, "Promotional discount"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "Discount\nTotal", "Discount\rTotal" })
    @DisplayName("Constructor Rejects Invalid Discount Label")
    void constructorRejectsInvalidDiscountLabel(String label) {
        basket.addProduct(milk);

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, fixedClock(), new Money(Sek.SEK, new BigDecimal("1")), label));
    }

    @Test
    @DisplayName("Get Total Price Keeps Snapshot After Quantity Change")
    void getTotalPriceKeepsSnapshotAfterQuantityChange() {
        Product cheapMilk = new Product("Milk", new Money(Sek.SEK, new BigDecimal("2.50")), food);
        Product cheapBread = new Product("Bread", new Money(Sek.SEK, new BigDecimal("3")), food);
        basket.addItem(new BasketItem(cheapMilk, 2));
        basket.addProduct(cheapBread);
        Receipt receipt = new Receipt(basket, fixedClock());

        basket.addProduct(cheapMilk);

        Money total = receipt.getTotalPrice();
        assertAll("Receipt keeps the original total",
                () -> assertEquals(0, new BigDecimal("8").compareTo(total.getAmount())),
                () -> assertSame(Sek.SEK, total.getCurrency())
        );
    }

    @Test
    @DisplayName("Get Total Price Keeps Snapshot After Row Removal")
    void getTotalPriceKeepsSnapshotAfterRowRemoval() {
        Product cheapMilk = new Product("Milk", new Money(Sek.SEK, new BigDecimal("2.50")), food);
        Product cheapBread = new Product("Bread", new Money(Sek.SEK, new BigDecimal("3")), food);
        basket.addItem(new BasketItem(cheapMilk, 2));
        basket.addProduct(cheapBread);
        Receipt receipt = new Receipt(basket, fixedClock());

        basket.removeProduct(cheapBread);

        Money total = receipt.getTotalPrice();
        assertAll("Receipt keeps the original total",
                () -> assertEquals(0, new BigDecimal("8").compareTo(total.getAmount())),
                () -> assertSame(Sek.SEK, total.getCurrency())
        );
    }

    @Test
    @DisplayName("Get Total Price Returns Zero For Empty Basket")
    void getTotalPriceReturnsZeroForEmptyBasket() {
        Receipt receipt = new Receipt(new Basket(), fixedClock());

        Money total = receipt.getTotalPrice();

        assertAll("Empty receipt total",
                () -> assertEquals(0, new BigDecimal("0").compareTo(total.getAmount())),
                () -> assertSame(Sek.SEK, total.getCurrency())
        );
    }

    @Test
    @DisplayName("To Text Formats Purchase Snapshot")
    void toTextFormatsPurchaseSnapshot() {
        basket.addItem(new BasketItem(milk, 2));
        basket.addProduct(bread);
        Receipt receipt = new Receipt(basket, fixedClock());
        String expected = "Receipt\n"
                + "Date: 2026-01-15 11:30\n"
                + String.format(Locale.ROOT, "%-20s %5s %10s %10s\n", "Product", "Quantity", "Unit price", "Line total")
                + "Milk" + " ".repeat(21) + "2" + " ".repeat(6) + "12.50"
                + " ".repeat(6) + "25.00\n"
                + "Bread" + " ".repeat(20) + "1" + " ".repeat(6) + "20.00"
                + " ".repeat(6) + "20.00\n"
                + "Total: 45.00 SEK\n"
                + "VAT included:\n"
                + "6 %: 2.55 SEK\n";

        String text = receipt.toText();

        assertEquals(expected, text);
    }

    @ParameterizedTest
    @CsvSource({
            "SIX_PERCENT, 106, '6 %: 6.00 SEK'",
            "TWELVE_PERCENT, 112, '12 %: 12.00 SEK'",
            "TWENTY_FIVE_PERCENT, 125, '25 %: 25.00 SEK'"
    })
    @DisplayName("Receipt Shows VAT Included In Price For Each Rate")
    void toTextShowsVatIncludedInPrice(VATCategory vatCategory, BigDecimal price, String expectedVat) {
        ProductCategory category = new ProductCategory("Test category", vatCategory);
        Product product = new Product("Test product", new Money(Sek.SEK, price), category);
        basket.addProduct(product);
        Receipt receipt = new Receipt(basket, fixedClock());

        String text = receipt.toText();

        assertTrue(text.endsWith("VAT included:\n" + expectedVat + "\n"));
        assertEquals(0, price.compareTo(receipt.getTotalPrice().getAmount()));
    }

    @Test
    @DisplayName("Receipt Groups VAT By Rate In Ascending Order")
    void toTextGroupsVatByRate() {
        ProductCategory other = new ProductCategory("Other", VATCategory.TWENTY_FIVE_PERCENT);
        basket.addProduct(new Product("Other product", new Money(Sek.SEK, new BigDecimal("125")), other));
        basket.addItem(new BasketItem(new Product("Food", new Money(Sek.SEK, new BigDecimal("53")), food), 2));
        basket.addProduct(new Product("Other food", new Money(Sek.SEK, new BigDecimal("106")), food));
        Receipt receipt = new Receipt(basket, fixedClock());

        String text = receipt.toText();

        assertTrue(text.endsWith("VAT included:\n6 %: 12.00 SEK\n25 %: 25.00 SEK\n"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    @DisplayName("Receipt Applies Discounts Across VAT Rates")
    void toTextCalculatesVatAfterDiscount(boolean supplyFinalTotal) {
        ProductCategory other = new ProductCategory("Other", VATCategory.TWENTY_FIVE_PERCENT);
        basket.addProduct(new Product("Food", new Money(Sek.SEK, new BigDecimal("106")), food));
        basket.addProduct(new Product("Other product", new Money(Sek.SEK, new BigDecimal("125")), other));
        Receipt receipt;
        if (supplyFinalTotal) {
            receipt = new Receipt(basket, new Money(Sek.SEK, new BigDecimal("184.80")), fixedClock());
        } else {
            receipt = new Receipt(basket, fixedClock(), new Money(Sek.SEK, new BigDecimal("46.20")), "Discount");
        }

        String text = receipt.toText();

        assertTrue(text.contains("Total: 184.80 SEK\n"));
        assertTrue(text.endsWith("VAT included:\n6 %: 4.80 SEK\n25 %: 20.00 SEK\n"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "106"})
    @DisplayName("Zero Total Shows Zero VAT")
    void toTextShowsZeroVatWhenTotalIsZero(BigDecimal price) {
        basket.addProduct(new Product("Test product", new Money(Sek.SEK, price), food));
        Receipt receipt = new Receipt(basket, new Money(Sek.SEK, new BigDecimal("0")), fixedClock());

        String text = receipt.toText();

        assertTrue(text.endsWith("VAT included:\n6 %: 0.00 SEK\n"));
    }

    @Test
    @DisplayName("Receipt Keeps VAT Rate After Product Category Changes")
    void toTextKeepsVatSnapshotAfterCategoryChange() {
        ProductCategory other = new ProductCategory("Other", VATCategory.TWENTY_FIVE_PERCENT);
        class MutableProduct extends Product {
            private ProductCategory currentCategory = food;

            MutableProduct() {
                super("Test product", new Money(Sek.SEK, new BigDecimal("106")), food);
            }

            @Override
            public ProductCategory getCategory() {
                return currentCategory;
            }
        }
        MutableProduct product = new MutableProduct();
        basket.addProduct(product);
        Receipt receipt = new Receipt(basket, fixedClock());

        product.currentCategory = other;

        assertTrue(receipt.toText().endsWith("VAT included:\n6 %: 6.00 SEK\n"));
    }

    @Test
    @DisplayName("To Text Shows Applied Discount")
    void toTextShowsAppliedDiscount() {
        Product product = new Product(
                "Test product",
                new Money(Sek.SEK, new BigDecimal("100.0")),
                food
        );
        basket.addProduct(product);

        Money discountedTotal = new Money(Sek.SEK, new BigDecimal("80.0"));
        Receipt receipt = new Receipt(
                basket,
                discountedTotal,
                fixedClock()
        );

        String text = receipt.toText();

        assertAll(
                "Receipt output shows how the total price was calculated",
                () -> assertTrue(text.contains("Subtotal: 100.00 SEK\n")),
                () -> assertTrue(text.contains("Discount: -20.00 SEK\n")),
                () -> assertTrue(text.contains("Total: 80.00 SEK\n"))
        );
    }

    @Test
    @DisplayName("To Text Uses Decimal Point Regardless Of Locale")
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
                () -> assertTrue(text.contains("Total: 25.00 SEK\n"))
        );
    }

    @Test
    @DisplayName("To Text Keeps Snapshot After Quantity Change")
    void toTextKeepsSnapshotAfterQuantityChange() {
        basket.addProduct(milk);
        Receipt receipt = new Receipt(basket, fixedClock());
        String originalText = receipt.toText();

        basket.addProduct(milk);

        String text = receipt.toText();

        assertEquals(originalText, text);
    }

    @Test
    @DisplayName("To Text Keeps Snapshot After Row Removal")
    void toTextKeepsSnapshotAfterRowRemoval() {
        basket.addProduct(milk);
        Receipt receipt = new Receipt(basket, fixedClock());
        String originalText = receipt.toText();

        basket.removeProduct(milk);

        String text = receipt.toText();

        assertEquals(originalText, text);
    }

    @Test
    @DisplayName("To Text Formats Empty Receipt")
    void toTextFormatsEmptyReceipt() {
        Receipt receipt = new Receipt(new Basket(), fixedClock());
        String expected = "Receipt\n"
                + "Date: 2026-01-15 11:30\n"
                + String.format(Locale.ROOT, "%-20s %5s %10s %10s\n", "Product", "Quantity", "Unit price", "Line total")
                + "Total: 0.00 SEK\n";

        String text = receipt.toText();

        assertEquals(expected, text);
    }

    @Test
    @DisplayName("Constructor Rejects Non-SEK Basket")
    void constructorRejectsNonSekBasket() {
        Basket basket = new Basket(new Currency("EUR", "€"));
        Clock clock = fixedClock();

        assertThrows(IllegalArgumentException.class, () -> new Receipt(basket, clock));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1"})
    @DisplayName("Constructor Rejects Invalid Subtotal")
    void constructorRejectsInvalidSubtotal(BigDecimal amount) {
        Basket invalidBasket = new Basket() {
            @Override
            public Money getTotalPrice() {
                return new Money(Sek.SEK, amount);
            }
        };
        Clock clock = fixedClock();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Receipt(invalidBasket, clock));

        assertEquals("Subtotal must be non-negative", exception.getMessage());
    }

    @Test
    @DisplayName("To Text Preserves Long Product Names")
    void toTextPreservesLongProductNames() {
        String longName = "Extra long product name that fits on the receipt";
        basket.addProduct(new Product(longName, new Money(Sek.SEK, new BigDecimal("12.50")), food));
        Receipt receipt = new Receipt(basket, fixedClock());

        String[] lines = receipt.toText().split("\n");

        assertTrue(lines[3].startsWith(longName + " "));
    }

    @Test
    @DisplayName("To Text Aligns Prices After Long Product Name")
    void toTextAlignsPricesAfterLongProductName() {
        String longName = "Extra long product name that fits on the receipt";
        basket.addProduct(new Product(longName, new Money(Sek.SEK, new BigDecimal("12.50")), food));
        basket.addProduct(new Product("Milk", new Money(Sek.SEK, new BigDecimal("12.50")), food));
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
    @DisplayName("Get Items Keeps Snapshot After Quantity Change")
    void getItemsKeepsSnapshotAfterQuantityChange() {
        basket.addItem(new BasketItem(milk, 2));
        basket.addProduct(bread);
        Receipt receipt = new Receipt(basket, fixedClock());

        basket.addProduct(milk);

        List<ReceiptLine> items = receipt.getItems();

        assertEquals(2, items.size());
        assertAll("Receipt keeps the original rows",
                () -> assertReceiptLine(items.get(0), "Milk", 2,
                        new BigDecimal("12.50"), new BigDecimal("25")),
                () -> assertReceiptLine(items.get(1), "Bread", 1,
                        new BigDecimal("20"), new BigDecimal("20"))
        );
    }

    @Test
    @DisplayName("Get Items Keeps Snapshot After Row Removal")
    void getItemsKeepsSnapshotAfterRowRemoval() {
        basket.addItem(new BasketItem(milk, 2));
        basket.addProduct(bread);
        Receipt receipt = new Receipt(basket, fixedClock());

        basket.removeProduct(bread);

        List<ReceiptLine> items = receipt.getItems();

        assertEquals(2, items.size());
        assertAll("Receipt keeps the original rows",
                () -> assertReceiptLine(items.get(0), "Milk", 2,
                        new BigDecimal("12.50"), new BigDecimal("25")),
                () -> assertReceiptLine(items.get(1), "Bread", 1,
                        new BigDecimal("20"), new BigDecimal("20"))
        );
    }

    @Test
    @DisplayName("Get Items Returns Unmodifiable List")
    void getItemsReturnsUnmodifiableList() {
        basket.addItem(new BasketItem(milk, 2));
        Receipt receipt = new Receipt(basket, fixedClock());

        List<ReceiptLine> items = receipt.getItems();

        assertThrows(UnsupportedOperationException.class, () -> items.clear());
        assertEquals(1, receipt.getItems().size());
        assertReceiptLine(receipt.getItems().get(0), "Milk", 2,
                new BigDecimal("12.50"), new BigDecimal("25"));
    }

    @Test
    @DisplayName("Get Items Returns Empty List For Empty Basket")
    void getItemsReturnsEmptyListForEmptyBasket() {
        Receipt receipt = new Receipt(basket, fixedClock());

        List<ReceiptLine> items = receipt.getItems();

        assertTrue(items.isEmpty());
    }

    @Test
    @DisplayName("Constructor Captures Timestamp In Clock Time Zone")
    void constructorCapturesTimestampInClockTimeZone() {
        Clock clock = fixedClock();
        LocalDateTime expectedTimestamp = LocalDateTime.of(2026, 1, 15, 11, 30);

        Receipt receipt = new Receipt(basket, clock);

        assertEquals(expectedTimestamp, receipt.getTimestamp());
    }

    @Test
    @DisplayName("Constructor Rejects Null Basket")
    void constructorRejectsNullBasket() {
        Basket basket = null;
        Clock clock = fixedClock();

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, clock));
    }

    @Test
    @DisplayName("Constructor Rejects Null Clock")
    void constructorRejectsNullClock() {
        Clock clock = null;

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, clock));
    }

    private void assertReceiptLine(ReceiptLine line, String expectedName, int expectedQuantity,
            BigDecimal expectedUnitPrice, BigDecimal expectedTotal) {
        assertAll("Receipt line for " + expectedName,
                () -> assertEquals(expectedName, line.getProductName()),
                () -> assertEquals(expectedQuantity, line.getQuantity()),
                () -> assertEquals(0, expectedUnitPrice.compareTo(line.getUnitPrice().getAmount())),
                () -> assertSame(Sek.SEK, line.getUnitPrice().getCurrency()),
                () -> assertEquals(0, expectedTotal.compareTo(line.getTotalPrice().getAmount())),
                () -> assertSame(Sek.SEK, line.getTotalPrice().getCurrency())
        );
    }
}
