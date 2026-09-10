package com.example.hackathon_letu_scanner.receiving;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReceivingPolicyTest {
    private ReceivingPolicy policy;
    private ReceivingInvoice currentInvoice;
    private ReceivingInvoice otherInvoice;

    @Before
    public void setUp() {
        policy = new ReceivingPolicy();
        currentInvoice = invoice("ПН-01", "item-1", "4601234567893", 5);
        otherInvoice = invoice("ПН-02", "item-2", "4602222222220", 2);
    }

    @Test
    public void matchingEanCanBeConfirmed() {
        ScanValidation result = policy.validateBarcode(
                currentInvoice,
                Arrays.asList(currentInvoice, otherInvoice),
                "4601234567893"
        );

        assertEquals(ScanValidation.Kind.MATCH, result.getKind());
        assertEquals("item-1", result.getItem().getId());
    }

    @Test
    public void codeFromAnotherInvoiceIsRejected() {
        ScanValidation result = policy.validateBarcode(
                currentInvoice,
                Arrays.asList(currentInvoice, otherInvoice),
                "4602222222220"
        );

        assertEquals(ScanValidation.Kind.WRONG_INVOICE, result.getKind());
        assertEquals("ПН-02", result.getRelatedInvoiceId());
    }

    @Test
    public void malformedOrBadChecksumIsRejected() {
        assertEquals(
                ScanValidation.Kind.INVALID_FORMAT,
                policy.validateBarcode(currentInvoice, Collections.singletonList(currentInvoice), "123")
                        .getKind()
        );
        assertFalse(ReceivingPolicy.isValidEan13("4601234567890"));
    }

    @Test
    public void discrepancyChecksEveryLine() {
        Map<String, Integer> received = new HashMap<>();
        received.put("item-1", 5);
        assertFalse(policy.hasDiscrepancy(currentInvoice, received));

        received.put("item-1", 4);
        assertTrue(policy.hasDiscrepancy(currentInvoice, received));
    }

    @Test
    public void confirmationCountsExactlyOnePhysicalBox() {
        assertEquals(3, policy.confirmOneBox(2));
    }

    private ReceivingInvoice invoice(
            String invoiceId,
            String itemId,
            String barcode,
            int expected
    ) {
        ReceivingItem item = new ReceivingItem(
                itemId,
                "Коробка",
                barcode,
                expected,
                Collections.singletonList(new BoxContentItem("Товар", "АРТ. 1", 6))
        );
        return new ReceivingInvoice(
                invoiceId,
                "Поставщик",
                "Сегодня",
                Collections.singletonList(item)
        );
    }
}
