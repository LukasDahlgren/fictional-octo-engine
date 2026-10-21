package kassasystem.product;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.scanning.EAN;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
}