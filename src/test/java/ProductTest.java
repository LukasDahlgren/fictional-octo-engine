import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    @ParameterizedTest(name = "Pris {0}")
    @ValueSource(doubles = {0, Double.MIN_VALUE, 0.01, 12.50, Double.MAX_VALUE})
    @DisplayName("Noll och positiva ändliga styckpriser accepteras")
    void constructorAcceptsNonNegativeFinitePrice(double amount) {
        Money price = new Money(Sek.SEK, amount);

        Product product = new Product("Milk", price, null);

        assertSame(price, product.getPrice());
    }

    @ParameterizedTest(name = "Pris {0}")
    @ValueSource(doubles = {-Double.MIN_VALUE, -0.01, -12.50, -Double.MAX_VALUE})
    @DisplayName("Negativa styckpriser avvisas")
    void constructorRejectsNegativePrice(double amount) {
        Money price = new Money(Sek.SEK, amount);

        assertThrows(IllegalArgumentException.class, () -> new Product("Milk", price, null));
    }

    @ParameterizedTest(name = "Pris {0}")
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    @DisplayName("NaN och oändliga styckpriser avvisas")
    void constructorRejectsNonFinitePrice(double amount) {
        Money price = new Money(Sek.SEK, amount);

        assertThrows(IllegalArgumentException.class, () -> new Product("Milk", price, null));
    }

    @Test
    @DisplayName("Ett saknat styckepris avvisas")
    void constructorRejectsNullPrice() {
        assertThrows(IllegalArgumentException.class, () -> new Product("Milk", null, null));
    }
}
