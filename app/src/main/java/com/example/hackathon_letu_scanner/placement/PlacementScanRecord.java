package com.example.hackathon_letu_scanner.placement;

public final class PlacementScanRecord {
    private final long timestamp;
    private final String code;
    private final PlacementValidation.Kind result;

    public PlacementScanRecord(long timestamp, String code, PlacementValidation.Kind result) {
        this.timestamp = timestamp;
        this.code = code;
        this.result = result;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCode() {
        return code;
    }

    public PlacementValidation.Kind getResult() {
        return result;
    }
}
