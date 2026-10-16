package kassasystem.discount;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    @Test
    @DisplayName("Erbjudandet innehåller sin rabattprocent")
    void offerHasDiscountPercentage() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);
        DiscountOffer offer = new DiscountOffer(
                20.0,
                startDate,
                endDate
        );

        double discountPercentage = offer.getDiscountPercentage();

        assertEquals(20.0, discountPercentage, 0.001);
    }
    @Test
    @DisplayName("Erbjudandet är aktivt på slutdatumet")
    void offerIsActiveOnEndDate() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);
        DiscountOffer offer = new DiscountOffer(20.0, startDate, endDate);

        boolean active = offer.isActiveOn(endDate);

        assertTrue(active);
    }
}