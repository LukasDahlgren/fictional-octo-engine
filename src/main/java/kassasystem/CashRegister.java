package kassasystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CashRegister {

    private Purchase currentPurchase;
    private final List<Purchase> pausedPurchases = new ArrayList<>();

    // Returns null when the register has no current purchase.
    public Purchase getCurrentPurchase() {
        return currentPurchase;
    }

    public List<Purchase> getPausedPurchases() {
        return Collections.unmodifiableList(pausedPurchases);
    }

    public Purchase startPurchase() {
        // TODO: Create a purchase when the register has no current purchase.
        throw new UnsupportedOperationException("Starting a purchase is not implemented yet");
    }

    public void pausePurchase() {
        // TODO: Pause the current purchase, store it, and free the register.
        throw new UnsupportedOperationException("Pausing a purchase is not implemented yet");
    }

    public void resumePurchase(Purchase purchase) {
        // TODO: Resume a purchase from pausedPurchases when the register is free.
        throw new UnsupportedOperationException("Resuming a purchase is not implemented yet");
    }

    public void completePurchase() {
        // TODO: Complete the current purchase and free the register after success.
        throw new UnsupportedOperationException("Completing a purchase is not implemented yet");
    }

    public void cancelPurchase() {
        // TODO: Cancel the current purchase and free the register after success.
        throw new UnsupportedOperationException("Cancelling a purchase is not implemented yet");
    }
}

