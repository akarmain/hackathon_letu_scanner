package com.example.hackathon_letu_scanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Single source of truth for the boxes shown during receiving.
 *
 * <p>The sample data lives here instead of in an Activity so it can later be replaced by a
 * repository/API without changing either screen.</p>
 */
public final class BoxCatalog {

    private static final String PRODUCT_IMAGE_URL = "https://s3.akarmain.ru/S/cat.jpg";

    private static final List<Product> SAMPLE_PRODUCTS = Collections.unmodifiableList(Arrays.asList(
            new Product("Молочные продукты", "Молоко ультрапастеризованное 3,2%",
                    "1 л · тетра-пак", 6, PRODUCT_IMAGE_URL),
            new Product("Молочные продукты", "Кефир 2,5%",
                    "1 л · пластиковая бутылка", 4, PRODUCT_IMAGE_URL),
            new Product("Сыры", "Сыр российский",
                    "200 г · вакуумная упаковка", 2, PRODUCT_IMAGE_URL),
            new Product("Бакалея", "Макароны спираль",
                    "450 г · пакет", 4, PRODUCT_IMAGE_URL),
            new Product("Бакалея", "Гречка ядрица",
                    "900 г · пакет", 4, PRODUCT_IMAGE_URL),
            new Product("Бакалея", "Сахар-песок",
                    "1 кг · пакет", 4, PRODUCT_IMAGE_URL)
    ));

    private static final List<BoxDetails> BOXES = Collections.unmodifiableList(Arrays.asList(
            new BoxDetails(1, "04601234567893", SAMPLE_PRODUCTS),
            new BoxDetails(2, "04601234567909", SAMPLE_PRODUCTS),
            new BoxDetails(3, "04601234567916", SAMPLE_PRODUCTS),
            new BoxDetails(4, "04601234567923", SAMPLE_PRODUCTS),
            new BoxDetails(5, "04601234567930", SAMPLE_PRODUCTS)
    ));

    private BoxCatalog() {
    }

    @Nullable
    public static BoxDetails findBox(int number) {
        for (BoxDetails box : BOXES) {
            if (box.number == number) {
                return box;
            }
        }
        return null;
    }

    public static final class BoxDetails {
        private final int number;
        @NonNull
        private final String code;
        @NonNull
        private final List<Product> products;

        private BoxDetails(int number, @NonNull String code, @NonNull List<Product> products) {
            this.number = number;
            this.code = code;
            this.products = products;
        }

        public int getNumber() {
            return number;
        }

        @NonNull
        public String getCode() {
            return code;
        }

        @NonNull
        public List<Product> getProducts() {
            return products;
        }

        public int getTotalUnits() {
            int total = 0;
            for (Product product : products) {
                total += product.quantity;
            }
            return total;
        }
    }

    public static final class Product {
        @NonNull
        private final String category;
        @NonNull
        private final String name;
        @NonNull
        private final String packageDescription;
        private final int quantity;
        @NonNull
        private final String imageUrl;

        private Product(@NonNull String category, @NonNull String name,
                @NonNull String packageDescription, int quantity, @NonNull String imageUrl) {
            this.category = category;
            this.name = name;
            this.packageDescription = packageDescription;
            this.quantity = quantity;
            this.imageUrl = imageUrl;
        }

        @NonNull
        public String getCategory() {
            return category;
        }

        @NonNull
        public String getName() {
            return name;
        }

        @NonNull
        public String getPackageDescription() {
            return packageDescription;
        }

        public int getQuantity() {
            return quantity;
        }

        @NonNull
        public String getImageUrl() {
            return imageUrl;
        }
    }
}
