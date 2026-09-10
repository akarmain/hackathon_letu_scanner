package com.example.warehouse.scanner;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Public scanner boundary used by operation screens. The implementation behind this
 * contract can be replaced without changing receiving business rules.
 */
public final class BarcodeScannerContract extends ActivityResultContract<ScannerRequest, String> {

    static final String EXTRA_SCAN_MODE = "warehouse.scanner.SCAN_MODE";

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, ScannerRequest input) {
        ScannerRequest request = input == null ? ScannerRequest.ean13() : input;
        return new Intent(context, ScannerActivity.class)
                .putExtra(EXTRA_SCAN_MODE, request.getMode());
    }

    @Nullable
    @Override
    public String parseResult(int resultCode, @Nullable Intent intent) {
        if (resultCode != ScannerActivity.RESULT_OK || intent == null) {
            return null;
        }
        return intent.getStringExtra(ScannerActivity.EXTRA_BARCODE);
    }
}
