package kassasystem.product;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.scanning.EAN;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

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

}
