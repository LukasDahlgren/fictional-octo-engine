package kassasystem.discount;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscountOfferTest {

    private LocalDate startDate;
    private LocalDate endDate;
    private DiscountOffer offer;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2026, 10, 1);
        endDate = LocalDate.of(2026, 10, 31);
        offer = new DiscountOffer(new BigDecimal("20.0"), startDate, endDate);
    }

    @Test
    @DisplayName("Offer Is Active On Start Date")
    void offerIsActiveOnStartDate() {
        boolean active = offer.isActiveOn(startDate);

        assertTrue(active);
    }

    @Test
    @DisplayName("Offer Is Active On End Date")
    void offerIsActiveOnEndDate() {
        boolean active = offer.isActiveOn(endDate);

        assertTrue(active);
    }

    @Test
    @DisplayName("Offer Is Inactive Before Start Date")
    void offerIsInactiveBeforeStartDate() {
        LocalDate dayBeforeStart = LocalDate.of(2026, 9, 30);

        boolean active = offer.isActiveOn(dayBeforeStart);

        assertFalse(active);
    }

    @Test
    @DisplayName("Offer Is Inactive After End Date")
    void offerIsInactiveAfterEndDate() {
        LocalDate dayAfterEnd = LocalDate.of(2026, 11, 1);

        boolean active = offer.isActiveOn(dayAfterEnd);

        assertFalse(active);
    }

    @Test
    @DisplayName("Offer Has Discount Percentage")
    void offerHasDiscountPercentage() {
        assertEquals(new BigDecimal("20.0"), offer.getDiscountPercentage());
    }

    @ParameterizedTest(name = "{0} procent")
    @ValueSource(strings = {"-1.0", "101.0"})
    @DisplayName("Discount Percentage Outside Valid Range Is Rejected")
    void discountPercentageOutsideValidRangeIsRejected(
            BigDecimal discountPercentage
    ) {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DiscountOffer(
                        discountPercentage,
                        startDate,
                        endDate
                )
        );
    }
    @ParameterizedTest(name = "{0} procent")
    @ValueSource(strings = {"0.0", "100.0"})
    @DisplayName("Discount Percentage On Boundaries Is Accepted")
    void discountPercentageOnBoundariesIsAccepted(
            BigDecimal discountPercentage
    ) {
        DiscountOffer boundaryOffer = new DiscountOffer(
                discountPercentage,
                startDate,
                endDate
        );

        assertEquals(
                discountPercentage,
                boundaryOffer.getDiscountPercentage()
        );
    }

    @Test
    @DisplayName("Discount Offer Rejects A Null Percentage")
    void discountOfferRejectsNullPercentage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DiscountOffer(null, startDate, endDate)
        );
    }
}
