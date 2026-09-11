package com.example.hackathon_letu_scanner.receiving;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Pure receiving rules; it has no dependency on camera or Android UI. */
public final class ReceivingPolicy {

    public ScanValidation validateBarcode(
            ReceivingInvoice currentInvoice,
            List<ReceivingInvoice> allInvoices,
            Set<String> acceptedBarcodes,
            String rawBarcode
    ) {
        String barcode = rawBarcode == null ? "" : rawBarcode.trim();
        if (!isValidEan13(barcode)) {
            return ScanValidation.failure(ScanValidation.Kind.INVALID_FORMAT);
        }

        ReceivingItem currentItem = currentInvoice.findItemByBarcode(barcode);
        if (currentItem != null) {
            if (acceptedBarcodes.contains(barcode)) {
                return ScanValidation.alreadyReceived(currentItem, barcode);
            }
            return ScanValidation.match(currentItem, barcode);
        }

        for (ReceivingInvoice invoice : allInvoices) {
            if (!invoice.getId().equals(currentInvoice.getId())
                    && invoice.findItemByBarcode(barcode) != null) {
                return ScanValidation.wrongInvoice(invoice.getId());
            }
        }
        return ScanValidation.failure(ScanValidation.Kind.NOT_FOUND);
    }

    public boolean hasDiscrepancy(ReceivingInvoice invoice, Map<String, Integer> received) {
        for (ReceivingItem item : invoice.getItems()) {
            int actual = received.getOrDefault(item.getId(), 0);
            if (actual != item.getExpectedQuantity()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidEan13(String barcode) {
        if (barcode == null || !barcode.matches("\\d{13}")) {
            return false;
        }
        int sum = 0;
        for (int index = 0; index < 12; index++) {
            int digit = barcode.charAt(index) - '0';
            sum += index % 2 == 0 ? digit : digit * 3;
        }
        int expectedCheckDigit = (10 - sum % 10) % 10;
        return expectedCheckDigit == barcode.charAt(12) - '0';
    }
}
