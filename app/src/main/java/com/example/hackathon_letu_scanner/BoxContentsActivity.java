package com.example.hackathon_letu_scanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class BoxContentsActivity extends AppCompatActivity {

    public static final String EXTRA_BOX_NUMBER = "box_number";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int requestedBoxNumber = getIntent().getIntExtra(EXTRA_BOX_NUMBER, 1);
        BoxCatalog.BoxDetails box = BoxCatalog.findBox(requestedBoxNumber);
        if (box == null) {
            finish();
            return;
        }

        setTitle(getString(R.string.box_title, box.getNumber()));
        setContentView(R.layout.activity_box_contents);
        configureBackButton();
        bindBox(box);
    }

    private void configureBackButton() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void bindBox(BoxCatalog.BoxDetails box) {
        ((TextView) findViewById(R.id.boxCode)).setText(
                getString(R.string.box_code_full, box.getCode()));
        ((TextView) findViewById(R.id.boxContentsSummary)).setText(getString(
                R.string.box_contents_summary, box.getProducts().size(), box.getTotalUnits()));

        LinearLayout productsContainer = findViewById(R.id.productsContainer);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (BoxCatalog.Product product : box.getProducts()) {
            View item = inflater.inflate(R.layout.item_box_product, productsContainer, false);
            bindProduct(item, product);
            productsContainer.addView(item);
        }

        MaterialButton scanBoxButton = findViewById(R.id.scanBoxButton);
        scanBoxButton.setOnClickListener(view -> Toast.makeText(
                this,
                getString(R.string.box_already_scanned, box.getNumber()),
                Toast.LENGTH_SHORT
        ).show());
    }

    private void bindProduct(View item, BoxCatalog.Product product) {
        ((TextView) item.findViewById(R.id.productCategory)).setText(product.getCategory());
        ((TextView) item.findViewById(R.id.productName)).setText(product.getName());
        ((TextView) item.findViewById(R.id.productPackage)).setText(product.getPackageDescription());
        ((TextView) item.findViewById(R.id.productQuantity)).setText(
                getString(R.string.product_quantity, product.getQuantity()));

        ImageView image = item.findViewById(R.id.productImage);
        RemoteImageLoader.load(image, product.getImageUrl());
        item.setContentDescription(getString(
                R.string.product_description, product.getName(), product.getQuantity()));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
