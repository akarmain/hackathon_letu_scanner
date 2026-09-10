package com.example.hackathon_letu_scanner.placement;

public final class PlacementValidation {
    public enum Kind {
        MATCH,
        WRONG_PRODUCT,
        NOT_FOUND,
        WRONG_LOCATION
    }

    private final Kind kind;
    private final PlacementItem relatedItem;

    private PlacementValidation(Kind kind, PlacementItem relatedItem) {
        this.kind = kind;
        this.relatedItem = relatedItem;
    }

    public static PlacementValidation match() {
        return new PlacementValidation(Kind.MATCH, null);
    }

    public static PlacementValidation wrongProduct(PlacementItem item) {
        return new PlacementValidation(Kind.WRONG_PRODUCT, item);
    }

    public static PlacementValidation notFound() {
        return new PlacementValidation(Kind.NOT_FOUND, null);
    }

    public static PlacementValidation wrongLocation() {
        return new PlacementValidation(Kind.WRONG_LOCATION, null);
    }

    public Kind getKind() {
        return kind;
    }

    public PlacementItem getRelatedItem() {
        return relatedItem;
    }
}
