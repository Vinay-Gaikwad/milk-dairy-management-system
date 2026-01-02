// NetworkReceiver.java
package com.milk.milkrun.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.milk.milkrun.workers.SyncWorker;

public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            WorkManager.getInstance(context).enqueue(
                    new OneTimeWorkRequest.Builder(SyncWorker.class).build()
            );
        }
    }
}