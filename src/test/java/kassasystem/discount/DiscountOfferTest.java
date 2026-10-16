package kassasystem.discount;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    @Test
    @DisplayName("Erbjudandet är inaktivt före startdatumet")
    void offerIsInactiveBeforeStartDate() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);
        DiscountOffer offer = new DiscountOffer(
                20.0,
                startDate,
                endDate
        );
        LocalDate dayBeforeStart = LocalDate.of(2026, 9, 30);

        boolean active = offer.isActiveOn(dayBeforeStart);

        assertFalse(active);
    }
    @Test
    @DisplayName("Erbjudandet är inaktivt efter slutdatumet")
    void offerIsInactiveAfterEndDate() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);
        DiscountOffer offer = new DiscountOffer(
                20.0,
                startDate,
                endDate
        );
        LocalDate dayAfterEnd = LocalDate.of(2026, 11, 1);

        boolean active = offer.isActiveOn(dayAfterEnd);

        assertFalse(active);
    }
}