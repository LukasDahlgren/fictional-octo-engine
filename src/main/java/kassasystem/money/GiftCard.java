
package kassasystem.money;

import java.util.Objects;

public class GiftCard {

    private Money balance;

    public GiftCard(Money initialBalance) {
        Objects.requireNonNull(initialBalance, "Startsaldo får inte vara null.");

        if (initialBalance.getAmount() < 0) {
            throw new IllegalArgumentException(
                    "Startsaldo får inte vara negativt."
            );
        }

        this.balance = initialBalance;
    }

    public Money getBalance() {
        return balance;
    }

    public void use(Money amount) {
        Objects.requireNonNull(amount, "Belopp får inte vara null.");
        if (!Double.isFinite(amount.getAmount())) {
            throw new IllegalArgumentException("Beloppet måste vara ändligt.");
        }

        if (!balance.getCurrency().equals(amount.getCurrency())) {
            throw new IllegalArgumentException(
                    "Valutorna måste vara samma."
            );
        }

        if (amount.getAmount() < 0) {
            throw new IllegalArgumentException(
                    "Beloppet får inte vara negativt."
            );
        }

        if (amount.getAmount() > balance.getAmount()) {
            throw new IllegalArgumentException(
                    "Presentkortet har inte tillräckligt saldo."
            );
        }

        balance = balance.subtract(amount);
    }
}

