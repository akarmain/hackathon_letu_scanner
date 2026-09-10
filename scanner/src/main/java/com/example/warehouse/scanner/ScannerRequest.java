package com.example.warehouse.scanner;

/**
 * Describes what an operation screen expects to scan without exposing the scanner
 * implementation to business code.
 */
public final class ScannerRequest {
    static final int MODE_EAN_13 = 1;
    static final int MODE_PRODUCT = 2;
    static final int MODE_LOCATION = 3;

    private final int mode;

    private ScannerRequest(int mode) {
        this.mode = mode;
    }

    public static ScannerRequest ean13() {
        return new ScannerRequest(MODE_EAN_13);
    }

    public static ScannerRequest productCode() {
        return new ScannerRequest(MODE_PRODUCT);
    }

    public static ScannerRequest locationCode() {
        return new ScannerRequest(MODE_LOCATION);
    }

    int getMode() {
        return mode;
    }
}
