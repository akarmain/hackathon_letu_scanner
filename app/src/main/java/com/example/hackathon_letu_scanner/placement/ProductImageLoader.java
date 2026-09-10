package com.example.hackathon_letu_scanner.placement;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.hackathon_letu_scanner.R;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Small dependency-free loader for the representative demo images. */
public final class ProductImageLoader {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(12 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount() / 1024;
        }
    };

    private ProductImageLoader() {
    }

    public static void load(ImageView imageView, String source, String contentDescription) {
        imageView.setImageResource(R.drawable.ic_product_placeholder);
        imageView.setContentDescription(contentDescription);
        imageView.setTag(source);

        Bitmap cached = CACHE.get(source);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        EXECUTOR.execute(() -> {
            Bitmap bitmap = download(source);
            if (bitmap == null) {
                return;
            }
            CACHE.put(source, bitmap);
            MAIN_HANDLER.post(() -> {
                if (source.equals(imageView.getTag())) {
                    imageView.setImageBitmap(bitmap);
                }
            });
        });
    }

    private static Bitmap download(String source) {
        HttpURLConnection connection = null;
        try {
            URI uri = new URI(source.replace(" ", "%20"));
            URL url = new URL(uri.toASCIIString());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(7000);
            connection.setInstanceFollowRedirects(true);
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) {
                return null;
            }
            try (BufferedInputStream stream = new BufferedInputStream(connection.getInputStream())) {
                return BitmapFactory.decodeStream(stream);
            }
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
