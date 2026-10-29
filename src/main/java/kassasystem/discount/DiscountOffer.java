package kassasystem.discount;

import java.time.LocalDate;

public class DiscountOffer {

    private final double discountPercentage;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public DiscountOffer(
            double discountPercentage,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (discountPercentage < 0.0 || discountPercentage > 100.0) {
            throw new IllegalArgumentException(
                    "Discount percentage must be between 0 and 100"
            );
        }

        this.discountPercentage = discountPercentage;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public boolean isActiveOn(LocalDate date) {
        boolean hasStarted = !date.isBefore(startDate);
        boolean hasNotEnded = !date.isAfter(endDate);

        return hasStarted && hasNotEnded;
    }
}
