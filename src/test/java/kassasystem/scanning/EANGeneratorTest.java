package kassasystem.scanning;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class EANGeneratorTest {

    @Test
    @DisplayName("Generates a valid EAN-13 code")
    public void generatesValidEAN13() {
        EANGenerator generator = new EANGenerator();

        EAN generatedEAN = generator.generate();

        EAN validatedEAN = new EAN(generatedEAN.getCode());

        assertEquals(generatedEAN.getCode(), validatedEAN.getCode());
        assertEquals(13, generatedEAN.getCode().length());
    }

    @Test
    @DisplayName("Generates a different code each time")
    public void generatesDifferentCodes() {
        EANGenerator generator = new EANGenerator();

        EAN firstEAN = generator.generate();
        EAN secondEAN = generator.generate();

        assertNotEquals(firstEAN.getCode(), secondEAN.getCode());
    }
}
