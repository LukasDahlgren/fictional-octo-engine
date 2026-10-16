package kassasystem.discount;

import kassasystem.customer.Customer;
import kassasystem.money.Money;

import java.time.LocalDate;

public class DiscountCalculator {

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
        if (offer != null && offer.isActiveOn(purchaseDate)) {
            double discountPercentage = offer.getDiscountPercentage();
            double discountMultiplier = 1.0 - discountPercentage / 100.0;
            double discountedAmount = originalPrice.getAmount() * discountMultiplier;

            return new Money(
                    originalPrice.getCurrency(),
                    discountedAmount
            );
        }

        if (customer.hasActiveMembershipOn(purchaseDate)) {
            double discountedAmount = originalPrice.getAmount() * 0.90;

            return new Money(
                    originalPrice.getCurrency(),
                    discountedAmount
            );
        }

        return originalPrice;
    }
}