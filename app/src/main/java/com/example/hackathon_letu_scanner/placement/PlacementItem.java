package com.example.hackathon_letu_scanner.placement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PlacementItem {
    private final int id;
    private final String sku;
    private final String name;
    private final String description;
    private final String category;
    private final double priceRub;
    private final String markingType;
    private final double widthMm;
    private final double heightMm;
    private final double depthMm;
    private final String imageUrl;
    private final String imageType;
    private final List<String> codes;
    private final int quantity;
    private final int boxes;
    private final String location;

    public PlacementItem(
            int id,
            String sku,
            String name,
            String description,
            String category,
            double priceRub,
            String markingType,
            double widthMm,
            double heightMm,
            double depthMm,
            String imageUrl,
            String imageType,
            List<String> codes,
            int quantity,
            int boxes,
            String location
    ) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.category = category;
        this.priceRub = priceRub;
        this.markingType = markingType;
        this.widthMm = widthMm;
        this.heightMm = heightMm;
        this.depthMm = depthMm;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.codes = Collections.unmodifiableList(new ArrayList<>(codes));
        this.quantity = quantity;
        this.boxes = boxes;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public double getPriceRub() {
        return priceRub;
    }

    public String getMarkingType() {
        return markingType;
    }

    public double getWidthMm() {
        return widthMm;
    }

    public double getHeightMm() {
        return heightMm;
    }

    public double getDepthMm() {
        return depthMm;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageType() {
        return imageType;
    }

    public List<String> getCodes() {
        return codes;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getBoxes() {
        return boxes;
    }

    public String getLocation() {
        return location;
    }

    public boolean matchesCode(String code) {
        if (code == null) {
            return false;
        }
        String normalized = code.trim();
        for (String itemCode : codes) {
            if (itemCode.equalsIgnoreCase(normalized)) {
                return true;
            }
        }
        return false;
    }
}
