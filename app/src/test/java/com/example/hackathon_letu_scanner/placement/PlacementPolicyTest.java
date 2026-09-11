package com.example.hackathon_letu_scanner.placement;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PlacementPolicyTest {
    private PlacementPolicy policy;
    private List<PlacementItem> items;

    @Before
    public void setUp() {
        policy = new PlacementPolicy();
        PlacementRepository.get().reset();
        items = PlacementRepository.get().getItems();
    }

    @Test
    public void expectedEanCodeMatchesCurrentProduct() {
        PlacementValidation result = policy.validateProduct(
                items.get(0),
                items,
                "4609001000055"
        );

        assertEquals(PlacementValidation.Kind.MATCH, result.getKind());
    }

    @Test
    public void knownCodeFromAnotherProductIsRejectedAsWrongProduct() {
        PlacementValidation result = policy.validateProduct(
                items.get(0),
                items,
                "4609001000062"
        );

        assertEquals(PlacementValidation.Kind.WRONG_PRODUCT, result.getKind());
        assertEquals("MAKE-001", result.getRelatedItem().getSku());
    }

    @Test
    public void unknownCodeIsRejectedWithoutChangingTask() {
        PlacementValidation result = policy.validateProduct(
                items.get(0),
                items,
                "4609001999999"
        );

        assertEquals(PlacementValidation.Kind.NOT_FOUND, result.getKind());
        assertEquals(0, PlacementRepository.get().getPlacedCount());
    }

    @Test
    public void locationComparisonIgnoresCaseAndOuterWhitespace() {
        PlacementValidation result = policy.validateLocation(items.get(0), "  a3-2 ");

        assertEquals(PlacementValidation.Kind.MATCH, result.getKind());
    }

    @Test
    public void differentLocationIsRejected() {
        PlacementValidation result = policy.validateLocation(items.get(0), "A3-3");

        assertEquals(PlacementValidation.Kind.WRONG_LOCATION, result.getKind());
    }

    @Test
    public void resetRestoresPlacementTask() {
        PlacementRepository repository = PlacementRepository.get();
        repository.confirmCurrentItem(items.get(0).getId());

        repository.reset();

        assertEquals(0, repository.getPlacedCount());
        assertEquals(0, repository.getScanRecords().size());
    }

    @Test
    public void everyPlacementProductUsesValidEan13() {
        assertEquals(3, items.size());
        for (PlacementItem item : items) {
            assertEquals("barcode", item.getMarkingType());
            assertEquals(1, item.getCodes().size());
            assertTrue(isValidEan13(item.getCodes().get(0)));
        }
    }

    private boolean isValidEan13(String value) {
        if (value == null || !value.matches("\\d{13}")) {
            return false;
        }
        int sum = 0;
        for (int index = 0; index < 12; index++) {
            int digit = value.charAt(index) - '0';
            sum += index % 2 == 0 ? digit : digit * 3;
        }
        int expectedCheckDigit = (10 - sum % 10) % 10;
        return expectedCheckDigit == value.charAt(12) - '0';
    }
}
