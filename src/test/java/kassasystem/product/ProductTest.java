package kassasystem.product;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.scanning.EAN;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    @DisplayName("Rejects a null product name")
    void rejectsNullName() {
        Money price = new Money(Sek.SEK, 12.50);
        ProductCategory category = new ProductCategory("Food", VATCategory.SIX_PERCENT);
        EAN ean = new EAN("4006381333931");

        assertThrows(
                IllegalArgumentException.class,
                () -> new Product(null, price, category, ean)
        );
    }

    @Test
    @DisplayName("Rejects a null price")
    void rejectsNullPrice() {
        ProductCategory category = new ProductCategory("Food", VATCategory.SIX_PERCENT);
        EAN ean = new EAN("4006381333931");

        assertThrows(
                IllegalArgumentException.class,
                () -> new Product("Milk", null, category, ean)
        );
    }

    @Test
    @DisplayName("Rejects a product without a category")
    void rejectsNullCategory() {
        EAN ean = new EAN("4006381333931");

        assertThrows(
                IllegalArgumentException.class,
                () -> new Product(
                        "Milk",
                        new Money(Sek.SEK, 12.50),
                        null,
                        ean
                )
        );
    }

    @Test
    @DisplayName("Rejects a null EAN")
    void rejectsNullEAN() {
        Money price = new Money(Sek.SEK, 12.50);
        ProductCategory category = new ProductCategory("Food", VATCategory.SIX_PERCENT);

        assertThrows(
                IllegalArgumentException.class,
                () -> new Product("Milk", price, category, null)
        );
    }

    @Test
    @DisplayName("Returns the product details supplied to the constructor")
    void returnsProductDetails() {
        Money price = new Money(Sek.SEK, 12.50);
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
    @DisplayName("Returns the product name from toString")
    void toStringReturnsProductName() {
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product product = new Product(
                "Milk",
                new Money(Sek.SEK, 12.50),
                category,
                new EAN("4006381333931")
        );

        assertEquals("Milk", product.toString());
    }

    @Test
    @DisplayName("Generates an EAN when none is supplied")
    void generatesEANWhenNotProvided() {
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);

        Product product = new Product(
                "Milk",
                new Money(Sek.SEK, 12.50),
                category
        );

        assertNotNull(product.getEan());
        assertEquals(13, product.getEan().getCode().length());
    }

    @Test
    @DisplayName("Rejects a blank product name")
    void rejectsBlankName() {
        Money price = new Money(Sek.SEK, 12.50);
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);
        EAN ean = new EAN("4006381333931");

        assertThrows(
                IllegalArgumentException.class,
                () -> new Product("   ", price, category, ean)
        );
    }

}