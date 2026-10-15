package kassasystem.scanning;

public class EANGenerator {

    private int nextNumber = 0;

    public EAN generate() {
       if (nextNumber > 99999) {
           throw new IllegalStateException("No more codes are available");
       }

       String firstTwelveDigits = String.format("2000000%05d", nextNumber);
       nextNumber++;

       int checkDigit = EAN.calculateCheckDigit(firstTwelveDigits);
       String code = firstTwelveDigits + checkDigit;

       return new EAN(code);

    }
}
