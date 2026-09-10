package com.example.hackathon_letu_scanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hackathon_letu_scanner.placement.PlacementRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class PlacementResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement_result);
        setTitle(R.string.placement_result_title);
        configureBackButton();

        PlacementRepository repository = PlacementRepository.get();
        ((TextView) findViewById(R.id.placementResultQuantity)).setText(getString(
                R.string.placement_result_quantity,
                repository.getPlacedCount(),
                repository.getItems().size()
        ));
        ((TextView) findViewById(R.id.placementResultTime)).setText(getString(
                R.string.placement_result_time,
                new SimpleDateFormat("d MMMM, HH:mm", Locale.forLanguageTag("ru-RU"))
                        .format(new Date())
        ));
        findViewById(R.id.placementBackToOperationsButton).setOnClickListener(view -> backToMain());
    }

    private void configureBackButton() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void backToMain() {
        Intent intent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        backToMain();
        return true;
    }
}
