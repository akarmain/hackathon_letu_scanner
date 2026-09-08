package com.example.hackathon_letu_scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class BoxCatalogTest {

    @Test
    public void firstBoxContainsExpectedProductSummary() {
        BoxCatalog.BoxDetails box = BoxCatalog.findBox(1);

        assertNotNull(box);
        assertEquals("04601234567893", box.getCode());
        assertEquals(6, box.getProducts().size());
        assertEquals(24, box.getTotalUnits());
        assertEquals("https://s3.akarmain.ru/S/cat.jpg",
                box.getProducts().get(0).getImageUrl());
    }
}
