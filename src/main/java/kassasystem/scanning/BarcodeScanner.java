package kassasystem.scanning;

import kassasystem.product.Product;
import kassasystem.product.ProductCatalog;

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

        EAN ean;

        try{
            ean = new EAN(eanString);
        }
        catch (IllegalArgumentException e){
            return new ScanResult(ScanResult.Status.INVALID_CODE, null);

        }

        Product product = productCatalog.findByEAn(ean);

        if(product==null){
            return new ScanResult(ScanResult.Status.UNKNOWN_PRODUCT, null);
        }

        return new ScanResult(ScanResult.Status.FOUND, product);
    }
}
