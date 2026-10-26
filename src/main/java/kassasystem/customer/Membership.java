package kassasystem.customer;

import java.time.LocalDate;

public class Membership {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public Membership(LocalDate startDate, LocalDate endDate) {

        this.startDate = startDate;
        this.endDate = endDate;

    }

    public boolean isActiveOn(LocalDate date) {
        boolean hasStarted = !date.isBefore(startDate);
        boolean hasNotEnded = !date.isAfter(endDate);

        return hasStarted && hasNotEnded;
    }
}
