package com.milk.milkrun;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.milk.milkrun.collectiondatabase.CollectionDatabase;
import com.milk.milkrun.PendingRequest;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";
    private static final String API_URL = "https://api.sanadeinfotech.in/api.php";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting sync worker");

        CollectionDatabase db = CollectionDatabase.getInstance(getApplicationContext());
        List<PendingRequest> pendingRequests = db.pendingRequestDao().getAllPendingRequests();

        if (pendingRequests.isEmpty()) {
            Log.d(TAG, "No pending requests to sync");
            return Result.success();
        }

        Log.d(TAG, "Found " + pendingRequests.size() + " pending requests");

        boolean allSuccess = true;

        for (PendingRequest request : pendingRequests) {
            if (isStopped()) {
                Log.d(TAG, "Worker stopped before completing all requests");
                return Result.retry();
            }

            try {
                Log.d(TAG, "Processing request ID: " + request.id);

                boolean success = sendToApi(request.jsonData);

                if (success) {
                    Log.d(TAG, "Successfully synced request ID: " + request.id);
                    db.pendingRequestDao().deleteById(request.id);
                } else {
                    if (request.retryCount >= 2) { // After 3 total attempts (0, 1, 2)
                        Log.w(TAG, "Max retries reached for request ID: " + request.id);
                        db.pendingRequestDao().deleteById(request.id);
                    } else {
                        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                .format(new Date());
                        db.pendingRequestDao().incrementRetryCount(request.id, timestamp);
                        Log.d(TAG, "Incremented retry count for request ID: " + request.id);
                        allSuccess = false;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing request ID: " + request.id, e);
                allSuccess = false;
            }
        }

        return allSuccess ? Result.success() : Result.retry();
    }

    private boolean sendToApi(String jsonData) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(API_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(15000); // 15 seconds
            connection.setReadTimeout(15000);
            connection.setDoOutput(true);

            // Write the JSON data
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Check response code
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "API response code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response if needed
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    Log.d(TAG, "API response: " + response.toString());
                }
                return true;
            } else {
                // Read error stream if available
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    Log.e(TAG, "API error response: " + errorResponse.toString());
                }
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending data to API", e);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}