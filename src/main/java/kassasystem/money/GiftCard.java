
package kassasystem.money;

import java.util.Objects;

public class GiftCard {

    private Money balance;

    public GiftCard(Money initialBalance) {
        Objects.requireNonNull(initialBalance, "Initial balance must not be null.");

        if (initialBalance.getAmount() < 0) {
            throw new IllegalArgumentException(
                    "Initial balance must not be negative."
            );
        }

        this.balance = initialBalance;
    }

    public Money getBalance() {
        return balance;
    }

    public void use(Money amount) {
        Objects.requireNonNull(amount, "Amount must not be null.");
        if (!Double.isFinite(amount.getAmount())) {
            throw new IllegalArgumentException("Amount must be finite.");
        }

        if (!balance.getCurrency().equals(amount.getCurrency())) {
            throw new IllegalArgumentException(
                    "Currencies must match."
            );
        }

        if (amount.getAmount() < 0) {
            throw new IllegalArgumentException(
                    "Amount must not be negative."
            );
        }

        if (amount.getAmount() > balance.getAmount()) {
            throw new IllegalArgumentException(
                    "Gift card balance is insufficient."
            );
        }

        balance = balance.subtract(amount);
    }
}

