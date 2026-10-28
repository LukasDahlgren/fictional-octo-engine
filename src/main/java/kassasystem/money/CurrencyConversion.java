package kassasystem.money;

import java.util.Objects;

public class CurrencyConversion {
    private final double sekPerEur;
    private final double conversionFee;
    private static final double FIXED_CONVERSION_FEE = 10.0;

    public CurrencyConversion(double sekPerEur, double conversionFee) {
        if (!Double.isFinite(sekPerEur)) {
            throw new IllegalArgumentException("Valutakursen måste vara ändlig");
        }
        if (sekPerEur <= 0) {
            throw new IllegalArgumentException("Valutakursen måste vara över 0");
        }
        if (!Double.isFinite(conversionFee)) {
            throw new IllegalArgumentException("Växlingsavgiften måste vara ändlig");
        }
        if (conversionFee <= 0) {
            throw new IllegalArgumentException("Växlingsavgiften får inte vara noll");
        }
        this.sekPerEur = sekPerEur;
        this.conversionFee = conversionFee;

    }

    public Money convertToSek(Money amount) {
        Objects.requireNonNull(amount, "Beloppet amount får inte vara null");

        if (amount.getCurrency() == Sek.SEK) {
            return amount;
        }

        if (amount.getCurrency() == Eur.EUR) {
            double convertedAmount = amount.getAmount() * sekPerEur;
            double percentageFee = convertedAmount * conversionFee;
            double totalWithFees =
                    convertedAmount + percentageFee + FIXED_CONVERSION_FEE;

            double roundedAmount =
                    Math.round(totalWithFees * 100.0) / 100.0;

            return new Money(Sek.SEK, roundedAmount);
        }

        throw new IllegalArgumentException("Valutan finns inte");
    }
}
