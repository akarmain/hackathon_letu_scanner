package com.example.hackathon_letu_scanner.receiving;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReceivingItem {
    private final String id;
    private final String name;
    private final String barcode;
    private final int expectedQuantity;
    private final List<BoxContentItem> contents;

    public ReceivingItem(
            String id,
            String name,
            String barcode,
            int expectedQuantity,
            List<BoxContentItem> contents
    ) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.expectedQuantity = expectedQuantity;
        this.contents = Collections.unmodifiableList(new ArrayList<>(contents));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBarcode() {
        return barcode;
    }

    public int getExpectedQuantity() {
        return expectedQuantity;
    }

    public List<BoxContentItem> getContents() {
        return contents;
    }
}
