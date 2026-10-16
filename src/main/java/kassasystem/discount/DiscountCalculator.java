package kassasystem.discount;

import kassasystem.customer.Customer;
import kassasystem.money.Money;

import java.time.LocalDate;

public class DiscountCalculator {

    private static final double MEMBERSHIP_DISCOUNT_PERCENTAGE = 10.0;

    public Money calculatePrice(
            Money originalPrice,
            Customer customer,
            LocalDate purchaseDate
    ) {
        return calculatePrice(originalPrice, customer, purchaseDate, null);
    }

    public Money calculatePrice(
            Money originalPrice,
            Customer customer,
            LocalDate purchaseDate,
            DiscountOffer offer
    ) {
        double greatestDiscountPercentage = 0.0;

        if (customer.hasActiveMembershipOn(purchaseDate)) {
            greatestDiscountPercentage = MEMBERSHIP_DISCOUNT_PERCENTAGE;
        }

        if (offer != null && offer.isActiveOn(purchaseDate)) {
            double offerDiscountPercentage = offer.getDiscountPercentage();

            if (offerDiscountPercentage > greatestDiscountPercentage) {
                greatestDiscountPercentage = offerDiscountPercentage;
            }
        }

        if (greatestDiscountPercentage == 0.0) {
            return originalPrice;
        }

        double discountMultiplier =
                1.0 - greatestDiscountPercentage / 100.0;
        double discountedAmount =
                originalPrice.getAmount() * discountMultiplier;

        return new Money(
                originalPrice.getCurrency(),
                discountedAmount
        );
    }
}