package kassasystem.discount;

import java.time.LocalDate;

public class DiscountOffer {

    public DiscountOffer(
            double discountPercentage,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    public boolean isActiveOn(LocalDate date) {
        return true;
    }
}