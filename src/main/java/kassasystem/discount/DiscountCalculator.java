package kassasystem.discount;

import kassasystem.customer.Customer;
import kassasystem.money.Money;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DiscountCalculator {

    private static final BigDecimal MEMBERSHIP_DISCOUNT_PERCENTAGE = BigDecimal.TEN;

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
        BigDecimal greatestDiscountPercentage = BigDecimal.ZERO;

        if (customer.hasActiveMembershipOn(purchaseDate)) {
            greatestDiscountPercentage = MEMBERSHIP_DISCOUNT_PERCENTAGE;
        }

        if (offer != null && offer.isActiveOn(purchaseDate)) {
            BigDecimal offerDiscountPercentage = offer.getDiscountPercentage();

            if (offerDiscountPercentage.compareTo(greatestDiscountPercentage) > 0) {
                greatestDiscountPercentage = offerDiscountPercentage;
            }
        }

        if (greatestDiscountPercentage.compareTo(BigDecimal.ZERO) == 0) {
            return originalPrice;
        }

        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                greatestDiscountPercentage.divide(new BigDecimal("100")));
        BigDecimal discountedAmount = originalPrice.getAmount().multiply(discountMultiplier);

        return new Money(
                originalPrice.getCurrency(),
                discountedAmount
        );
    }
}
