package com.example.hackathon_letu_scanner;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.hackathon_letu_scanner.receiving.ReceivingInvoice;
import com.example.hackathon_letu_scanner.receiving.ReceivingRepository;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public final class ReceivingResultActivity extends AppCompatActivity {
    private ReceivingRepository repository;
    private ReceivingInvoice invoice;
    private ImageView photoPreview;
    private TextView photoStatus;
    private Uri pendingPhotoUri;

    private final ActivityResultLauncher<Uri> takePhoto =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), saved -> {
                if (saved && pendingPhotoUri != null) {
                    repository.saveInvoicePhoto(invoice.getId(), pendingPhotoUri.toString());
                    showPhoto(pendingPhotoUri);
                } else {
                    Toast.makeText(this, R.string.photo_not_saved, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving_result);
        setTitle(R.string.receiving_result_title);
        repository = ReceivingRepository.get(this);
        invoice = repository.findInvoice(getIntent().getStringExtra(ReceivingTaskActivity.EXTRA_INVOICE_ID));
        if (invoice == null) {
            finish();
            return;
        }

        photoPreview = findViewById(R.id.invoicePhotoPreview);
        photoStatus = findViewById(R.id.invoicePhotoStatus);
        renderResult();
        findViewById(R.id.takeInvoicePhotoButton).setOnClickListener(view -> launchCamera());
        findViewById(R.id.doneReceivingButton).setOnClickListener(view -> finish());
    }

    private void renderResult() {
        int received = repository.getReceivedTotal(invoice.getId());
        ((TextView) findViewById(R.id.resultInvoiceId)).setText(invoice.getId());
        ((TextView) findViewById(R.id.resultQuantity)).setText(getString(
                R.string.result_quantity,
                received,
                invoice.getExpectedTotal()
        ));
        long completedAt = repository.getCompletedAt(invoice.getId());
        String date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(new Date(completedAt));
        ((TextView) findViewById(R.id.resultTime)).setText(getString(R.string.result_time, date));

        String reason = repository.getDiscrepancyReason(invoice.getId());
        TextView discrepancy = findViewById(R.id.resultDiscrepancy);
        if (reason == null || reason.isEmpty()) {
            discrepancy.setText(R.string.result_no_discrepancy);
            discrepancy.setTextColor(getColor(R.color.warehouse_success));
        } else {
            discrepancy.setText(getString(R.string.result_discrepancy, reason));
            discrepancy.setTextColor(getColor(R.color.warehouse_warning));
        }

        String savedPhoto = repository.getInvoicePhoto(invoice.getId());
        if (savedPhoto != null && !savedPhoto.isEmpty()) {
            showPhoto(Uri.parse(savedPhoto));
        }
    }

    private void launchCamera() {
        File directory = new File(getCacheDir(), "receiving");
        if (!directory.exists() && !directory.mkdirs()) {
            Toast.makeText(this, R.string.photo_storage_error, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File photo = File.createTempFile("invoice_" + invoice.getId() + "_", ".jpg", directory);
            pendingPhotoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photo
            );
            takePhoto.launch(pendingPhotoUri);
        } catch (IOException exception) {
            Toast.makeText(this, R.string.photo_storage_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void showPhoto(Uri photoUri) {
        photoPreview.setImageURI(photoUri);
        photoPreview.setVisibility(View.VISIBLE);
        photoStatus.setText(R.string.photo_saved);
        photoStatus.setTextColor(getColor(R.color.warehouse_success));
    }
}
