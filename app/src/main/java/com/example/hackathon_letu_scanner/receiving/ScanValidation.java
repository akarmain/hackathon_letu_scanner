package com.example.hackathon_letu_scanner.receiving;

public final class ScanValidation {
    public enum Kind {
        MATCH,
        INVALID_FORMAT,
        NOT_FOUND,
        WRONG_INVOICE
    }

    private final Kind kind;
    private final ReceivingItem item;
    private final String relatedInvoiceId;

    private ScanValidation(Kind kind, ReceivingItem item, String relatedInvoiceId) {
        this.kind = kind;
        this.item = item;
        this.relatedInvoiceId = relatedInvoiceId;
    }

    public static ScanValidation match(ReceivingItem item) {
        return new ScanValidation(Kind.MATCH, item, null);
    }

    public static ScanValidation failure(Kind kind) {
        return new ScanValidation(kind, null, null);
    }

    public static ScanValidation wrongInvoice(String invoiceId) {
        return new ScanValidation(Kind.WRONG_INVOICE, null, invoiceId);
    }

    public Kind getKind() {
        return kind;
    }

    public ReceivingItem getItem() {
        return item;
    }

    public String getRelatedInvoiceId() {
        return relatedInvoiceId;
    }
}
