package com.example.hackathon_letu_scanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Lightweight URL image loader for the small, fixed receiving list. */
public final class RemoteImageLoader {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final LruCache<String, Bitmap> CACHE = new LruCache<>(12);

    private RemoteImageLoader() {
    }

    public static void load(@NonNull ImageView imageView, @NonNull String imageUrl) {
        imageView.setTag(imageUrl);
        imageView.setImageResource(R.drawable.ic_product_placeholder);

        Bitmap cached = CACHE.get(imageUrl);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        EXECUTOR.execute(() -> {
            Bitmap bitmap = download(imageUrl);
            if (bitmap == null) {
                return;
            }
            CACHE.put(imageUrl, bitmap);
            MAIN_HANDLER.post(() -> {
                if (imageUrl.equals(imageView.getTag())) {
                    imageView.setImageBitmap(bitmap);
                }
            });
        });
    }

    private static Bitmap download(String imageUrl) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(imageUrl).openConnection();
            connection.setConnectTimeout(7_000);
            connection.setReadTimeout(10_000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Accept", "image/*");
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) {
                return null;
            }
            try (BufferedInputStream input = new BufferedInputStream(connection.getInputStream())) {
                return BitmapFactory.decodeStream(input);
            }
        } catch (IOException ignored) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
