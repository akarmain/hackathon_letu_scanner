package com.example.hackathon_letu_scanner.receiving;

public final class ScanRecord {
    private final long timestamp;
    private final String barcode;
    private final ScanValidation.Kind result;

    public ScanRecord(long timestamp, String barcode, ScanValidation.Kind result) {
        this.timestamp = timestamp;
        this.barcode = barcode;
        this.result = result;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getBarcode() {
        return barcode;
    }

    public ScanValidation.Kind getResult() {
        return result;
    }
}
