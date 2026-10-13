import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecieptTest {

    @Test
    @DisplayName("Kvittot sparar köpets tid från den angivna klockan och tidszonen")
    void constructorCapturesTimestampInClockTimeZone() {
        Basket basket = new Basket();
        Clock clock = Clock.fixed(
                Instant.parse("2026-01-15T10:30:00Z"),
                ZoneId.of("Europe/Stockholm"));
        LocalDateTime expectedTimestamp = LocalDateTime.of(2026, 1, 15, 11, 30);

        Receipt receipt = new Receipt(basket, clock);

        assertEquals(expectedTimestamp, receipt.getTimestamp());
    }

    @Test
    @DisplayName("Konstruktorn avvisar en saknad korg")
    void constructorRejectsNullBasket() {
        Basket basket = null;
        Clock clock = Clock.systemUTC();

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, clock));
    }

    @Test
    @DisplayName("Konstruktorn avvisar en saknad klocka")
    void constructorRejectsNullClock() {
        Basket basket = new Basket();
        Clock clock = null;

        assertThrows(IllegalArgumentException.class,
                () -> new Receipt(basket, clock));
    }
}
