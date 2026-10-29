
package kassasystem.payment;

public class FakePayer{
    private final boolean paymentSucceeds;

    public FakePayer(boolean paymentSucceeds) {
        this.paymentSucceeds = paymentSucceeds;
    }

    public boolean pay() {
        return paymentSucceeds;
    }
}
