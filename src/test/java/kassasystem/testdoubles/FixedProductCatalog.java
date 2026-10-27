package kassasystem.testdoubles;

import kassasystem.product.Product;
import kassasystem.scanning.ProductCatalog;
import kassasystem.scanning.EAN;

public class FixedProductCatalog extends ProductCatalog {
    private final Product product;
    private int lookupCalls;
    private EAN receivedEan;

    public FixedProductCatalog(Product product) {
        this.product = product;
    }

    @Override
    public Product findByEAn(EAN ean) {
        lookupCalls++;
        receivedEan = ean;
        return product;
    }

    public int getLookupCalls() {
        return lookupCalls;
    }

    public EAN getReceivedEan() {
        return receivedEan;
    }
}
