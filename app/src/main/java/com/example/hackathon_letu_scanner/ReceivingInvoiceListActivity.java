package com.example.hackathon_letu_scanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathon_letu_scanner.receiving.ReceivingInvoice;
import com.example.hackathon_letu_scanner.receiving.ReceivingRepository;

public final class ReceivingInvoiceListActivity extends AppCompatActivity {
    private LinearLayout invoiceContainer;
    private ReceivingRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving_invoice_list);
        setTitle(R.string.receiving_title);
        configureBackButton();
        invoiceContainer = findViewById(R.id.invoiceContainer);
        repository = ReceivingRepository.get(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showInvoices();
    }

    private void showInvoices() {
        invoiceContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (ReceivingInvoice invoice : repository.getInvoices()) {
            View card = inflater.inflate(R.layout.item_receiving_invoice, invoiceContainer, false);
            TextView id = card.findViewById(R.id.invoiceId);
            TextView supplier = card.findViewById(R.id.invoiceSupplier);
            TextView progress = card.findViewById(R.id.invoiceProgress);
            TextView status = card.findViewById(R.id.invoiceStatus);

            int received = repository.getReceivedTotal(invoice.getId());
            id.setText(invoice.getId());
            supplier.setText(invoice.getSupplier());
            progress.setText(getString(
                    R.string.invoice_progress,
                    received,
                    invoice.getExpectedTotal(),
                    invoice.getDeliveryDate()
            ));
            if (repository.isCompleted(invoice.getId())) {
                status.setText(R.string.status_completed);
                status.setTextColor(getColor(R.color.warehouse_success));
            } else if (received > 0) {
                status.setText(R.string.status_in_progress);
                status.setTextColor(getColor(R.color.warehouse_warning));
            } else {
                status.setText(R.string.status_new);
                status.setTextColor(getColor(R.color.warehouse_blue_dark));
            }
            card.setOnClickListener(view -> openInvoice(invoice));
            invoiceContainer.addView(card);
        }
    }

    private void openInvoice(ReceivingInvoice invoice) {
        Class<?> target = repository.isCompleted(invoice.getId())
                ? ReceivingResultActivity.class
                : ReceivingTaskActivity.class;
        Intent intent = new Intent(this, target);
        intent.putExtra(ReceivingTaskActivity.EXTRA_INVOICE_ID, invoice.getId());
        startActivity(intent);
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
