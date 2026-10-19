package kassasystem.product;

import kassasystem.scanning.EAN;

import java.util.HashMap;
import java.util.Map;

public class ProductCatalog {

    private final Map<String, Product> productsByEAN = new HashMap<>();

    public void add(Product product) {
        if(product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
        String eanCode = product.getEan().getCode();
        Product existingProduct = productsByEAN.get(eanCode);

        if(existingProduct != null && existingProduct != product) {
            throw new IllegalArgumentException("EAN is already assigned to another product");
        }

        productsByEAN.put(product.getEan().getCode(), product);
    }

    public Product findByEAn(EAN ean) {
        if(ean == null) {
            throw new IllegalArgumentException("EAN must not be null");
        }
        return productsByEAN.get(ean.getCode());
    }

}
