package kassasystem.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class CurrencyConversion {
    private final BigDecimal sekPerEur;
    private final BigDecimal conversionFee;
    private static final BigDecimal FIXED_CONVERSION_FEE = new BigDecimal("10.00");

    public CurrencyConversion(BigDecimal sekPerEur, BigDecimal conversionFee) {
        Objects.requireNonNull(sekPerEur, "Exchange rate must not be null");
        Objects.requireNonNull(conversionFee, "Conversion fee must not be null");
        if (sekPerEur.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be greater than zero");
        }
        if (conversionFee.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Conversion fee must be greater than zero");
        }
        this.sekPerEur = sekPerEur;
        this.conversionFee = conversionFee;

    }

    public Money convertToSek(Money amount) {
        Objects.requireNonNull(amount, "Amount must not be null");

        if (amount.getCurrency() == Sek.SEK) {
            return amount;
        }

        if (amount.getCurrency() == Eur.EUR) {
            BigDecimal convertedAmount = amount.getAmount().multiply(sekPerEur);
            BigDecimal percentageFee = convertedAmount.multiply(conversionFee);
            BigDecimal totalWithFees = convertedAmount.add(percentageFee)
                    .add(FIXED_CONVERSION_FEE);
            BigDecimal roundedAmount = totalWithFees.setScale(2, RoundingMode.HALF_UP);

            return new Money(Sek.SEK, roundedAmount);
        }

        throw new IllegalArgumentException("Unsupported currency");
    }
}
