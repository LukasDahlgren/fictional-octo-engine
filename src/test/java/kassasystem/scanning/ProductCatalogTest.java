package kassasystem.scanning;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProductCatalogTest {

    @Test
    @DisplayName("Finds a product by its EAN")
    void findsProductByEAN() {
        EAN ean = new EAN("4006381333931");
        Product product = new Product(
                "Milk",
                new Money(Sek.SEK, 12.50),
                new ProductCategory("Food", VATCategory.SIX_PERCENT),
                ean
        );
        ProductCatalog catalog = new ProductCatalog();

        catalog.add(product);

        assertSame(product, catalog.findByEAn(ean));
    }

    @Test
    @DisplayName("Rejects a different product with an EAN already in use")
    void rejectsDuplicateEAN() {
        EAN ean = new EAN("4006381333931");

        Product firstProduct = new Product(
                "Milk",
                new Money(Sek.SEK, 12.50),
                new ProductCategory("Food", VATCategory.SIX_PERCENT),
                ean
        );
        Product secondProduct = new Product(
                "Bread",
                new Money(Sek.SEK, 20.00),
                new ProductCategory("Food", VATCategory.SIX_PERCENT),
                ean
        );

        ProductCatalog catalog = new ProductCatalog();
        catalog.add(firstProduct);

        assertThrows(IllegalArgumentException.class, () -> catalog.add(secondProduct));
        assertSame(firstProduct, catalog.findByEAn(ean));
    }

    @Test
    @DisplayName("Rejects adding a null product")
    void rejectsNullProduct() {
        ProductCatalog catalog = new ProductCatalog();

        assertThrows(IllegalArgumentException.class, () -> catalog.add(null));
    }

    @Test
    @DisplayName("Rejects searching with a null EAN")
    void rejectsNullEAN() {
        ProductCatalog catalog = new ProductCatalog();

        assertThrows(IllegalArgumentException.class, () -> catalog.findByEAn(null));
    }

    @Test
    @DisplayName("Allows adding the same product more than once")
    void allowsAddingSameProductAgain() {
        EAN ean = new EAN("4006381333931");
        Product product = new Product(
                "Milk",
                new Money(Sek.SEK, 12.50),
                new ProductCategory("Food", VATCategory.SIX_PERCENT),
                ean
        );
        ProductCatalog catalog = new ProductCatalog();

        catalog.add(product);
        catalog.add(product);

        assertSame(product, catalog.findByEAn(ean));
    }

}
