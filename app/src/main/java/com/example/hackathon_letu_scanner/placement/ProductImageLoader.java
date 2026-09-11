package com.example.hackathon_letu_scanner.placement;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.hackathon_letu_scanner.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;

/** Small dependency-free loader for the representative demo images. */
public final class ProductImageLoader {
    private static final String TAG = "ProductImageLoader";
    private static final String DISK_CACHE_DIRECTORY = "product-images";
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final ConcurrentHashMap<String, Object> SOURCE_LOCKS = new ConcurrentHashMap<>();
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

        if (source == null || source.trim().isEmpty()) {
            return;
        }

        Bitmap cached = CACHE.get(source);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        Context appContext = imageView.getContext().getApplicationContext();
        EXECUTOR.execute(() -> {
            Bitmap bitmap = loadOnce(appContext, source);
            if (bitmap == null) {
                return;
            }
            MAIN_HANDLER.post(() -> {
                if (source.equals(imageView.getTag())) {
                    imageView.setImageBitmap(bitmap);
                }
            });
        });
    }

    private static Bitmap loadOnce(Context context, String source) {
        Object sourceLock = SOURCE_LOCKS.computeIfAbsent(source, key -> new Object());
        try {
            synchronized (sourceLock) {
                Bitmap cached = CACHE.get(source);
                if (cached != null) {
                    return cached;
                }

                Bitmap bitmap = readFromDisk(context, source);
                if (bitmap == null) {
                    bitmap = downloadAndPersist(context, source);
                }
                if (bitmap != null) {
                    CACHE.put(source, bitmap);
                }
                return bitmap;
            }
        } finally {
            SOURCE_LOCKS.remove(source, sourceLock);
        }
    }

    private static Bitmap readFromDisk(Context context, String source) {
        File cacheFile = getCacheFile(context, source);
        if (!cacheFile.isFile()) {
            return null;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
        if (bitmap != null) {
            return bitmap;
        }

        Log.w(TAG, "Removing unreadable cached image: " + cacheFile.getName());
        if (!cacheFile.delete()) {
            Log.w(TAG, "Unable to remove unreadable cached image: " + cacheFile.getName());
        }
        return null;
    }

    private static Bitmap downloadAndPersist(Context context, String source) {
        HttpURLConnection connection = null;
        try {
            URI uri = new URI(source.replace(" ", "%20"));
            URL url = new URL(uri.toASCIIString());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(7000);
            connection.setInstanceFollowRedirects(true);
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                Log.w(TAG, "Image request failed with HTTP " + responseCode + ": " + source);
                return null;
            }
            try (BufferedInputStream stream = new BufferedInputStream(connection.getInputStream())) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }

                byte[] imageBytes = output.toByteArray();
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                if (bitmap == null) {
                    Log.w(TAG, "Downloaded file is not a readable image: " + source);
                    return null;
                }
                persist(context, source, imageBytes);
                return bitmap;
            }
        } catch (Exception exception) {
            Log.w(TAG, "Unable to load product image: " + source, exception);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void persist(Context context, String source, byte[] imageBytes) {
        File cacheFile = getCacheFile(context, source);
        File directory = cacheFile.getParentFile();
        if (directory == null
                || (!directory.isDirectory() && !directory.mkdirs() && !directory.isDirectory())) {
            Log.w(TAG, "Unable to create the product image directory");
            return;
        }

        File temporaryFile = null;
        try {
            temporaryFile = File.createTempFile("product-image-", ".tmp", directory);
            try (BufferedOutputStream output = new BufferedOutputStream(
                    new FileOutputStream(temporaryFile))) {
                output.write(imageBytes);
            }

            if (!cacheFile.isFile() && !temporaryFile.renameTo(cacheFile)) {
                Log.w(TAG, "Unable to move product image into persistent storage: " + source);
            }
        } catch (Exception exception) {
            Log.w(TAG, "Unable to persist product image: " + source, exception);
        } finally {
            if (temporaryFile != null && temporaryFile.exists() && !temporaryFile.delete()) {
                Log.w(TAG, "Unable to remove temporary product image: " + temporaryFile.getName());
            }
        }
    }

    private static File getCacheFile(Context context, String source) {
        File directory = new File(context.getFilesDir(), DISK_CACHE_DIRECTORY);
        return new File(directory, sha256(source) + ".image");
    }

    private static String sha256(String source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                int unsignedValue = value & 0xff;
                result.append(Character.forDigit(unsignedValue >>> 4, 16));
                result.append(Character.forDigit(unsignedValue & 0x0f, 16));
            }
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
