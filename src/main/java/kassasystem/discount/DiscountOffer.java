package kassasystem.discount;

import java.time.LocalDate;

public class DiscountOffer {

    private final LocalDate startDate;

    public DiscountOffer(
            double discountPercentage,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.startDate = startDate;
    }

    public boolean isActiveOn(LocalDate date) {
        return !date.isBefore(startDate);
    }
}