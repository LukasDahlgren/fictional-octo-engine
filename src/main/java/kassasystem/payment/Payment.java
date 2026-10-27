package kassasystem.payment;

import kassasystem.money.Money;

public class Payment {

    private final Money amount;
    private final Payer payer;

    public Payment(Money amount, Payer payer) {
        if (payer == null) {
            throw new IllegalArgumentException("Payer must not be null");
        }

        this.amount = amount;
        this.payer = payer;
    }

    public boolean pay() {
        return payer.pay(amount);
    }
}