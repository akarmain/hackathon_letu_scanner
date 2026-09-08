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

import com.google.android.material.card.MaterialCardView;

public class ReceivingListActivity extends AppCompatActivity {

    private static final Receiving[] RECEIVINGS = {
            new Receiving("45872", "ООО «Продукт Сервис»", "Сегодня, 09:30", "Основной склад"),
            new Receiving("45891", "АО «Северная линия»", "Сегодня, 12:15", "Основной склад"),
            new Receiving("45903", "ООО «ТоргПоставка»", "Завтра, 10:00", "Склад № 2")
    };

    private LinearLayout receivingsContainer;
    private TextView receivingsSummary;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.receivings_title);
        setContentView(R.layout.activity_receiving_list);
        configureBackButton();

        receivingsContainer = findViewById(R.id.receivingsContainer);
        receivingsSummary = findViewById(R.id.receivingsSummary);
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderReceivings();
    }

    private void configureBackButton() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void renderReceivings() {
        receivingsContainer.removeAllViews();
        int completedCount = 0;
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Receiving receiving : RECEIVINGS) {
            boolean completed = ReceivingStatusStore.isCompleted(this, receiving.id);
            if (completed) {
                completedCount++;
            }
            View item = inflater.inflate(R.layout.item_receiving, receivingsContainer, false);
            bindItem(item, receiving, completed);
            receivingsContainer.addView(item);
        }

        receivingsSummary.setText(getString(
                R.string.receivings_summary, RECEIVINGS.length - completedCount, completedCount));
    }

    private void bindItem(View item, Receiving receiving, boolean completed) {
        ((TextView) item.findViewById(R.id.receivingNumber)).setText(
                getString(R.string.delivery_number, receiving.id));
        ((TextView) item.findViewById(R.id.receivingSupplier)).setText(receiving.supplier);
        ((TextView) item.findViewById(R.id.receivingDate)).setText(receiving.date);
        ((TextView) item.findViewById(R.id.receivingWarehouse)).setText(receiving.warehouse);

        TextView status = item.findViewById(R.id.receivingStatus);
        MaterialCardView card = (MaterialCardView) item;
        if (completed) {
            status.setText(R.string.receiving_status_completed);
            status.setTextColor(getColor(R.color.warehouse_success));
            status.setBackgroundResource(R.drawable.bg_status_completed);
            card.setContentDescription(getString(
                    R.string.completed_receiving_description, receiving.id));
            card.setAlpha(0.78f);
            card.setClickable(false);
        } else {
            status.setText(R.string.receiving_status_pending);
            status.setTextColor(getColor(R.color.warehouse_blue_dark));
            status.setBackgroundResource(R.drawable.bg_status_pending);
            card.setContentDescription(getString(
                    R.string.open_receiving_description, receiving.id));
            card.setOnClickListener(view -> openReceiving(receiving));
        }
    }

    private void openReceiving(Receiving receiving) {
        Intent intent = new Intent(this, OperationActivity.class);
        intent.putExtra(OperationActivity.EXTRA_TITLE_RES_ID, R.string.action_receiving);
        intent.putExtra(OperationActivity.EXTRA_RECEIVING_ID, receiving.id);
        intent.putExtra(OperationActivity.EXTRA_DELIVERY_NUMBER, receiving.id);
        intent.putExtra(OperationActivity.EXTRA_SUPPLIER, receiving.supplier);
        intent.putExtra(OperationActivity.EXTRA_DELIVERY_DATE, receiving.date);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private static final class Receiving {
        final String id;
        final String supplier;
        final String date;
        final String warehouse;

        Receiving(String id, String supplier, String date, String warehouse) {
            this.id = id;
            this.supplier = supplier;
            this.date = date;
            this.warehouse = warehouse;
        }
    }
}
