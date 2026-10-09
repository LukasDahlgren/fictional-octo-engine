import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MembershipTest {

    @Test
    @DisplayName("Medlemskapet är aktivt på startdatumet")
    void membershipIsActiveOnStartDate(){
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        Membership membership = new Membership(startDate, endDate);

        boolean active = membership.isActiveOn(startDate);

        assertTrue(active);
    }

    @Test
    @DisplayName("Medlemskapet är aktivt på slutdatumet")
    void membershipIsActiveOnEndDate(){
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        Membership membership = new Membership(startDate, endDate);

        boolean active = membership.isActiveOn(endDate);

        assertTrue(active);
    }
    @Test
    @DisplayName("Medlemskapet är inaktivt före startdatumet")
    void membershipIsInActiveBeforeStartDate(){
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        Membership membership = new Membership(startDate, endDate);
        LocalDate dayBeforeStart = LocalDate.of(2025,12,31);

        boolean active = membership.isActiveOn(dayBeforeStart);

        assertFalse(active);
    }
    @Test
    @DisplayName("Medlemskapet är inaktivt efter slutdatumet")
    void membershipIsInActiveAfterEndDate(){
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        Membership membership = new Membership(startDate, endDate);
        LocalDate dayAfterEnd = LocalDate.of(2027,1,1);

        boolean active = membership.isActiveOn(dayAfterEnd);

        assertFalse(active);
    }

        }