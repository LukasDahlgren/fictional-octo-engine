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