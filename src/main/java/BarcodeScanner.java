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
        String ean = codeReader.readCode();

        if(ean == null){
            return new ScanResult(ScanResult.Status.FAILED_READ, null);
        }

        if(ean.isBlank()){
            return new ScanResult(ScanResult.Status.INVALID_CODE, null);
        }

        throw new UnsupportedOperationException("Scanning is not implemented yet");
    }
}
