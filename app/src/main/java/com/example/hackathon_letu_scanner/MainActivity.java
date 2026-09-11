package com.example.hackathon_letu_scanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathon_letu_scanner.placement.PlacementRepository;
import com.example.hackathon_letu_scanner.receiving.ReceivingRepository;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.receivingButton).setOnClickListener(view ->
                startActivity(new Intent(this, ReceivingInvoiceListActivity.class))
        );
        findViewById(R.id.placementButton).setOnClickListener(view ->
                startActivity(new Intent(this, PlacementActivity.class))
        );
        findViewById(R.id.resetButton).setOnClickListener(view -> confirmReset());
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reset_title)
                .setMessage(R.string.reset_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_reset, (dialog, which) -> resetDemoData())
                .show();
    }

    private void resetDemoData() {
        ReceivingRepository.get(this).reset();
        PlacementRepository.get().reset();
        Toast.makeText(this, R.string.reset_completed, Toast.LENGTH_SHORT).show();
    }
}
