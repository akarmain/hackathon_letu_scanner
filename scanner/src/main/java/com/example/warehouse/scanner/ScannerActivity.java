package com.example.warehouse.scanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

public final class ScannerActivity extends AppCompatActivity {

    static final String EXTRA_BARCODE = "warehouse.scanner.BARCODE";

    private PreviewView previewView;
    private View permissionPanel;
    private TextView statusText;
    private ScannerEngine scannerEngine;
    private boolean scannerStarted;
    private boolean returningResult;
    private int scanMode;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    showScanner();
                } else {
                    showPermissionError();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        scanMode = getIntent().getIntExtra(
                BarcodeScannerContract.EXTRA_SCAN_MODE,
                ScannerRequest.MODE_EAN_13
        );
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        previewView = findViewById(R.id.scannerPreview);
        permissionPanel = findViewById(R.id.permissionPanel);
        statusText = findViewById(R.id.scannerStatus);
        Button retryButton = findViewById(R.id.retryPermissionButton);
        Button settingsButton = findViewById(R.id.openSettingsButton);
        Button cancelButton = findViewById(R.id.cancelScanButton);
        Button manualEntryButton = findViewById(R.id.manualEntryButton);

        retryButton.setOnClickListener(view -> permissionLauncher.launch(Manifest.permission.CAMERA));
        settingsButton.setOnClickListener(view -> openApplicationSettings());
        cancelButton.setOnClickListener(view -> finish());
        manualEntryButton.setOnClickListener(view -> showManualEntryDialog());

        if (hasCameraPermission()) {
            showScanner();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!scannerStarted && hasCameraPermission()) {
            showScanner();
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void showScanner() {
        permissionPanel.setVisibility(View.GONE);
        previewView.setVisibility(View.VISIBLE);
        statusText.setText(getScannerHint());
        if (scannerStarted) {
            return;
        }
        scannerStarted = true;
        scannerEngine = new MlKitScannerEngine(this, previewView, scanMode);
        scannerEngine.start(new ScannerEngine.Callback() {
            @Override
            public void onBarcode(String value) {
                runOnUiThread(() -> returnBarcode(value));
            }

            @Override
            public void onFailure() {
                runOnUiThread(() -> statusText.setText(R.string.scanner_read_error));
            }
        });
    }

    private void showPermissionError() {
        stopScanner();
        previewView.setVisibility(View.GONE);
        permissionPanel.setVisibility(View.VISIBLE);
    }

    private void openApplicationSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null)
        );
        startActivity(intent);
    }

    private void returnBarcode(String barcode) {
        returningResult = true;
        Intent result = new Intent().putExtra(EXTRA_BARCODE, barcode);
        setResult(RESULT_OK, result);
        finish();
    }

    private void showManualEntryDialog() {
        stopScanner();
        EditText barcodeInput = new EditText(this);
        boolean eanOnly = scanMode != ScannerRequest.MODE_LOCATION;
        barcodeInput.setInputType(eanOnly
                ? InputType.TYPE_CLASS_NUMBER
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        barcodeInput.setHint(getManualHint());
        barcodeInput.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(eanOnly ? 13 : 64)
        });
        int padding = Math.round(24 * getResources().getDisplayMetrics().density);
        barcodeInput.setPadding(padding, padding / 2, padding, padding / 2);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.scanner_manual_title)
                .setMessage(getManualMessage())
                .setView(barcodeInput)
                .setNegativeButton(R.string.scanner_cancel, null)
                .setPositiveButton(R.string.scanner_manual_submit, null)
                .create();
        dialog.setOnShowListener(ignored -> {
            barcodeInput.requestFocus();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String barcode = barcodeInput.getText().toString().trim();
                if ((eanOnly && !barcode.matches("\\d{13}"))
                        || (!eanOnly && barcode.isEmpty())) {
                    barcodeInput.setError(getString(
                            eanOnly
                                    ? R.string.scanner_manual_error
                                    : R.string.scanner_manual_empty_error
                    ));
                    return;
                }
                returningResult = true;
                dialog.dismiss();
                returnBarcode(barcode);
            });
        });
        dialog.setOnDismissListener(ignored -> {
            if (!returningResult && hasCameraPermission()) {
                showScanner();
            }
        });
        dialog.show();
    }

    private int getScannerHint() {
        if (scanMode == ScannerRequest.MODE_PRODUCT) {
            return R.string.scanner_product_hint;
        }
        if (scanMode == ScannerRequest.MODE_LOCATION) {
            return R.string.scanner_location_hint;
        }
        return R.string.scanner_hint;
    }

    private int getManualHint() {
        if (scanMode == ScannerRequest.MODE_LOCATION) {
            return R.string.scanner_location_manual_hint;
        }
        if (scanMode == ScannerRequest.MODE_PRODUCT) {
            return R.string.scanner_product_manual_hint;
        }
        return R.string.scanner_manual_hint;
    }

    private int getManualMessage() {
        if (scanMode == ScannerRequest.MODE_LOCATION) {
            return R.string.scanner_location_manual_message;
        }
        if (scanMode == ScannerRequest.MODE_PRODUCT) {
            return R.string.scanner_product_manual_message;
        }
        return R.string.scanner_manual_message;
    }

    private void stopScanner() {
        if (scannerEngine != null) {
            scannerEngine.stop();
            scannerEngine = null;
        }
        scannerStarted = false;
    }

    @Override
    protected void onDestroy() {
        stopScanner();
        super.onDestroy();
    }
}
