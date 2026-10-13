public class Product{

    private final String name;
    private final Money price;
    private ProductCategory category;

    public Product(String name, Money price, ProductCategory category) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }

        if (price == null) {
            throw new IllegalArgumentException("Price must not be null");
        }
        double amount = price.getAmount();
        if (!Double.isFinite(amount)) {
            throw new IllegalArgumentException("Price must be finite");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Price must not be negative");
        }

        this.name = name;
        this.price = price;
        this.category = category;
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

    @Override
    public String toString() {
        return name;
    }
}
