import java.time.LocalDate;

public class Membership {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public Membership(LocalDate startDate, LocalDate endDate) {

        this.startDate = startDate;
        this.endDate = endDate;

    }

    public boolean isActiveOn(LocalDate date) {
        if (date.isBefore(startDate)) {
            return false;
        }

        if (date.isAfter(endDate)) {
            return false;
        }

        return true;
    }
}
