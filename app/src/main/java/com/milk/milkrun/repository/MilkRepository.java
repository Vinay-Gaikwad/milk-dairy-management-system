package com.milk.milkrun.repository;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.milk.milkrun.api.model.ApiResponse;
import com.milk.milkrun.api.model.TrnMilkModel;
import com.milk.milkrun.api.model.RetrofitClient;
import com.milk.milkrun.collectiondatabase.CollectionDatabase;
import com.milk.milkrun.collectiondatabase.MilkCollection;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MilkRepository {
    private static final String TAG = "MilkRepository";
    private final CollectionDatabase localDb;
    private final Context context;

    public MilkRepository(Context context) {
        this.context = context;
        this.localDb = CollectionDatabase.getInstance(context);
    }

    public void saveCollection(MilkCollection collection) {
        new Thread(() -> {
            try {
                long id = localDb.milkCollectionDao().insert(collection);
                collection.id = (int) id;
                Log.d(TAG, "Saved locally with ID: " + collection.id);

                // Attempt sync immediately after local save
                syncRecord(collection);
            } catch (Exception e) {
                Log.e(TAG, "Local DB insert failed: " + e.getMessage());
            }
        }).start();
    }

    public void syncRecord(MilkCollection collection) {
        TrnMilkModel model = convertToApiModel(collection);

        Log.d(TAG, "Sending to API: " + new Gson().toJson(model)); // ✅ Logs the full JSON

        RetrofitClient.getApiService().sendMilkTransaction(model).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccessful()) {
                    handleApiResponse(collection);
                } else {
                    handleApiError(collection, "API error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                handleApiError(collection, "Network error: " + t.getMessage());
            }
        });
    }

    private TrnMilkModel convertToApiModel(MilkCollection collection) {
        TrnMilkModel model = new TrnMilkModel();
        model.softCustCode = collection.softCustCode;
        model.atrNo = "ATR" + collection.id;
        model.trNo = "TR" + collection.id;

        // 🔥 Send only yyyy-MM-dd to backend (already handled)
        model.trDate = collection.trDate; // this should already be like "2025-04-25"

        model.mlkTrType = "2"; // Always set to "2"
        model.meCode = collection.meCode;
        model.partyCode = collection.partyCode;
        model.mlkTypeCode = collection.mlkTypeCode;
        model.qty = collection.qty;
        model.fat = collection.fat;
        model.snf = collection.snf;
        model.rate = collection.rate;
        model.amt = collection.amt;

        // set remaining fields 0
        model.sampNo = "";
        model.srNo = 0;
        model.deg = 0;
        model.water = 0;
        model.blTypeCode = "";
        model.brCode = "";
        model.pstInd = "";
        model.sgSdQty = 0;
        model.sgSdFat = 0;
        model.sgSdSNF = 0;
        model.sgSdRate = 0;
        model.sgSdAmt = 0;
        model.ssmQty = 0;
        model.ssmFat = 0;
        model.ssmSNF = 0;
        model.ssmRate = 0;
        model.ssmAmt = 0;
        model.bdQty = 0;
        model.bdRate = 0;
        model.bdAmt = 0;

        return model;
    }


    private void handleApiResponse(MilkCollection collection) {
        new Thread(() -> {
            collection.isSynced = true;
            localDb.milkCollectionDao().update(collection);
            Log.d(TAG, "Record synced successfully. ID: " + collection.id);
        }).start();
    }

    private void handleApiError(MilkCollection collection, String error) {
        Log.e(TAG, "Sync failed for record ID " + collection.id + ": " + error);
    }

    public List<MilkCollection> getUnsyncedRecords() {
        return localDb.milkCollectionDao().getUnsyncedRecords();
    }

    public void syncPendingRecords() {
        new Thread(() -> {
            List<MilkCollection> unsynced = getUnsyncedRecords();
            Log.d(TAG, "Found " + unsynced.size() + " unsynced records.");
            for (MilkCollection record : unsynced) {
                syncRecord(record);
            }
        }).start();
    }
}
