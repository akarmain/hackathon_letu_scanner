package com.example.hackathon_letu_scanner.receiving;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Local demo source isolated from the operation UI and scanner. */
public final class ReceivingRepository {
    private static final String PREFERENCES = "receiving_progress_v2";
    private static ReceivingRepository instance;

    private final SharedPreferences preferences;
    private final List<ReceivingInvoice> invoices;

    private ReceivingRepository(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        invoices = createDemoInvoices();
    }

    public static synchronized ReceivingRepository get(Context context) {
        if (instance == null) {
            instance = new ReceivingRepository(context);
        }
        return instance;
    }

    public List<ReceivingInvoice> getInvoices() {
        return Collections.unmodifiableList(invoices);
    }

    public ReceivingInvoice findInvoice(String invoiceId) {
        for (ReceivingInvoice invoice : invoices) {
            if (invoice.getId().equals(invoiceId)) {
                return invoice;
            }
        }
        return null;
    }

    public int getReceived(String invoiceId, String itemId) {
        return preferences.getInt(quantityKey(invoiceId, itemId), 0);
    }

    public Map<String, Integer> getReceived(String invoiceId) {
        ReceivingInvoice invoice = findInvoice(invoiceId);
        Map<String, Integer> quantities = new LinkedHashMap<>();
        if (invoice != null) {
            for (ReceivingItem item : invoice.getItems()) {
                quantities.put(item.getId(), getReceived(invoiceId, item.getId()));
            }
        }
        return quantities;
    }

    public int getReceivedTotal(String invoiceId) {
        int total = 0;
        for (int value : getReceived(invoiceId).values()) {
            total += value;
        }
        return total;
    }

    public synchronized int confirmBox(
            String invoiceId,
            String itemId,
            ReceivingPolicy policy
    ) {
        int updated = policy.confirmOneBox(getReceived(invoiceId, itemId));
        preferences.edit().putInt(quantityKey(invoiceId, itemId), updated).apply();
        return updated;
    }

    public synchronized void recordScan(
            String invoiceId,
            String barcode,
            ScanValidation.Kind result
    ) {
        String existing = preferences.getString(scanLogKey(invoiceId), "");
        String entry = System.currentTimeMillis() + "," + barcode + "," + result.name();
        preferences.edit()
                .putString(scanLogKey(invoiceId), existing.isEmpty() ? entry : existing + ";" + entry)
                .apply();
    }

    public List<ScanRecord> getScanLog(String invoiceId) {
        String encoded = preferences.getString(scanLogKey(invoiceId), "");
        List<ScanRecord> records = new ArrayList<>();
        if (encoded == null || encoded.isEmpty()) {
            return records;
        }
        for (String entry : encoded.split(";")) {
            String[] fields = entry.split(",", 3);
            if (fields.length != 3) {
                continue;
            }
            try {
                records.add(new ScanRecord(
                        Long.parseLong(fields[0]),
                        fields[1],
                        ScanValidation.Kind.valueOf(fields[2])
                ));
            } catch (IllegalArgumentException ignored) {
                // Ignore one malformed local record and keep the rest of the operation usable.
            }
        }
        return records;
    }

    public void complete(String invoiceId, String discrepancyReason) {
        preferences.edit()
                .putBoolean(completedKey(invoiceId), true)
                .putLong(completedAtKey(invoiceId), System.currentTimeMillis())
                .putString(reasonKey(invoiceId), discrepancyReason == null ? "" : discrepancyReason)
                .apply();
    }

    public boolean isCompleted(String invoiceId) {
        return preferences.getBoolean(completedKey(invoiceId), false);
    }

    public long getCompletedAt(String invoiceId) {
        return preferences.getLong(completedAtKey(invoiceId), 0L);
    }

    public String getDiscrepancyReason(String invoiceId) {
        return preferences.getString(reasonKey(invoiceId), "");
    }

    public void saveInvoicePhoto(String invoiceId, String photoUri) {
        preferences.edit().putString(photoKey(invoiceId), photoUri).apply();
    }

    public String getInvoicePhoto(String invoiceId) {
        return preferences.getString(photoKey(invoiceId), "");
    }

    private static String quantityKey(String invoiceId, String itemId) {
        return "quantity." + invoiceId + "." + itemId;
    }

    private static String completedKey(String invoiceId) {
        return "completed." + invoiceId;
    }

    private static String completedAtKey(String invoiceId) {
        return "completed_at." + invoiceId;
    }

    private static String reasonKey(String invoiceId) {
        return "reason." + invoiceId;
    }

    private static String photoKey(String invoiceId) {
        return "photo." + invoiceId;
    }

    private static String scanLogKey(String invoiceId) {
        return "scan_log." + invoiceId;
    }

    private static List<ReceivingInvoice> createDemoInvoices() {
        ReceivingInvoice first = new ReceivingInvoice(
                "ПН-240910-01",
                "ООО «Бьюти Логистика»",
                "10 сентября, 16:30",
                Arrays.asList(
                        new ReceivingItem(
                                "sauvage",
                                "Коробка BL-01",
                                "4601234567893",
                                5,
                                Arrays.asList(
                                        new BoxContentItem(
                                                "Dior Sauvage, туалетная вода 100 мл",
                                                "АРТ. 104582",
                                                6
                                        ),
                                        new BoxContentItem(
                                                "Dior Sauvage, гель для душа 250 мл",
                                                "АРТ. 104611",
                                                4
                                        )
                                )
                        ),
                        new ReceivingItem(
                                "lancome",
                                "Коробка BL-02",
                                "4601111111119",
                                3,
                                Arrays.asList(
                                        new BoxContentItem(
                                                "Lancôme Hypnôse, тушь для ресниц",
                                                "АРТ. 207314",
                                                12
                                        ),
                                        new BoxContentItem(
                                                "Lancôme Le Crayon Khôl, карандаш",
                                                "АРТ. 207328",
                                                8
                                        )
                                )
                        )
                )
        );
        ReceivingInvoice second = new ReceivingInvoice(
                "ПН-240910-02",
                "АО «Парфюм Сервис»",
                "10 сентября, 18:00",
                Arrays.asList(
                        new ReceivingItem(
                                "clarins",
                                "Коробка PS-01",
                                "4602222222220",
                                4,
                                Arrays.asList(
                                        new BoxContentItem(
                                                "Clarins Lip Comfort Oil, масло для губ",
                                                "АРТ. 318790",
                                                10
                                        ),
                                        new BoxContentItem(
                                                "Clarins Joli Rouge, помада",
                                                "АРТ. 318804",
                                                6
                                        )
                                )
                        ),
                        new ReceivingItem(
                                "boss",
                                "Коробка PS-02",
                                "4603333333331",
                                2,
                                Arrays.asList(
                                        new BoxContentItem(
                                                "Hugo Boss Bottled, туалетная вода 50 мл",
                                                "АРТ. 441205",
                                                8
                                        ),
                                        new BoxContentItem(
                                                "Hugo Boss Bottled, дезодорант",
                                                "АРТ. 441219",
                                                8
                                        )
                                )
                        )
                )
        );
        return Arrays.asList(first, second);
    }
}
