package com.example.hackathon_letu_scanner;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathon_letu_scanner.placement.PlacementItem;
import com.example.hackathon_letu_scanner.placement.PlacementPolicy;
import com.example.hackathon_letu_scanner.placement.PlacementRepository;
import com.example.hackathon_letu_scanner.placement.PlacementValidation;
import com.example.hackathon_letu_scanner.placement.ProductImageLoader;
import com.example.warehouse.scanner.BarcodeScannerContract;
import com.example.warehouse.scanner.ScannerRequest;

import java.util.List;

public final class PlacementActivity extends AppCompatActivity {
    private enum Stage {
        WAITING_PRODUCT,
        WAITING_LOCATION,
        READY_TO_CONFIRM
    }

    private PlacementRepository repository;
    private final PlacementPolicy policy = new PlacementPolicy();
    private Stage stage = Stage.WAITING_PRODUCT;

    private ProgressBar progressBar;
    private TextView progressText;
    private TextView positionCounter;
    private TextView currentName;
    private TextView currentCode;
    private TextView currentQuantity;
    private TextView currentLocation;
    private TextView stateMessage;
    private ImageView currentImage;
    private LinearLayout nextItemsContainer;
    private TextView nextItemsHeading;
    private Button primaryButton;

    private final ActivityResultLauncher<ScannerRequest> scannerLauncher =
            registerForActivityResult(new BarcodeScannerContract(), this::handleScannedCode);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement);
        repository = PlacementRepository.get();

        findViewById(R.id.placementBackButton).setOnClickListener(view -> finish());
        bindViews();
        primaryButton.setOnClickListener(view -> performPrimaryAction());

        if (repository.isCompleted()) {
            openResult();
            return;
        }
        renderTask();
    }

    private void bindViews() {
        progressBar = findViewById(R.id.placementProgress);
        progressText = findViewById(R.id.placementProgressText);
        positionCounter = findViewById(R.id.placementPositionCounter);
        currentName = findViewById(R.id.placementCurrentName);
        currentCode = findViewById(R.id.placementCurrentCode);
        currentQuantity = findViewById(R.id.placementCurrentQuantity);
        currentLocation = findViewById(R.id.placementCurrentLocation);
        stateMessage = findViewById(R.id.placementStateMessage);
        currentImage = findViewById(R.id.placementCurrentImage);
        nextItemsContainer = findViewById(R.id.placementNextItemsContainer);
        nextItemsHeading = findViewById(R.id.placementNextItemsHeading);
        primaryButton = findViewById(R.id.placementPrimaryButton);
    }

    private void renderTask() {
        List<PlacementItem> items = repository.getItems();
        int placed = repository.getPlacedCount();
        PlacementItem current = repository.getCurrentItem();
        if (current == null) {
            openResult();
            return;
        }

        progressText.setText(getString(R.string.placement_progress_value, placed, items.size()));
        progressBar.setMax(items.size());
        progressBar.setProgress(placed);
        positionCounter.setText(getString(R.string.placement_position_counter, placed + 1, items.size()));
        currentName.setText(current.getName());
        currentCode.setText(formatCode(current));
        setQuantityText(currentQuantity, current);
        currentLocation.setText(getString(R.string.placement_location_value, current.getLocation()));
        ProductImageLoader.load(
                currentImage,
                current.getImageUrl(),
                getString(R.string.product_image_description, current.getName())
        );

        nextItemsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int index = placed + 1; index < items.size(); index++) {
            PlacementItem item = items.get(index);
            View row = inflater.inflate(
                    R.layout.item_placement_next,
                    nextItemsContainer,
                    false
            );
            ((TextView) row.findViewById(R.id.placementNextName)).setText(item.getName());
            ((TextView) row.findViewById(R.id.placementNextCode)).setText(formatCode(item));
            setQuantityText(row.findViewById(R.id.placementNextQuantity), item);
            ProductImageLoader.load(
                    row.findViewById(R.id.placementNextImage),
                    item.getImageUrl(),
                    getString(R.string.product_image_description, item.getName())
            );
            nextItemsContainer.addView(row);
        }
        nextItemsHeading.setVisibility(
                nextItemsContainer.getChildCount() == 0 ? View.GONE : View.VISIBLE
        );

        stage = Stage.WAITING_PRODUCT;
        hideStateMessage();
        updatePrimaryButton();
    }

    private String formatCode(PlacementItem item) {
        return getString(
                R.string.placement_ean_summary,
                item.getSku(),
                item.getCodes().get(0)
        );
    }

    private void setQuantityText(TextView target, PlacementItem item) {
        String units = getResources().getQuantityString(
                R.plurals.placement_units,
                item.getQuantity(),
                item.getQuantity()
        );
        String boxes = getResources().getQuantityString(
                R.plurals.placement_boxes,
                item.getBoxes(),
                item.getBoxes()
        );
        String prefix = getString(R.string.placement_quantity_prefix);
        SpannableString value = new SpannableString(
                getString(R.string.placement_quantity, prefix, units, boxes)
        );
        int valueStart = prefix.length();
        value.setSpan(
                new ForegroundColorSpan(getColor(R.color.warehouse_blue)),
                valueStart,
                value.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        value.setSpan(
                new StyleSpan(Typeface.BOLD),
                valueStart,
                value.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        target.setText(value);
    }

    private void performPrimaryAction() {
        hideStateMessage();
        if (stage == Stage.WAITING_PRODUCT) {
            scannerLauncher.launch(ScannerRequest.productCode());
        } else if (stage == Stage.WAITING_LOCATION) {
            scannerLauncher.launch(ScannerRequest.locationCode());
        } else {
            showConfirmation();
        }
    }

    private void handleScannedCode(@Nullable String code) {
        if (code == null) {
            showInfo(getString(R.string.placement_scan_cancelled));
            return;
        }
        PlacementItem current = repository.getCurrentItem();
        if (current == null) {
            openResult();
            return;
        }

        if (stage == Stage.WAITING_PRODUCT) {
            PlacementValidation validation = policy.validateProduct(
                    current,
                    repository.getItems(),
                    code
            );
            repository.recordScan(code, validation.getKind());
            if (validation.getKind() == PlacementValidation.Kind.MATCH) {
                stage = Stage.WAITING_LOCATION;
                showSuccess(getString(R.string.placement_product_scanned, current.getName()));
                updatePrimaryButton();
            } else if (validation.getKind() == PlacementValidation.Kind.WRONG_PRODUCT) {
                showError(getString(
                        R.string.placement_wrong_product,
                        validation.getRelatedItem().getName()
                ));
            } else {
                showError(getString(R.string.placement_product_not_found));
            }
            return;
        }

        PlacementValidation validation = policy.validateLocation(current, code);
        repository.recordScan(code, validation.getKind());
        if (validation.getKind() == PlacementValidation.Kind.MATCH) {
            stage = Stage.READY_TO_CONFIRM;
            showSuccess(getString(R.string.placement_location_scanned, current.getLocation()));
            updatePrimaryButton();
        } else {
            showError(getString(
                    R.string.placement_wrong_location,
                    code,
                    current.getLocation()
            ));
        }
    }

    private void showConfirmation() {
        PlacementItem current = repository.getCurrentItem();
        if (current == null) {
            openResult();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.placement_confirm_title)
                .setMessage(getString(
                        R.string.placement_confirm_message,
                        current.getName(),
                        current.getQuantity(),
                        current.getLocation()
                ))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(
                        R.string.placement_confirm_action,
                        (dialog, which) -> confirmPlacement(current)
                )
                .show();
    }

    private void confirmPlacement(PlacementItem item) {
        repository.confirmCurrentItem(item.getId());
        if (repository.isCompleted()) {
            openResult();
            return;
        }
        renderTask();
        showSuccess(getString(R.string.placement_item_confirmed, item.getName()));
    }

    private void updatePrimaryButton() {
        int textRes;
        int iconRes;
        if (stage == Stage.WAITING_PRODUCT) {
            textRes = R.string.placement_scan_product;
            iconRes = R.drawable.ic_scan_white;
        } else if (stage == Stage.WAITING_LOCATION) {
            textRes = R.string.placement_scan_location;
            iconRes = R.drawable.ic_scan_white;
        } else {
            textRes = R.string.placement_confirm_action;
            iconRes = R.drawable.ic_check_white;
        }
        primaryButton.setText(textRes);
        primaryButton.setCompoundDrawablesRelativeWithIntrinsicBounds(iconRes, 0, 0, 0);
    }

    private void showSuccess(String message) {
        showStateMessage(
                message,
                R.color.warehouse_success,
                R.drawable.bg_state_success
        );
    }

    private void showError(String message) {
        showStateMessage(
                message,
                R.color.warehouse_error,
                R.drawable.bg_state_error
        );
    }

    private void showInfo(String message) {
        showStateMessage(
                message,
                R.color.warehouse_blue_dark,
                R.drawable.bg_state_info
        );
    }

    private void showStateMessage(String message, int colorRes, int backgroundRes) {
        stateMessage.setText(message);
        stateMessage.setTextColor(getColor(colorRes));
        stateMessage.setBackgroundResource(backgroundRes);
        stateMessage.setVisibility(View.VISIBLE);
    }

    private void hideStateMessage() {
        stateMessage.setVisibility(View.GONE);
    }

    private void openResult() {
        startActivity(new Intent(this, PlacementResultActivity.class));
        finish();
    }
}
