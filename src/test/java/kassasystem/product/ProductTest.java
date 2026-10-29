package kassasystem.product;

import java.math.BigDecimal;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.scanning.EAN;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    @DisplayName("Rejects Null Name")
    void rejectsNullName() {
        Money price = new Money(Sek.SEK, new BigDecimal("12.50"));
        ProductCategory category = new ProductCategory("Food", VATCategory.SIX_PERCENT);
        EAN ean = new EAN("4006381333931");

        assertThrows(
                IllegalArgumentException.class,
                () -> new Product(null, price, category, ean)
        );
    }

    @Test
    @DisplayName("Rejects Null Price")
    void rejectsNullPrice() {
        ProductCategory category = new ProductCategory("Food", VATCategory.SIX_PERCENT);
        EAN ean = new EAN("4006381333931");

        assertThrows(
                IllegalArgumentException.class,
                () -> new Product("Milk", null, category, ean)
        );
    }

    @Test
    @DisplayName("Rejects Null Category")
    void rejectsNullCategory() {
        EAN ean = new EAN("4006381333931");

        assertThrows(
                IllegalArgumentException.class,
                () -> new Product(
                        "Milk",
                        new Money(Sek.SEK, new BigDecimal("12.50")),
                        null,
                        ean
                )
        );
    }

    @Test
    @DisplayName("Rejects Null EAN")
    void rejectsNullEAN() {
        Money price = new Money(Sek.SEK, new BigDecimal("12.50"));
        ProductCategory category = new ProductCategory("Food", VATCategory.SIX_PERCENT);

        assertThrows(
                IllegalArgumentException.class,
                () -> new Product("Milk", price, category, null)
        );
    }

    @Test
    @DisplayName("Returns Product Details")
    void returnsProductDetails() {
        Money price = new Money(Sek.SEK, new BigDecimal("12.50"));
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);
        EAN ean = new EAN("4006381333931");

        Product product = new Product("Milk", price, category, ean);

        assertEquals("Milk", product.getName());
        assertEquals(price, product.getPrice());
        assertEquals(category, product.getCategory());
        assertEquals(ean, product.getEan());
    }

    @Test
    @DisplayName("To String Returns Product Name")
    void toStringReturnsProductName() {
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product product = new Product(
                "Milk",
                new Money(Sek.SEK, new BigDecimal("12.50")),
                category,
                new EAN("4006381333931")
        );

        assertEquals("Milk", product.toString());
    }

    @Test
    @DisplayName("Generates EAN When Not Provided")
    void generatesEANWhenNotProvided() {
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);

        Product product = new Product(
                "Milk",
                new Money(Sek.SEK, new BigDecimal("12.50")),
                category
        );

        assertNotNull(product.getEan());
        assertEquals(13, product.getEan().getCode().length());
    }

    @Test
    @DisplayName("Rejects Blank Name")
    void rejectsBlankName() {
        Money price = new Money(Sek.SEK, new BigDecimal("12.50"));
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);
        EAN ean = new EAN("4006381333931");

        assertThrows(
                IllegalArgumentException.class,
                () -> new Product("   ", price, category, ean)
        );
    }

}