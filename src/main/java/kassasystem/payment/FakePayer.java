package kassasystem.payment;

import kassasystem.money.Money;

public class FakePayer implements Payer {
    private final boolean paymentSucceeds;

    public FakePayer(boolean paymentSucceeds) {
        this.paymentSucceeds = paymentSucceeds;
    }

    @Override
    public boolean pay(Money amount) {
        return paymentSucceeds;
    }
}
