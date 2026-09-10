package com.example.hackathon_letu_scanner.receiving;

public final class BoxContentItem {
    private final String name;
    private final String sku;
    private final int quantity;

    public BoxContentItem(String name, String sku, int quantity) {
        this.name = name;
        this.sku = sku;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }
}
