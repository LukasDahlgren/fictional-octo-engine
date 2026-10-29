package kassasystem.discount;

import java.math.BigDecimal;

import java.time.LocalDate;

public class DiscountOffer {

    private final BigDecimal discountPercentage;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public DiscountOffer(
            BigDecimal discountPercentage,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (discountPercentage == null
                || discountPercentage.compareTo(BigDecimal.ZERO) < 0
                || discountPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException(
                    "Discount percentage must be between 0 and 100"
            );
        }

        this.discountPercentage = discountPercentage;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public boolean isActiveOn(LocalDate date) {
        boolean hasStarted = !date.isBefore(startDate);
        boolean hasNotEnded = !date.isAfter(endDate);

        return hasStarted && hasNotEnded;
    }
}
