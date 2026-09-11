package com.example.hackathon_letu_scanner.receiving;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReceivingInvoice {
    private final String id;
    private final String supplier;
    private final String deliveryDate;
    private final List<ReceivingItem> items;

    public ReceivingInvoice(
            String id,
            String supplier,
            String deliveryDate,
            List<ReceivingItem> items
    ) {
        this.id = id;
        this.supplier = supplier;
        this.deliveryDate = deliveryDate;
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
    }

    public String getId() {
        return id;
    }

    public String getSupplier() {
        return supplier;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public List<ReceivingItem> getItems() {
        return items;
    }

    public ReceivingItem findItemByBarcode(String barcode) {
        for (ReceivingItem item : items) {
            if (item.matchesBarcode(barcode)) {
                return item;
            }
        }
        return null;
    }

    public int getExpectedTotal() {
        int total = 0;
        for (ReceivingItem item : items) {
            total += item.getExpectedQuantity();
        }
        return total;
    }
}
