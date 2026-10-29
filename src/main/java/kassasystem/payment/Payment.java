package kassasystem.payment;

import kassasystem.money.Money;

public class Payment {

    private final Money amount;
    private final Payer payer;

    public Payment(Money amount, Payer payer) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }
        if (amount.getAmount() < 0) {
            throw new IllegalArgumentException("Amount must not be negative");
        }

        if (payer == null) {
            throw new IllegalArgumentException("Payer must not be null");
        }
        if (!Double.isFinite(amount.getAmount())) {
            throw new IllegalArgumentException("Amount must be finite");
        }

        this.amount = amount;
        this.payer = payer;
    }

    public boolean pay() {
        return payer.pay(amount);
    }
}