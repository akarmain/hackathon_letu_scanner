package com.example.hackathon_letu_scanner;

import android.content.res.ColorStateList;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class OperationActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE_RES_ID = "operation_title_res_id";
    public static final String EXTRA_RECEIVING_ID = "receiving_id";
    public static final String EXTRA_DELIVERY_NUMBER = "delivery_number";
    public static final String EXTRA_SUPPLIER = "supplier";
    public static final String EXTRA_DELIVERY_DATE = "delivery_date";

    private static final String STATE_ACCEPTED_BOXES = "accepted_boxes";
    private static final String STATE_FAIL_NEXT_SCAN = "fail_next_scan";
    private static final int TOTAL_BOXES = 5;
    private static final int INITIAL_ACCEPTED_BOXES = 3;
    private int acceptedBoxes = INITIAL_ACCEPTED_BOXES;
    private boolean failNextScan;

    private TextView progressCount;
    private TextView totalBoxes;
    private TextView boxesLeft;
    private ProgressBar boxesProgress;
    private LinearLayout boxesContainer;
    private MaterialButton scanButton;
    private TextView finishButton;
    private String receivingId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int titleResId = getIntent().getIntExtra(EXTRA_TITLE_RES_ID, R.string.action_receiving);
        setTitle(titleResId);
        configureBackButton();

        if (titleResId != R.string.action_receiving) {
            showPlaceholder(titleResId);
            return;
        }

        setContentView(R.layout.activity_operation);
        receivingId = getIntent().getStringExtra(EXTRA_RECEIVING_ID);
        if (savedInstanceState != null) {
            acceptedBoxes = savedInstanceState.getInt(STATE_ACCEPTED_BOXES, INITIAL_ACCEPTED_BOXES);
            failNextScan = savedInstanceState.getBoolean(STATE_FAIL_NEXT_SCAN, false);
        }

        bindViews();
        bindReceivingDetails();
        renderState();
        scanButton.setOnClickListener(view -> mockScan());
        finishButton.setOnClickListener(view -> finishReceiving());
    }

    private void configureBackButton() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showPlaceholder(int titleResId) {
        TextView placeholder = new TextView(this);
        placeholder.setGravity(Gravity.CENTER);
        placeholder.setPadding(dp(24), dp(24), dp(24), dp(24));
        placeholder.setText(getString(R.string.operation_placeholder, getString(titleResId)));
        placeholder.setTextColor(ContextCompat.getColor(this, R.color.warehouse_muted));
        placeholder.setTextSize(18);
        setContentView(placeholder);
    }

    private void bindViews() {
        progressCount = findViewById(R.id.progressCount);
        totalBoxes = findViewById(R.id.totalBoxes);
        boxesLeft = findViewById(R.id.boxesLeft);
        boxesProgress = findViewById(R.id.boxesProgress);
        boxesContainer = findViewById(R.id.boxesContainer);
        scanButton = findViewById(R.id.scanButton);
        finishButton = findViewById(R.id.finishButton);
    }

    private void bindReceivingDetails() {
        String deliveryNumber = getIntent().getStringExtra(EXTRA_DELIVERY_NUMBER);
        String supplier = getIntent().getStringExtra(EXTRA_SUPPLIER);
        String deliveryDate = getIntent().getStringExtra(EXTRA_DELIVERY_DATE);

        if (deliveryNumber != null) {
            ((TextView) findViewById(R.id.deliveryTitle)).setText(
                    getString(R.string.delivery_number, deliveryNumber));
        }
        if (supplier != null) {
            ((TextView) findViewById(R.id.deliverySupplier)).setText(supplier);
        }
        if (deliveryDate != null) {
            ((TextView) findViewById(R.id.deliveryDate)).setText(deliveryDate);
        }
    }

    private void mockScan() {
        if (acceptedBoxes >= TOTAL_BOXES) {
            return;
        }

        if (failNextScan) {
            failNextScan = false;
            showScanError();
            return;
        }

        acceptedBoxes++;
        failNextScan = acceptedBoxes < TOTAL_BOXES;
        renderState();
        Toast.makeText(this, getString(R.string.scan_success, acceptedBoxes), Toast.LENGTH_SHORT).show();
    }

    private void showScanError() {
        Snackbar snackbar = Snackbar.make(
                findViewById(android.R.id.content), R.string.scan_error, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.warehouse_error_background));
        snackbar.setTextColor(ContextCompat.getColor(this, R.color.warehouse_error));

        View snackbarView = snackbar.getView();
        if (snackbarView.getLayoutParams() instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
            params.gravity = Gravity.TOP;
            params.setMargins(dp(16), dp(12), dp(16), 0);
            snackbarView.setLayoutParams(params);
        }
        snackbar.show();
    }

    private void renderState() {
        int boxesRemaining = TOTAL_BOXES - acceptedBoxes;
        progressCount.setText(getString(R.string.boxes_progress, acceptedBoxes, TOTAL_BOXES));
        totalBoxes.setText(getString(R.string.boxes_total, TOTAL_BOXES));
        boxesLeft.setText(getString(R.string.boxes_left, boxesRemaining));
        boxesProgress.setProgress(acceptedBoxes, true);
        renderAcceptedBoxes();

        boolean complete = acceptedBoxes == TOTAL_BOXES;
        scanButton.setEnabled(!complete);
        scanButton.setText(complete ? R.string.all_boxes_scanned : R.string.scan_box);
        scanButton.setTextSize(complete ? 15 : 19);
        scanButton.setContentDescription(getString(complete ? R.string.all_boxes_scanned : R.string.scan_box));
        scanButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                this, complete ? R.color.warehouse_disabled : R.color.warehouse_blue)));
    }

    private void renderAcceptedBoxes() {
        boxesContainer.removeAllViews();
        for (int boxNumber = 1; boxNumber <= acceptedBoxes; boxNumber++) {
            boxesContainer.addView(createBoxView(boxNumber));
        }
    }

    private View createBoxView(int boxNumber) {
        BoxCatalog.BoxDetails boxDetails = BoxCatalog.findBox(boxNumber);
        if (boxDetails == null) {
            return new View(this);
        }

        LinearLayout box = verticalLayout();
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (boxNumber > 1) {
            boxParams.topMargin = dp(28);
        }
        box.setLayoutParams(boxParams);
        box.setClickable(true);
        box.setFocusable(true);
        box.setBackgroundResource(android.R.drawable.list_selector_background);
        box.setContentDescription(getString(R.string.open_box_description, boxNumber));
        box.setOnClickListener(view -> openBoxContents(boxNumber));

        box.addView(textView(getString(R.string.box_title, boxNumber), 22, R.color.warehouse_text, true));

        TextView code = textView(getString(R.string.box_code, boxDetails.getCode()), 15,
                R.color.warehouse_muted, false);
        LinearLayout.LayoutParams codeParams = matchWidthWrapHeight();
        codeParams.topMargin = dp(4);
        code.setLayoutParams(codeParams);
        box.addView(code);

        View divider = new View(this);
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.warehouse_divider));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        dividerParams.topMargin = dp(12);
        dividerParams.bottomMargin = dp(14);
        divider.setLayoutParams(dividerParams);
        box.addView(divider);

        box.addView(textView(getString(R.string.box_expected), 16, R.color.warehouse_text, true));
        box.addView(createDetailRow(
                getString(R.string.box_products, boxDetails.getProducts().size()),
                R.string.products_icon_description));
        box.addView(createDetailRow(
                getString(R.string.box_units, boxDetails.getTotalUnits()),
                R.string.units_icon_description));
        return box;
    }

    private View createDetailRow(String text, int descriptionResId) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = matchWidthWrapHeight();
        rowParams.topMargin = dp(8);
        row.setLayoutParams(rowParams);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_layers);
        icon.setContentDescription(getString(descriptionResId));
        row.addView(icon, new LinearLayout.LayoutParams(dp(30), dp(30)));

        TextView detail = textView(text, 15, R.color.warehouse_muted, false);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        detailParams.leftMargin = dp(10);
        detail.setLayoutParams(detailParams);
        row.addView(detail);
        return row;
    }

    private void openBoxContents(int boxNumber) {
        Intent intent = new Intent(this, BoxContentsActivity.class);
        intent.putExtra(BoxContentsActivity.EXTRA_BOX_NUMBER, boxNumber);
        startActivity(intent);
    }

    private void finishReceiving() {
        int missingBoxes = TOTAL_BOXES - acceptedBoxes;
        if (missingBoxes == 0) {
            showFinishedMessage(0);
            return;
        }

        LinearLayout dialogContent = verticalLayout();
        int horizontalPadding = dp(24);
        dialogContent.setPadding(horizontalPadding, 0, horizontalPadding, 0);

        TextView message = textView(getString(R.string.finish_dialog_message, missingBoxes, TOTAL_BOXES),
                16, R.color.warehouse_muted, false);
        dialogContent.addView(message);

        String warningText = missingBoxes == 1
                ? getString(R.string.finish_dialog_warning_one)
                : getString(R.string.finish_dialog_warning_many, missingBoxes);
        TextView warning = textView("⚠  " + warningText, 14, R.color.warehouse_warning, true);
        warning.setBackgroundResource(R.drawable.bg_warning_banner);
        warning.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams warningParams = matchWidthWrapHeight();
        warningParams.topMargin = dp(16);
        warning.setLayoutParams(warningParams);
        dialogContent.addView(warning);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.finish_dialog_title)
                .setView(dialogContent)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.finish, (dialog, which) -> showFinishedMessage(missingBoxes))
                .show();
    }

    private void showFinishedMessage(int missingBoxes) {
        String message = missingBoxes == 0
                ? getString(R.string.receiving_finished)
                : getString(R.string.receiving_finished_shortage, missingBoxes);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.receiving_finished)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> completeReceiving())
                .show();
    }

    private void completeReceiving() {
        if (receivingId != null) {
            ReceivingStatusStore.markCompleted(this, receivingId);
        }
        Intent result = new Intent();
        result.putExtra(EXTRA_RECEIVING_ID, receivingId);
        setResult(RESULT_OK, result);
        finish();
    }

    private LinearLayout verticalLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private TextView textView(String text, float sizeSp, int colorResId, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sizeSp);
        view.setTextColor(ContextCompat.getColor(this, colorResId));
        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    private LinearLayout.LayoutParams matchWidthWrapHeight() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_ACCEPTED_BOXES, acceptedBoxes);
        outState.putBoolean(STATE_FAIL_NEXT_SCAN, failNextScan);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
