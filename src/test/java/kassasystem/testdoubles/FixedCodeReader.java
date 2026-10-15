package kassasystem.testdoubles;

import kassasystem.scanning.BarcodeScanner;

public class FixedCodeReader implements BarcodeScanner.CodeReader {
    private final String code;
    private int readCalls;

    public FixedCodeReader(String code) {
        this.code = code;
    }

    @Override
    public String readCode() {
        readCalls++;
        return code;
    }

    public int getReadCalls() {
        return readCalls;
    }
}
