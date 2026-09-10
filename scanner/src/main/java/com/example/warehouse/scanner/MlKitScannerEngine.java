package com.example.warehouse.scanner;

import android.media.Image;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

final class MlKitScannerEngine implements ScannerEngine {

    private final LifecycleOwner lifecycleOwner;
    private final PreviewView previewView;
    private final ExecutorService analyzerExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final AtomicBoolean delivered = new AtomicBoolean(false);
    private final com.google.mlkit.vision.barcode.BarcodeScanner barcodeScanner;
    private ProcessCameraProvider cameraProvider;
    private Callback callback;

    MlKitScannerEngine(LifecycleOwner lifecycleOwner, PreviewView previewView) {
        this.lifecycleOwner = lifecycleOwner;
        this.previewView = previewView;
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_EAN_13)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);
    }

    @Override
    public void start(Callback callback) {
        this.callback = callback;
        delivered.set(false);
        ListenableFuture<ProcessCameraProvider> providerFuture =
                ProcessCameraProvider.getInstance(previewView.getContext());
        providerFuture.addListener(() -> {
            try {
                cameraProvider = providerFuture.get();
                bindCamera();
            } catch (Exception exception) {
                callback.onFailure();
            }
        }, ContextCompat.getMainExecutor(previewView.getContext()));
    }

    private void bindCamera() {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ResolutionSelector resolutionSelector = new ResolutionSelector.Builder()
                .setResolutionStrategy(new ResolutionStrategy(
                        new Size(1280, 720),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                ))
                .build();
        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        analysis.setAnalyzer(analyzerExecutor, this::analyze);

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis
        );
    }

    private void analyze(@NonNull ImageProxy imageProxy) {
        if (delivered.get() || !processing.compareAndSet(false, true)) {
            imageProxy.close();
            return;
        }

        Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            processing.set(false);
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.getImageInfo().getRotationDegrees()
        );
        barcodeScanner.process(image)
                .addOnSuccessListener(this::deliverFirstBarcode)
                .addOnFailureListener(error -> notifyFailure())
                .addOnCompleteListener(task -> {
                    processing.set(false);
                    imageProxy.close();
                });
    }

    private void deliverFirstBarcode(List<Barcode> barcodes) {
        if (barcodes.isEmpty()) {
            return;
        }
        String value = barcodes.get(0).getRawValue();
        if (value != null && delivered.compareAndSet(false, true) && callback != null) {
            callback.onBarcode(value);
        }
    }

    private void notifyFailure() {
        if (!delivered.get() && callback != null) {
            callback.onFailure();
        }
    }

    @Override
    public void stop() {
        delivered.set(true);
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        barcodeScanner.close();
        analyzerExecutor.shutdown();
    }
}
