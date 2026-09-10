package com.example.hackathon_letu_scanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindOperationButton(R.id.receivingButton, R.string.action_receiving);
        bindOperationButton(R.id.inventoryButton, R.string.action_inventory);
        bindOperationButton(R.id.placementButton, R.string.action_placement);
        bindOperationButton(R.id.replenishmentButton, R.string.action_replenishment);
    }

    private void bindOperationButton(int buttonId, int titleResId) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(view -> openOperation(titleResId));
    }

    private void openOperation(int titleResId) {
        Intent intent = new Intent(this, OperationActivity.class);
        intent.putExtra(OperationActivity.EXTRA_TITLE_RES_ID, titleResId);
        startActivity(intent);
    }
}
