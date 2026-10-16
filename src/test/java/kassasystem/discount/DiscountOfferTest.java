package kassasystem.discount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscountOfferTest {

    private LocalDate startDate;
    private LocalDate endDate;
    private DiscountOffer offer;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2026, 10, 1);
        endDate = LocalDate.of(2026, 10, 31);
        offer = new DiscountOffer(20.0, startDate, endDate);
    }

    @Test
    @DisplayName("Erbjudandet är aktivt på startdatumet")
    void offerIsActiveOnStartDate() {
        boolean active = offer.isActiveOn(startDate);

        assertTrue(active);
    }

    @Test
    @DisplayName("Erbjudandet är aktivt på slutdatumet")
    void offerIsActiveOnEndDate() {
        boolean active = offer.isActiveOn(endDate);

        assertTrue(active);
    }

    @Test
    @DisplayName("Erbjudandet är inaktivt före startdatumet")
    void offerIsInactiveBeforeStartDate() {
        LocalDate dayBeforeStart = LocalDate.of(2026, 9, 30);

        boolean active = offer.isActiveOn(dayBeforeStart);

        assertFalse(active);
    }

    @Test
    @DisplayName("Erbjudandet är inaktivt efter slutdatumet")
    void offerIsInactiveAfterEndDate() {
        LocalDate dayAfterEnd = LocalDate.of(2026, 11, 1);

        boolean active = offer.isActiveOn(dayAfterEnd);

        assertFalse(active);
    }

    @Test
    @DisplayName("Erbjudandet har en rabattprocent")
    void offerHasDiscountPercentage() {
        assertEquals(20.0, offer.getDiscountPercentage());
    }
}