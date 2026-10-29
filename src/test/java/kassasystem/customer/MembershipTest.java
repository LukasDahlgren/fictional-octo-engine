package kassasystem.customer;

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
    @DisplayName("Membership Is Active On Start Date")
    void membershipIsActiveOnStartDate(){
        boolean active = membership.isActiveOn(startDate);

        assertTrue(active);
    }

    @Test
    @DisplayName("Membership Is Active On End Date")
    void membershipIsActiveOnEndDate(){
        boolean active = membership.isActiveOn(endDate);

        assertTrue(active);
    }
    @Test
    @DisplayName("Membership Is Inactive Before Start Date")
    void membershipIsInActiveBeforeStartDate(){
        LocalDate dayBeforeStart = LocalDate.of(2025,12,31);
        boolean active = membership.isActiveOn(dayBeforeStart);

        assertFalse(active);
    }
    @Test
    @DisplayName("Membership Is Inactive After End Date")
    void membershipIsInActiveAfterEndDate(){
        LocalDate dayAfterEnd = LocalDate.of(2027,1,1);
        boolean active = membership.isActiveOn(dayAfterEnd);

        assertFalse(active);
    }

        }
