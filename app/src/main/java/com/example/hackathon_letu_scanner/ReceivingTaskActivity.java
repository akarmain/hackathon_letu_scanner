package com.example.hackathon_letu_scanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathon_letu_scanner.receiving.BoxContentItem;
import com.example.hackathon_letu_scanner.receiving.ReceivingInvoice;
import com.example.hackathon_letu_scanner.receiving.ReceivingItem;
import com.example.hackathon_letu_scanner.receiving.ReceivingPolicy;
import com.example.hackathon_letu_scanner.receiving.ReceivingRepository;
import com.example.hackathon_letu_scanner.receiving.ScanValidation;
import com.example.warehouse.scanner.BarcodeScannerContract;
import com.example.warehouse.scanner.ScannerRequest;

import java.util.Map;

public final class ReceivingTaskActivity extends AppCompatActivity {
    public static final String EXTRA_INVOICE_ID = "invoice_id";

    private ReceivingRepository repository;
    private ReceivingPolicy policy;
    private ReceivingInvoice invoice;
    private LinearLayout itemContainer;
    private ProgressBar progressBar;
    private TextView progressText;
    private TextView stateMessage;

    private final ActivityResultLauncher<ScannerRequest> scannerLauncher =
            registerForActivityResult(new BarcodeScannerContract(), this::handleBarcode);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving_task);
        setTitle(R.string.receiving_task_title);
        configureBackButton();

        repository = ReceivingRepository.get(this);
        policy = new ReceivingPolicy();
        invoice = repository.findInvoice(getIntent().getStringExtra(EXTRA_INVOICE_ID));
        if (invoice == null) {
            finish();
            return;
        }
        if (repository.isCompleted(invoice.getId())) {
            openResult();
            return;
        }

        bindViews();
        findViewById(R.id.scanBoxButton).setOnClickListener(view -> {
            hideStateMessage();
            scannerLauncher.launch(ScannerRequest.ean13());
        });
        findViewById(R.id.finishReceivingButton).setOnClickListener(view -> finishReceiving());
        renderTask();
    }

    private void bindViews() {
        itemContainer = findViewById(R.id.receivingItemContainer);
        progressBar = findViewById(R.id.receivingProgress);
        progressText = findViewById(R.id.receivingProgressText);
        stateMessage = findViewById(R.id.receivingStateMessage);
        ((TextView) findViewById(R.id.taskInvoiceId)).setText(invoice.getId());
        ((TextView) findViewById(R.id.taskSupplier)).setText(invoice.getSupplier());
        ((TextView) findViewById(R.id.taskDeliveryTime)).setText(invoice.getDeliveryDate());
    }

    private void renderTask() {
        itemContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        int totalReceived = 0;
        for (ReceivingItem item : invoice.getItems()) {
            int received = repository.getReceived(invoice.getId(), item.getId());
            totalReceived += received;
            View row = inflater.inflate(R.layout.item_receiving_line, itemContainer, false);
            ((TextView) row.findViewById(R.id.lineName)).setText(item.getName());
            ((TextView) row.findViewById(R.id.lineSku)).setText(
                    getString(R.string.box_barcode, item.getBarcode())
            );
            ((TextView) row.findViewById(R.id.lineContents)).setText(formatContents(item));
            TextView count = row.findViewById(R.id.lineCount);
            count.setText(getString(R.string.item_count, received, item.getExpectedQuantity()));
            if (received == item.getExpectedQuantity()) {
                count.setTextColor(getColor(R.color.warehouse_success));
            } else if (received > item.getExpectedQuantity()) {
                count.setTextColor(getColor(R.color.warehouse_error));
            }
            itemContainer.addView(row);
        }
        int expected = invoice.getExpectedTotal();
        progressText.setText(getString(R.string.receiving_total_progress, totalReceived, expected));
        progressBar.setMax(Math.max(expected, totalReceived));
        progressBar.setProgress(totalReceived);
    }

    private void handleBarcode(@Nullable String barcode) {
        if (barcode == null) {
            showStateMessage(R.string.scan_cancelled, false);
            return;
        }
        ScanValidation validation = policy.validateBarcode(
                invoice,
                repository.getInvoices(),
                barcode
        );
        repository.recordScan(invoice.getId(), barcode, validation.getKind());
        switch (validation.getKind()) {
            case MATCH:
                showBoxConfirmation(validation.getItem());
                break;
            case INVALID_FORMAT:
                showStateMessage(R.string.invalid_barcode, true);
                break;
            case WRONG_INVOICE:
                showStateMessage(
                        getString(R.string.wrong_invoice, validation.getRelatedInvoiceId()),
                        true
                );
                break;
            case NOT_FOUND:
            default:
                showStateMessage(R.string.product_not_found, true);
                break;
        }
    }

    private void showBoxConfirmation(ReceivingItem item) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.dialog_padding);
        content.setPadding(padding, 0, padding, 0);

        TextView details = new TextView(this);
        int alreadyReceived = repository.getReceived(invoice.getId(), item.getId());
        details.setText(getString(
                R.string.confirm_scan_details,
                item.getName(),
                item.getBarcode(),
                alreadyReceived,
                item.getExpectedQuantity(),
                formatContents(item)
        ));
        details.setTextSize(17);
        details.setTextColor(getColor(R.color.warehouse_text));
        content.addView(details);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_scan_title)
                .setView(content)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_accept_box, (ignored, which) -> confirmBox(item))
                .create();
        dialog.show();
    }

    private void confirmBox(ReceivingItem item) {
        try {
            repository.confirmBox(invoice.getId(), item.getId(), policy);
        } catch (ArithmeticException exception) {
            showStateMessage(R.string.quantity_too_large, true);
            return;
        }
        renderTask();
        showStateMessage(getString(R.string.box_confirmed, item.getName()), false);
    }

    private String formatContents(ReceivingItem item) {
        StringBuilder result = new StringBuilder();
        for (BoxContentItem contentItem : item.getContents()) {
            if (result.length() > 0) {
                result.append('\n');
            }
            result.append("• ")
                    .append(contentItem.getName())
                    .append(" — ")
                    .append(contentItem.getQuantity())
                    .append(" шт. (")
                    .append(contentItem.getSku())
                    .append(')');
        }
        return result.toString();
    }

    private void finishReceiving() {
        if (repository.getReceivedTotal(invoice.getId()) == 0) {
            showStateMessage(R.string.nothing_received, true);
            return;
        }
        Map<String, Integer> received = repository.getReceived(invoice.getId());
        if (policy.hasDiscrepancy(invoice, received)) {
            chooseDiscrepancyReason();
        } else {
            completeReceiving("");
        }
    }

    private void chooseDiscrepancyReason() {
        View content = getLayoutInflater().inflate(R.layout.dialog_discrepancy_reason, null);
        RadioGroup reasons = content.findViewById(R.id.discrepancyReasonGroup);
        RadioButton otherReason = content.findViewById(R.id.reasonOther);
        EditText customReason = content.findViewById(R.id.customReasonInput);
        TextView selectionError = content.findViewById(R.id.reasonSelectionError);

        reasons.setOnCheckedChangeListener((group, checkedId) -> {
            customReason.setVisibility(checkedId == R.id.reasonOther ? View.VISIBLE : View.GONE);
            selectionError.setVisibility(View.GONE);
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.discrepancy_title)
                .setView(content)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_finish_receiving, null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    int selectedId = reasons.getCheckedRadioButtonId();
                    if (selectedId == -1) {
                        selectionError.setVisibility(View.VISIBLE);
                        return;
                    }
                    String reason;
                    if (selectedId == otherReason.getId()) {
                        reason = customReason.getText().toString().trim();
                        if (reason.isEmpty()) {
                            customReason.setError(getString(R.string.custom_reason_required));
                            return;
                        }
                    } else {
                        RadioButton selected = content.findViewById(selectedId);
                        reason = selected.getText().toString();
                    }
                    dialog.dismiss();
                    completeReceiving(reason);
                }));
        dialog.show();
    }

    private void completeReceiving(String discrepancyReason) {
        repository.complete(invoice.getId(), discrepancyReason);
        openResult();
    }

    private void openResult() {
        Intent intent = new Intent(this, ReceivingResultActivity.class);
        intent.putExtra(EXTRA_INVOICE_ID, invoice.getId());
        startActivity(intent);
        finish();
    }

    private void showStateMessage(int messageRes, boolean error) {
        showStateMessage(getString(messageRes), error);
    }

    private void showStateMessage(String message, boolean error) {
        stateMessage.setText(message);
        stateMessage.setTextColor(getColor(error ? R.color.warehouse_error : R.color.warehouse_success));
        stateMessage.setBackgroundResource(
                error ? R.drawable.bg_state_error : R.drawable.bg_state_success
        );
        stateMessage.setVisibility(View.VISIBLE);
    }

    private void hideStateMessage() {
        stateMessage.setVisibility(View.GONE);
    }

    private void configureBackButton() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
