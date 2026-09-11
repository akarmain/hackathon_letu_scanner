package com.example.hackathon_letu_scanner.placement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class PlacementRepository {
    private static PlacementRepository instance;

    private final List<PlacementItem> items;
    private final List<PlacementScanRecord> scanRecords = new ArrayList<>();
    private int placedCount;

    private PlacementRepository() {
        items = Collections.unmodifiableList(Arrays.asList(
                new PlacementItem(
                        1,
                        "PERF-001",
                        "NOIRÉ Eau de Parfum 50 мл",
                        "Парфюмерная вода с древесно-цитрусовым ароматом. Флакон 50 мл.",
                        "Парфюмерия",
                        5490.0,
                        "barcode",
                        72.0,
                        115.0,
                        45.0,
                        "https://s3.akarmain.ru/S/NOIRÉ Eau de Parfum 50.jpg",
                        "representative",
                        Collections.singletonList("4609001000055"),
                        4,
                        1,
                        "A3-2"
                ),
                new PlacementItem(
                        2,
                        "MAKE-001",
                        "VELVET LINE Матовая помада 4,2 г",
                        "Стойкая матовая помада с насыщенным цветом. Масса 4,2 г.",
                        "Декоративная косметика",
                        1190.0,
                        "barcode",
                        28.0,
                        92.0,
                        28.0,
                        "https://s3.akarmain.ru/S/VELVET_LINE.jpg",
                        "representative",
                        Collections.singletonList("4609001000062"),
                        3,
                        1,
                        "A3-3"
                ),
                new PlacementItem(
                        3,
                        "MAKE-002",
                        "LUMIÈRE Volume Тушь для ресниц 10 мл",
                        "Тушь для придания объёма и разделения ресниц. Объём 10 мл.",
                        "Декоративная косметика",
                        1390.0,
                        "barcode",
                        24.0,
                        135.0,
                        24.0,
                        "https://s3.akarmain.ru/S/LUMIÈRE_T_Volume.jpg",
                        "representative",
                        Collections.singletonList("4609001000079"),
                        3,
                        1,
                        "A3-4"
                )
        ));
    }

    public static synchronized PlacementRepository get() {
        if (instance == null) {
            instance = new PlacementRepository();
        }
        return instance;
    }

    public List<PlacementItem> getItems() {
        return items;
    }

    public int getPlacedCount() {
        return placedCount;
    }

    public PlacementItem getCurrentItem() {
        return isCompleted() ? null : items.get(placedCount);
    }

    public boolean isCompleted() {
        return placedCount >= items.size();
    }

    public void confirmCurrentItem(int itemId) {
        PlacementItem current = getCurrentItem();
        if (current == null || current.getId() != itemId) {
            throw new IllegalStateException("Placement item is no longer current");
        }
        placedCount++;
    }

    public void recordScan(String code, PlacementValidation.Kind result) {
        scanRecords.add(new PlacementScanRecord(System.currentTimeMillis(), code, result));
    }

    public List<PlacementScanRecord> getScanRecords() {
        return Collections.unmodifiableList(scanRecords);
    }

    public void reset() {
        placedCount = 0;
        scanRecords.clear();
    }
}
