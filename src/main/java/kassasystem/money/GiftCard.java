
package kassasystem.money;

import java.util.Objects;
import java.math.BigDecimal;

public class GiftCard {

    private Money balance;

    public GiftCard(Money initialBalance) {
        Objects.requireNonNull(initialBalance, "Initial balance must not be null.");

        if (initialBalance.getAmount().compareTo(BigDecimal.ZERO) < 0) {
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
        if (!balance.getCurrency().equals(amount.getCurrency())) {
            throw new IllegalArgumentException(
                    "Currencies must match."
            );
        }

        if (amount.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Amount must not be negative."
            );
        }

        if (amount.isGreaterThan(balance)) {
            throw new IllegalArgumentException(
                    "Gift card balance is insufficient."
            );
        }

        balance = balance.subtract(amount);
    }
}

