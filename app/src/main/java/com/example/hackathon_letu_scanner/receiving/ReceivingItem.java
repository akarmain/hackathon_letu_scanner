package com.example.hackathon_letu_scanner.receiving;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReceivingItem {
    private final String id;
    private final String name;
    private final List<String> boxBarcodes;
    private final List<BoxContentItem> contents;

    public ReceivingItem(
            String id,
            String name,
            List<String> boxBarcodes,
            List<BoxContentItem> contents
    ) {
        this.id = id;
        this.name = name;
        this.boxBarcodes = Collections.unmodifiableList(new ArrayList<>(boxBarcodes));
        this.contents = Collections.unmodifiableList(new ArrayList<>(contents));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getBoxBarcodes() {
        return boxBarcodes;
    }

    public int getExpectedQuantity() {
        return boxBarcodes.size();
    }

    public List<BoxContentItem> getContents() {
        return contents;
    }

    public boolean matchesBarcode(String barcode) {
        return barcode != null && boxBarcodes.contains(barcode.trim());
    }
}
