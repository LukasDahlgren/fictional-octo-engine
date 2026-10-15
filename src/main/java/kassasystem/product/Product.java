package kassasystem.product;

import kassasystem.money.Money;
import kassasystem.scanning.EAN;

public class Product{

    private final String name;
    private final Money price;
    private final EAN ean;
    private ProductCategory category;

    public Product(String name, Money price, ProductCategory category, EAN ean) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }

        if (price == null) {
            throw new IllegalArgumentException("Price must not be null");
        }

        if (ean == null) {
            throw new IllegalArgumentException("EAN must not be null");
        }

        this.name = name;
        this.price = price;
        this.category = category;
        this.ean = ean;
    }

    public String getName() {
        return name;
    }

    public Money getPrice() {
        return price;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public EAN getEan() {
        return ean;
    }
    @Override
    public String toString() {
        return name;
    }
}
