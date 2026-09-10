package com.example.hackathon_letu_scanner;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class OperationActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE_RES_ID = "operation_title_res_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int titleResId = getIntent().getIntExtra(EXTRA_TITLE_RES_ID, R.string.app_name);
        setTitle(titleResId);
        configureBackButton();
        showPlaceholder(titleResId);
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
