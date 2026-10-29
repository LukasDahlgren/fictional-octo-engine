
package kassasystem.payment;

import kassasystem.money.Money;

public interface Payer {
    boolean pay(Money amount);
}

