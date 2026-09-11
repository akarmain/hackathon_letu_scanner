package com.example.hackathon_letu_scanner.receiving;

public final class ScanValidation {
    public enum Kind {
        MATCH,
        ALREADY_RECEIVED,
        INVALID_FORMAT,
        NOT_FOUND,
        WRONG_INVOICE
    }

    private final Kind kind;
    private final ReceivingItem item;
    private final String barcode;
    private final String relatedInvoiceId;

    private ScanValidation(Kind kind, ReceivingItem item, String barcode, String relatedInvoiceId) {
        this.kind = kind;
        this.item = item;
        this.barcode = barcode;
        this.relatedInvoiceId = relatedInvoiceId;
    }

    public static ScanValidation match(ReceivingItem item, String barcode) {
        return new ScanValidation(Kind.MATCH, item, barcode, null);
    }

    public static ScanValidation alreadyReceived(ReceivingItem item, String barcode) {
        return new ScanValidation(Kind.ALREADY_RECEIVED, item, barcode, null);
    }

    public static ScanValidation failure(Kind kind) {
        return new ScanValidation(kind, null, null, null);
    }

    public static ScanValidation wrongInvoice(String invoiceId) {
        return new ScanValidation(Kind.WRONG_INVOICE, null, null, invoiceId);
    }

    public Kind getKind() {
        return kind;
    }

    public ReceivingItem getItem() {
        return item;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getRelatedInvoiceId() {
        return relatedInvoiceId;
    }
}
