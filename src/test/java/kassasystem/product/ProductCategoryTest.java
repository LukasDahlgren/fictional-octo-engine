package kassasystem.product;

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
}
