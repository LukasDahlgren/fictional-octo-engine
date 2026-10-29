package kassasystem.product;

import java.math.BigDecimal;

public enum VATCategory {
    SIX_PERCENT("0.06"),
    TWELVE_PERCENT("0.12"),
    TWENTY_FIVE_PERCENT("0.25");

    private final BigDecimal rate;

    VATCategory(String rate) {
        this.rate = new BigDecimal(rate);
    }

    public BigDecimal getRate() {
        return rate;
    }
}
