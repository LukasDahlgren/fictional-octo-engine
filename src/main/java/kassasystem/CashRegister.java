package kassasystem;

import java.time.Clock;

import kassasystem.basket.Basket;
import kassasystem.scanning.BarcodeScanner;
import kassasystem.scanning.ScanResult;
import kassasystem.receipt.Receipt;

public class CashRegister {

    private final Clock clock;
    private Purchase currentPurchase;

    public CashRegister() {
        this(Clock.systemUTC());
    }

    public CashRegister(Clock clock) {
        if (clock == null) {
            throw new IllegalArgumentException("Clock must not be null");
        }
        this.clock = clock;
    }

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

    public Receipt completePurchase() {
        if (currentPurchase == null) {
            throw new IllegalStateException("Cannot complete a purchase when there is no current purchase");
        }

        //Här borde någon form av "betalning" ske.
        //
        Receipt receipt = new Receipt(currentPurchase.getBasket(), clock);
        currentPurchase.complete();
        currentPurchase = null;
        return receipt;
    }

    public void cancelPurchase() {
        if (currentPurchase == null) {
            throw new IllegalStateException("Cannot cancel a purchase when there is no current purchase");
        }

        currentPurchase.cancel();
        currentPurchase = null;
    }
}

