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
        return originalPrice;
    }
}