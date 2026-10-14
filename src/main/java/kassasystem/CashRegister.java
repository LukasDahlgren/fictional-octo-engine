package kassasystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kassasystem.basket.Basket;

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
        if (currentPurchase != null) {
            throw new IllegalStateException("Cannot start a purchase while another purchase is current");
        }

        currentPurchase = new Purchase(new Basket());
        return currentPurchase;
    }

    public void pausePurchase() {
        if (currentPurchase == null) {
            throw new IllegalStateException("Cannot pause a purchase when there is no current purchase");
        }

        currentPurchase.pause();
        pausedPurchases.add(currentPurchase);
        currentPurchase = null;
    }

    public void resumePurchase(Purchase purchase) {
        // TODO: Resume a purchase from pausedPurchases when the register is free.
        throw new UnsupportedOperationException("CashRegister.resumePurchase() is not implemented yet");
    }

    public void completePurchase() {
        // TODO: Complete the current purchase and free the register after success.
        throw new UnsupportedOperationException("CashRegister.completePurchase() is not implemented yet");
    }

    public void cancelPurchase() {
        // TODO: Cancel the current purchase and free the register after success.
        throw new UnsupportedOperationException("CashRegister.cancelPurchase() is not implemented yet");
    }
}

