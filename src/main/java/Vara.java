public class Vara {

    private final String name;
    private final Money price;

    public Vara(String name, Money price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }

        if (price == null) {
            throw new IllegalArgumentException("Price must not be null");
        }

        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public Money getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return name;
    }
}
