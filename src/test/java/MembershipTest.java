import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MembershipTest {

    private LocalDate startDate;
    private LocalDate endDate;
    private Membership membership;

    @BeforeEach
    void setUp(){
        startDate = LocalDate.of(2026, 1, 1);
        endDate = LocalDate.of(2026, 12, 31);
        membership = new Membership(startDate, endDate);
    }

    @Test
    @DisplayName("Medlemskapet är aktivt på startdatumet")
    void membershipIsActiveOnStartDate(){
        boolean active = membership.isActiveOn(startDate);

        assertTrue(active);
    }

    @Test
    @DisplayName("Medlemskapet är aktivt på slutdatumet")
    void membershipIsActiveOnEndDate(){
        boolean active = membership.isActiveOn(endDate);

        assertTrue(active);
    }
    @Test
    @DisplayName("Medlemskapet är inaktivt före startdatumet")
    void membershipIsInActiveBeforeStartDate(){
        LocalDate dayBeforeStart = LocalDate.of(2025,12,31);
        boolean active = membership.isActiveOn(dayBeforeStart);

        assertFalse(active);
    }
    @Test
    @DisplayName("Medlemskapet är inaktivt efter slutdatumet")
    void membershipIsInActiveAfterEndDate(){
        LocalDate dayAfterEnd = LocalDate.of(2027,1,1);
        boolean active = membership.isActiveOn(dayAfterEnd);

        assertFalse(active);
    }

        }