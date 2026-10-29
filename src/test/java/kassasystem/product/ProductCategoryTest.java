package kassasystem.product;

import java.math.BigDecimal;

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
    @DisplayName("Rejects Null VAT Category")
    void rejectsNullVATCategory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProductCategory("Food", null)
        );
    }

    @Test
    @DisplayName("Rejects Product Assigned To Another Category")
    void rejectsProductAssignedToAnotherCategory() {
        ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);
        ProductCategory drinks = new ProductCategory("Drinks", VATCategory.TWENTY_FIVE_PERCENT);

        Product milk = new Product(
                "Milk",
                new Money(Sek.SEK, new BigDecimal("12.50")),
                food,
                new EAN("4006381333931")
        );

        assertThrows(IllegalArgumentException.class, () -> drinks.add(milk));
    }

    @Test
    @DisplayName("Rejects Null Name")
    void rejectsNullName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProductCategory(null, VATCategory.SIX_PERCENT)
        );
    }

    @Test
    @DisplayName("Rejects Blank Name")
    void rejectsBlankName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProductCategory("   ", VATCategory.SIX_PERCENT)
        );
    }

    @Test
    @DisplayName("Returns Name And Vat Rate")
    void returnsNameAndVatRate() {
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);

        assertEquals("Food", category.getName());
        assertEquals(0, new BigDecimal("0.06").compareTo(category.getVat()));
    }

    @Test
    @DisplayName("Adds Product Assigned To This Category")
    void addsProductAssignedToThisCategory() {
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product product = createProduct(category, "Milk");

        assertTrue(category.add(product));
        assertTrue(category.contains(product));
        assertEquals(1, category.size());
    }

    @Test
    @DisplayName("Does Not Add Duplicate Product")
    void doesNotAddDuplicateProduct() {
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);
        Product product = createProduct(category, "Milk");

        assertTrue(category.add(product));

        assertFalse(category.add(product));
        assertEquals(1, category.size());
    }

    @Test
    @DisplayName("Rejects Null Product")
    void rejectsNullProduct() {
        ProductCategory category =
                new ProductCategory("Food", VATCategory.SIX_PERCENT);

        assertThrows(
                IllegalArgumentException.class,
                () -> category.add(null)
        );
    }

    @Test
    @DisplayName("Removes Product")
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
    @DisplayName("Iterator Returns Products")
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
    @DisplayName("Iterator Does Not Allow Removing Products")
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
                new Money(Sek.SEK, new BigDecimal("12.50")),
                category,
                new EAN("4006381333931")
        );
    }

}
