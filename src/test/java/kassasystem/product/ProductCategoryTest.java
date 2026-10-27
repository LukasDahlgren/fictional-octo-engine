package kassasystem.product;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.scanning.EAN;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProductCategoryTest {

    @Test
    @DisplayName("Rejects a null VAT category")
    void rejectsNullVATCategory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProductCategory("Food", null)
        );
    }

    @Test
    @DisplayName("Rejects adding a product assigned to a different category")
    void rejectsProductAssignedToAnotherCategory() {
        ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);
        ProductCategory drinks = new ProductCategory("Drinks", VATCategory.TWENTY_FIVE_PERCENT);

        Product milk = new Product(
                "Milk",
                new Money(Sek.SEK, 12.50),
                food,
                new EAN("4006381333931")
        );

        assertThrows(IllegalArgumentException.class, () -> drinks.add(milk));
    }

    @Test
    @DisplayName("Rejects a null category name")
    void rejectsNullName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProductCategory(null, VATCategory.SIX_PERCENT)
        );
    }

    @Test
    @DisplayName("Rejects a blank category name")
    void rejectsBlankName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProductCategory("   ", VATCategory.SIX_PERCENT)
        );
    }

    @Test
    @DisplayName("Returns the category name and VAT rate")
    void returnsNameAndVatRate() {
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);

        assertEquals("Food", category.getName());
        assertEquals(0.06, category.getVat(), 0.0001);
    }

    @Test
    @DisplayName("Adds a product assigned to this category")
    void addsProductAssignedToThisCategory() {
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product product = createProduct(category, "Milk");

        assertTrue(category.add(product));
        assertTrue(category.contains(product));
        assertEquals(1, category.size());
    }

    @Test
    @DisplayName("Does not add the same product twice")
    void doesNotAddDuplicateProduct() {
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product product = createProduct(category, "Milk");

        assertTrue(category.add(product));

        assertFalse(category.add(product));
        assertEquals(1, category.size());
    }

    @Test
    @DisplayName("Rejects adding a null product")
    void rejectsNullProduct() {
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);

        assertThrows(
                IllegalArgumentException.class,
                () -> category.add(null)
        );
    }

    @Test
    @DisplayName("Removes a product from the category")
    void removesProduct() {
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product product = createProduct(category, "Milk");
        category.add(product);

        assertTrue(category.remove(product));
        assertFalse(category.contains(product));
        assertEquals(0, category.size());
        assertFalse(category.remove(product));
    }

    @Test
    @DisplayName("Returns category products through its iterator")
    void iteratorReturnsProducts() {
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product product = createProduct(category, "Milk");
        category.add(product);

        Iterator<Product> iterator = category.iterator();

        assertSame(product, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    @DisplayName("Does not allow removing a product through the iterator")
    void iteratorDoesNotAllowRemovingProducts() {
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product product = createProduct(category, "Milk");
        category.add(product);
        Iterator<Product> iterator = category.iterator();
        iterator.next();

        assertThrows(UnsupportedOperationException.class, iterator::remove);
        assertTrue(category.contains(product));
    }

    private Product createProduct(ProductCategory category, String name) {
        return new Product(
                name,
                new Money(Sek.SEK, 12.50),
                category,
                new EAN("4006381333931")
        );
    }

}
