public class BarcodeScanner {

    public interface CodeReader {
        // Return null when no code could be read.
        String readCode();
    }

    private final CodeReader codeReader;
    private final ProductCatalog productCatalog;

    public BarcodeScanner(CodeReader codeReader, ProductCatalog productCatalog) {
        if (codeReader == null) {
            throw new IllegalArgumentException("Code reader must not be null");
        }
        if (productCatalog == null) {
            throw new IllegalArgumentException("Product catalog must not be null");
        }

        this.codeReader = codeReader;
        this.productCatalog = productCatalog;
    }

    public ScanResult scan() {
        String eanString = codeReader.readCode();

        if(eanString == null){
            return new ScanResult(ScanResult.Status.FAILED_READ, null);
        }

        if(eanString.isBlank()){
            return new ScanResult(ScanResult.Status.INVALID_CODE, null);
        }

        EAN ean = new EAN(eanString);
        Product product = productCatalog.findByEAn(ean);

        if(product==null){
            return new ScanResult(ScanResult.Status.UNKNOWN_PRODUCT, null);
        }

        
        return new ScanResult(ScanResult.Status.FOUND, product);
        
        // TODO: Create an EAN from the code and call productCatalog.findByEAn(ean).
        // TODO: Return UNKNOWN_PRODUCT or FOUND with the matching product.
    }
}
