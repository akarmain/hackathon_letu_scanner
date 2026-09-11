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
    private String currentBarcode;
    private String otherBarcode;

    @Before
    public void setUp() {
        policy = new ReceivingPolicy();
        currentBarcode = "4609100100014";
        otherBarcode = "4609100300018";
        currentInvoice = invoice(
                "ПН-01",
                "item-1",
                Arrays.asList(
                        currentBarcode,
                        "4609100100021"
                )
        );
        otherInvoice = invoice(
                "ПН-02",
                "item-2",
                Arrays.asList(otherBarcode, "4609100300025", "4609100300032")
        );
    }

    @Test
    public void matchingEanCanBeConfirmed() {
        ScanValidation result = policy.validateBarcode(
                currentInvoice,
                Arrays.asList(currentInvoice, otherInvoice),
                Collections.emptySet(),
                currentBarcode
        );

        assertEquals(ScanValidation.Kind.MATCH, result.getKind());
        assertEquals("item-1", result.getItem().getId());
    }

    @Test
    public void codeFromAnotherInvoiceIsRejected() {
        ScanValidation result = policy.validateBarcode(
                currentInvoice,
                Arrays.asList(currentInvoice, otherInvoice),
                Collections.emptySet(),
                otherBarcode
        );

        assertEquals(ScanValidation.Kind.WRONG_INVOICE, result.getKind());
        assertEquals("ПН-02", result.getRelatedInvoiceId());
    }

    @Test
    public void malformedOrBadChecksumIsRejected() {
        assertEquals(
                ScanValidation.Kind.INVALID_FORMAT,
                policy.validateBarcode(
                        currentInvoice,
                        Collections.singletonList(currentInvoice),
                        Collections.emptySet(),
                        "123"
                )
                        .getKind()
        );
        assertFalse(ReceivingPolicy.isValidEan13("4601234567890"));
    }

    @Test
    public void discrepancyChecksEveryLine() {
        Map<String, Integer> received = new HashMap<>();
        received.put("item-1", 2);
        assertFalse(policy.hasDiscrepancy(currentInvoice, received));

        received.put("item-1", 1);
        assertTrue(policy.hasDiscrepancy(currentInvoice, received));
    }

    @Test
    public void alreadyAcceptedPhysicalBoxIsRejected() {
        ScanValidation result = policy.validateBarcode(
                currentInvoice,
                Arrays.asList(currentInvoice, otherInvoice),
                Collections.singleton(currentBarcode),
                currentBarcode
        );

        assertEquals(ScanValidation.Kind.ALREADY_RECEIVED, result.getKind());
        assertEquals(currentBarcode, result.getBarcode());
    }

    private ReceivingInvoice invoice(
            String invoiceId,
            String itemId,
            java.util.List<String> barcodes
    ) {
        ReceivingItem item = new ReceivingItem(
                itemId,
                "Коробка",
                barcodes,
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
