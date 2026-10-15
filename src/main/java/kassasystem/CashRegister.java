package kassasystem;

import kassasystem.basket.Basket;
import kassasystem.scanning.BarcodeScanner;
import kassasystem.scanning.ScanResult;

public class CashRegister {

    private Purchase currentPurchase;

    // Returns null when the register has no current purchase.
    public Purchase getCurrentPurchase() {
        return currentPurchase;
    }

    public Purchase startPurchase() {
        if (currentPurchase != null) {
            throw new IllegalStateException("Cannot start a purchase while another purchase is current");
        }

        currentPurchase = new Purchase(new Basket());
        return currentPurchase;
    }

    public ScanResult scanProduct(BarcodeScanner scanner) {
        if(currentPurchase == null){
            throw new IllegalStateException("Utan ett pågående köp kan en vara inte scannas");
        }

        if(currentPurchase.getStatus() != Purchase.Status.ACTIVE){
            throw new IllegalStateException("Varor kan endast scannas till ett aktivt köp");
        }

        ScanResult result = scanner.scan();

        if(result.getStatus() != ScanResult.Status.FOUND){
            return result;
        }

        currentPurchase.getBasket().addProduct(result.getProduct());
        return result;
    }

    public void completePurchase() {
        if (currentPurchase == null) {
            throw new IllegalStateException("Cannot complete a purchase when there is no current purchase");
        }

        //Här borde någon form av "betalning" ske.
        //
        currentPurchase.complete();
        currentPurchase = null;
    }

    public void cancelPurchase() {
        if (currentPurchase == null) {
            throw new IllegalStateException("Cannot cancel a purchase when there is no current purchase");
        }

        currentPurchase.cancel();
        currentPurchase = null;
    }
}

