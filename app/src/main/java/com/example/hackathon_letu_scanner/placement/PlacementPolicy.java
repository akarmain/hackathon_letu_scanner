package com.example.hackathon_letu_scanner.placement;

import java.util.List;

/** Business rules for placement; deliberately independent from camera or TSD APIs. */
public final class PlacementPolicy {

    public PlacementValidation validateProduct(
            PlacementItem expected,
            List<PlacementItem> taskItems,
            String code
    ) {
        if (expected.matchesCode(code)) {
            return PlacementValidation.match();
        }
        for (PlacementItem item : taskItems) {
            if (item.matchesCode(code)) {
                return PlacementValidation.wrongProduct(item);
            }
        }
        return PlacementValidation.notFound();
    }

    public PlacementValidation validateLocation(PlacementItem expected, String code) {
        if (code != null && expected.getLocation().equalsIgnoreCase(code.trim())) {
            return PlacementValidation.match();
        }
        return PlacementValidation.wrongLocation();
    }
}
