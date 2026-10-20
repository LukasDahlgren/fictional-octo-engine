package kassasystem.scanning;

public class EAN {

    private final String code;

    public EAN(String code) {
        if(code == null) {
            throw new IllegalArgumentException("EAN code must not be null");
        }

        if(code.length() != 13) {
            throw new IllegalArgumentException("EAN code is " + code.length() + ", must be 13 digits");
        }

        for (int i = 0; i < code.length(); i++) {
            char character = code.charAt(i);

            if (character < '0' || character > '9') {
                throw new IllegalArgumentException("EAN code must only contain ASCII digits");
            }
        }

        String firstTwelveDigits = code.substring(0, 12);
        int expectedCheckDigit = calculateCheckDigit(firstTwelveDigits);
        int actualCheckDigit = code.charAt(12) - '0';

        if (actualCheckDigit != expectedCheckDigit) {
            throw new IllegalArgumentException("EAN code has an invalid check digit");
        }

        this.code = code;
    }

    public String getCode() {
        return code;
    }

    static int calculateCheckDigit(String firstTwelveDigits) {
        int sum = 0;

        for (int i = 0; i <firstTwelveDigits.length(); i++) {
            int digit = firstTwelveDigits.charAt(i) - '0';

            if (i % 2 == 0) {
                sum = sum + digit;
            } else {
                sum = sum + digit * 3;
            }
        }

        return (10 - sum % 10) % 10;
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(Object other) {
        if(this == other) {
            return true;
        }

        if(!(other instanceof EAN otherEAN)) {
            return false;
        }

        return code.equals(otherEAN.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
