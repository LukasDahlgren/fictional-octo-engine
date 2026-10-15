package kassasystem.scanning;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCatalog;
import kassasystem.scanning.EAN;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class ProductCatalogTest {

    @Test
    @DisplayName("Finds a product by its EAN")
    void findsProductByEAN() {
        EAN ean = new EAN("4006381333931");
        Product product = new Product(
                "Milk",
                new Money(Sek.SEK, 12.50),
                null,
                ean
        );
        ProductCatalog catalog = new ProductCatalog();

        catalog.add(product);

        assertSame(product, catalog.findByEAN(ean));
    }
}
