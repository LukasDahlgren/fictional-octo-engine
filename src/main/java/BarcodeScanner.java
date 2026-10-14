public class BarcodeScanner {

    public interface CodeReader {
        // Return null when no code could be read.
        String readCode();
    }

    public interface ProductLookup {
        // Return null when no product matches the code.
        Product findByCode(String code);
    }

    private final CodeReader codeReader;
    private final ProductLookup productLookup;

    public BarcodeScanner(CodeReader codeReader, ProductLookup productLookup) {
        if (codeReader == null) {
            throw new IllegalArgumentException("Code reader must not be null");
        }
        if (productLookup == null) {
            throw new IllegalArgumentException("Product lookup must not be null");
        }

        this.codeReader = codeReader;
        this.productLookup = productLookup;
    }

    public ScanResult scan() {
        // TODO: Read a code and handle failed reads or blank codes.
        // TODO: Look up the product and handle unknown products.
        // TODO: Return a ScanResult with the status and matching product.
        throw new UnsupportedOperationException("Scanning is not implemented yet");
    }
}
