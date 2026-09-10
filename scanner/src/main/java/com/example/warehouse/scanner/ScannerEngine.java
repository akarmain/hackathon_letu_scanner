package com.example.warehouse.scanner;

/** Hardware-independent scanner lifecycle. */
public interface ScannerEngine {

    interface Callback {
        void onBarcode(String value);

        void onFailure();
    }

    void start(Callback callback);

    void stop();
}
