package kassasystem.scanning;

import kassasystem.money.Money;
import kassasystem.money.Sek;
import kassasystem.product.Product;
import kassasystem.product.ProductCategory;
import kassasystem.product.VATCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class ScanResultTest {

    private final ProductCategory food = new ProductCategory("Food", VATCategory.SIX_PERCENT);

    private Product milk;

    @BeforeEach
    void setUp() {
        milk = new Product("Milk", new Money(Sek.SEK, 12.50), food);
    }

    @Test
    @DisplayName("SR-01: FOUND sparar status och exakt samma vara")
    void constructorStoresFoundStatusAndProduct() {
        ScanResult result = new ScanResult(ScanResult.Status.FOUND, milk);

        assertAll("Found result stores status and product",
                () -> assertEquals(ScanResult.Status.FOUND, result.getStatus()),
                () -> assertSame(milk, result.getProduct())
        );
    }

    @Test
    @DisplayName("SR-02: FOUND utan vara avvisas")
    void constructorRejectsFoundWithoutProduct() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScanResult(ScanResult.Status.FOUND, null));
    }

    @ParameterizedTest(name = "Status: {0}")
    @EnumSource(value = ScanResult.Status.class,
            names = {"FAILED_READ", "INVALID_CODE", "UNKNOWN_PRODUCT"})
    @DisplayName("SR-03: Ett misslyckat resultat sparar status och saknar vara")
    void constructorStoresFailureStatusWithoutProduct(ScanResult.Status status) {
        ScanResult result = new ScanResult(status, null);

        assertAll("Failure result stores status without a product",
                () -> assertEquals(status, result.getStatus()),
                () -> assertNull(result.getProduct())
        );
    }

    @ParameterizedTest(name = "Status: {0}")
    @EnumSource(value = ScanResult.Status.class,
            names = {"FAILED_READ", "INVALID_CODE", "UNKNOWN_PRODUCT"})
    @DisplayName("SR-04: Ett misslyckat resultat med en vara avvisas")
    void constructorRejectsFailureWithProduct(ScanResult.Status status) {
        assertThrows(IllegalArgumentException.class,
                () -> new ScanResult(status, milk));
    }

    @Test
    @DisplayName("SR-05: Saknad status avvisas utan vara")
    void constructorRejectsMissingStatusWithoutProduct() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScanResult(null, null));
    }

    @Test
    @DisplayName("SR-05: Saknad status avvisas med vara")
    void constructorRejectsMissingStatusWithProduct() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScanResult(null, milk));
    }
}
