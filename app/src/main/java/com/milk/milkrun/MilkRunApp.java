package com.milk.milkrun;

import android.app.Application;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

public class MilkRunApp extends Application {

    private static final String TAG = "MilkRunApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize periodic sync
        WorkManagerHelper.scheduleSync(this);

        // Set up network monitoring
        setupNetworkListener();
    }

    private void setupNetworkListener() {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

            connectivityManager.registerNetworkCallback(
                    builder.build(),
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
                            Log.d(TAG, "Network available - triggering sync");
                            WorkManagerHelper.triggerImmediateSync(MilkRunApp.this);
                        }

                        @Override
                        public void onLost(Network network) {
                            Log.d(TAG, "Network lost");
                        }

                        @Override
                        public void onUnavailable() {
                            Log.d(TAG, "Network unavailable");
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up network listener", e);
        }
    }
}