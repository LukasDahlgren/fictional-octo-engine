public class ScanResult {

    public enum Status {
        FOUND,
        FAILED_READ,
        INVALID_CODE,
        UNKNOWN_PRODUCT
    }

    private final Status status;
    private final Product product;

    public ScanResult(Status status, Product product) {
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null");
        }
        if (status == Status.FOUND) {
            if (product == null) {
                throw new IllegalArgumentException("A found result must include a product");
            }
        } else {
            if (product != null) {
                throw new IllegalArgumentException("A failed result must not include a product");
            }
        }

        this.status = status;
        this.product = product;
    }

    public Status getStatus() {
        return status;
    }

    public Product getProduct() {
        return product;
    }
}
