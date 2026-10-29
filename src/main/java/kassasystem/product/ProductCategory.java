package kassasystem.product;

import java.math.BigDecimal;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class ProductCategory implements Iterable<Product> {

    private final String name;
    private final Set<Product> products = new LinkedHashSet<>();
    private final VATCategory vat;

    public ProductCategory(String name, VATCategory vat) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }

        if (vat == null) {
            throw new IllegalArgumentException("VAT category must not be null");
        }

        this.name = name;
        this.vat = vat;
    }

    public String getName() {
        return name;
    }

    public boolean add(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }

        if (product.getCategory() != this) {
            throw new IllegalArgumentException(
                    "Product belongs to a different category"
            );
        }

        return products.add(product);
    }

    public boolean remove(Product product) {
        return products.remove(product);
    }

    public boolean contains(Product product) {
        return products.contains(product);
    }

    public int size() {
        return products.size();
    }

    public BigDecimal getVat() {
        return vat.getRate();
    }

    @Override
    public Iterator<Product> iterator() {
        return Collections.unmodifiableSet(products).iterator();
    }
}
