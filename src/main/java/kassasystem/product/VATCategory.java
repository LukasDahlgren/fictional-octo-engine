package kassasystem.product;

public enum VATCategory {
    SIX_PERCENT(0.06),
    TWELVE_PERCENT(0.12),
    TWENTY_FIVE_PERCENT(0.25);

    private final double rate;

    VATCategory(double rate) {
        this.rate = rate;
    }

    public double getRate() {
        return rate;
    }
}
