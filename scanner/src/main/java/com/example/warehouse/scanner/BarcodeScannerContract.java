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
public final class BarcodeScannerContract extends ActivityResultContract<Void, String> {

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Void input) {
        return new Intent(context, ScannerActivity.class);
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
