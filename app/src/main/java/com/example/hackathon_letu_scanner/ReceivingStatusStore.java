package com.example.hackathon_letu_scanner;

import android.content.Context;
import android.content.SharedPreferences;

final class ReceivingStatusStore {

    private static final String PREFERENCES_NAME = "receiving_statuses";
    private static final String COMPLETED_PREFIX = "completed_";

    private ReceivingStatusStore() {
    }

    static boolean isCompleted(Context context, String receivingId) {
        return preferences(context).getBoolean(COMPLETED_PREFIX + receivingId, false);
    }

    static void markCompleted(Context context, String receivingId) {
        preferences(context).edit().putBoolean(COMPLETED_PREFIX + receivingId, true).apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
}
