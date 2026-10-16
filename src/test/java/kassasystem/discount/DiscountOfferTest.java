package kassasystem.discount;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscountOfferTest {

    @Test
    @DisplayName("Erbjudandet är aktivt på startdatumet")
    void offerIsActiveOnStartDate() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);
        DiscountOffer offer = new DiscountOffer(
                20.0,
                startDate,
                endDate
        );

        boolean active = offer.isActiveOn(startDate);

        assertTrue(active);
    }
}