package kassasystem.money;

import java.util.Objects;

public class CurrencyConversion {
    private final double sekPerEur;
    private final double conversionFee;

    public CurrencyConversion(double sekPerEur, double conversionFee) {
        if (sekPerEur <= 0) {
            throw new IllegalArgumentException("Valutakursen måste vara över 0");
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
            double MINIMAL_CONVERSION_FEE = 10;
            double convertedAmount = amount.getAmount() * sekPerEur * (1 + conversionFee) + MINIMAL_CONVERSION_FEE;
            convertedAmount = Math.round(convertedAmount * 100.0) / 100.0;
            return new Money(Sek.SEK, convertedAmount);
        }

        throw new IllegalArgumentException("Valutan finns inte");
    }
}
