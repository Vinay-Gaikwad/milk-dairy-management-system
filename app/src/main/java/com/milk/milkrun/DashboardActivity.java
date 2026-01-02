package com.milk.milkrun;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.milk.milkrun.api.party.MstParty;
import com.milk.milkrun.api.party.MstPartyResponse;
import com.milk.milkrun.api.party.PartyApiService;
import com.milk.milkrun.api.party.PartyRetrofitClient;
import com.milk.milkrun.collectiondatabase.CollectionDatabase;
import com.milk.milkrun.collectiondatabase.MilkCollection;
import com.milk.milkrun.customerdatabase.Customer;
import com.milk.milkrun.customerdatabase.CustomerDao;
import com.milk.milkrun.customerdatabase.CustomerDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    ImageView profileIcon;
    Dialog profileDialog;
    TextView welcomeText, dateText;
    Button addEntryBtn, viewRecordsBtn;
    String softCustCode, mobile;
    BarChart barChart;

    @Override
    protected void onResume() {
        super.onResume();
        getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .edit()
                .putString("lastActivity", this.getClass().getSimpleName())
                .apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        softCustCode = prefs.getString("SoftCustCode", "N/A");
        mobile = prefs.getString("Mobile", "N/A");
        observeMilkData();
        observeRecentEntries();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        CollectionDatabase.getInstance(this).milkCollectionDao()
                .getMilkForTodayAndYesterday(today + "%", yesterday + "%")
                .observe(this, this::setupComparisonBarChart);

        profileIcon = findViewById(R.id.profileIcon);
        welcomeText = findViewById(R.id.welcomeText);
        dateText = findViewById(R.id.dateText);
        TextView shiftText = findViewById(R.id.shiftText);
        String shift = getCurrentShift();
        shiftText.setText("शिफ्ट: " + shift);
        addEntryBtn = findViewById(R.id.addEntryBtn);
        viewRecordsBtn = findViewById(R.id.viewRecordsBtn);
        barChart = findViewById(R.id.barChart);

        ImageButton btnNewCustomer = findViewById(R.id.btnNewCustomer);
        ImageButton btnCustomerData = findViewById(R.id.btnCustomerData);

        btnNewCustomer.setOnClickListener(v -> startActivity(new Intent(this, NewCustomerRegistrationActivity.class)));
        btnCustomerData.setOnClickListener(v -> startActivity(new Intent(this, CustomerListActivity.class)));

        String currentDate = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());
        dateText.setText("आजची तारीख: " + currentDate);
        welcomeText.setText("स्वागत आहे, " + softCustCode);

        addEntryBtn.setOnClickListener(v -> startActivity(new Intent(this, EntryActivity.class).putExtra("SoftCustCode", softCustCode)));
        viewRecordsBtn.setOnClickListener(v -> startActivity(new Intent(this, BillListActivity.class)));
        profileIcon.setOnClickListener(v -> showProfileDialog());
        findViewById(R.id.shareSummaryBtn).setOnClickListener(v -> shareCardAsImage());

        fetchMstPartyData();
    }

    private String getCurrentShift() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        if ((hour >= 5 && hour < 15) || (hour == 15 && minute == 0)) {
            return "सकाळ";
        } else {
            return "संध्याकाळ";
        }
    }

    private void fetchMstPartyData() {
        PartyApiService apiService = PartyRetrofitClient.getClient().create(PartyApiService.class);
        Map<String, String> request = new HashMap<>();
        request.put("SoftCustCode", softCustCode);

        apiService.getMstPartyBySoftCustCode(request).enqueue(new Callback<MstPartyResponse>() {
            @Override
            public void onResponse(Call<MstPartyResponse> call, Response<MstPartyResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().status) {
                    List<MstParty> partyList = response.body().data;

                    new Thread(() -> {
                        CustomerDao dao = CustomerDatabase.getInstance(DashboardActivity.this).customerDao();
                        for (MstParty party : partyList) {
                            Customer customer = new Customer();
                            customer.number = party.PartyCode;
                            customer.name = party.PartyNameOth;
                            customer.mobile = party.Mob != null && !party.Mob.isEmpty() ? party.Mob : "Not set yet";
                            customer.address = party.Address != null && !party.Address.isEmpty() ? party.Address : "Not set yet";
                            customer.date = "";
                            customer.fatType = party.FatType;
                            dao.insertOrUpdate(customer);
                        }

                        runOnUiThread(() -> {
                            observeMilkData();
                            observeRecentEntries();
                        });
                    }).start();

                    Toast.makeText(DashboardActivity.this, "ग्राहक डेटा यशस्वीरित्या मिळाली", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DashboardActivity.this, "ग्राहक डेटा मिळवता आला नाही", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MstPartyResponse> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "सर्व्हर त्रुटी: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeMilkData() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String fullDate = today + "%";

        CollectionDatabase.getInstance(this).milkCollectionDao()
                .getLiveCollectionsForDate(fullDate)
                .observe(this, collections -> {
                    String currentShift = getCurrentShift();
                    Log.d("MilkDebug", "Current Shift: " + currentShift);
                    Log.d("MilkDebug", "Total Records Today: " + collections.size());

                    double cowLiters = 0, cowAmount = 0;
                    double buffaloLiters = 0, buffaloAmount = 0;
                    int cowEntries = 0, buffaloEntries = 0;

                    for (MilkCollection record : collections) {
                        Log.d("MilkDebug", "Record TimePeriod: " + record.timePeriod);
                        if (record.timePeriod == null) continue;

                        if (!record.timePeriod.startsWith(currentShift)) continue;

                        if ("2".equals(record.mlkTypeCode)) {
                            cowLiters += record.qty;
                            cowAmount += record.amt;
                            cowEntries++;
                        } else if ("1".equals(record.mlkTypeCode)) {
                            buffaloLiters += record.qty;
                            buffaloAmount += record.amt;
                            buffaloEntries++;
                        }
                    }

                    // Updated to show liters with two decimal places
                    ((TextView) findViewById(R.id.cowTotalLiters)).setText(String.format(Locale.getDefault(), "%.2f लिटर", cowLiters));
                    ((TextView) findViewById(R.id.cowTotalAmount)).setText(String.format("₹ %.2f", cowAmount));
                    ((TextView) findViewById(R.id.cowTotalEntries)).setText(String.valueOf(cowEntries));

                    ((TextView) findViewById(R.id.buffaloTotalLiters)).setText(String.format(Locale.getDefault(), "%.2f लिटर", buffaloLiters));
                    ((TextView) findViewById(R.id.buffaloTotalAmount)).setText(String.format("₹ %.2f", buffaloAmount));
                    ((TextView) findViewById(R.id.buffaloTotalEntries)).setText(String.valueOf(buffaloEntries));

                    Log.d("MilkDebug", "Cow: " + cowEntries + " entries | Buffalo: " + buffaloEntries + " entries");
                });
    }

    private void observeRecentEntries() {
        CollectionDatabase.getInstance(this).milkCollectionDao()
                .getLiveRecentEntries()
                .observe(this, recentList -> {
                    LinearLayout layout = findViewById(R.id.recentEntriesLayout);
                    layout.removeAllViews();

                    int index = 0;
                    for (MilkCollection entry : recentList) {
                        View cardView = LayoutInflater.from(this).inflate(R.layout.item_recent_entry, layout, false);

                        TextView tvCustomerInfo = cardView.findViewById(R.id.tvCustomerInfo);
                        TextView tvAnimalType = cardView.findViewById(R.id.tvAnimalType);
                        TextView tvDate = cardView.findViewById(R.id.tvDate);
                        TextView tvLiters = cardView.findViewById(R.id.tvLiters);
                        TextView tvTotal = cardView.findViewById(R.id.tvTotal);

                        // Format time (HH:mm:ss to hh:mm:ss a)
                        String formattedTime = "";
                        try {
                            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
                            Date time = inputFormat.parse(entry.time);
                            formattedTime = outputFormat.format(time);
                        } catch (Exception ignored) {}

                        String dateOnly = entry.trDate != null ? entry.trDate.split(" ")[0] : "--";

                        // Fixed shift display logic
                        String shiftDisplay = "--";
                        if (entry.timePeriod != null) {
                            if (entry.timePeriod.startsWith("सकाळ")) {
                                shiftDisplay = "सकाळ";
                            } else if (entry.timePeriod.startsWith("संध्याकाळ")) {
                                shiftDisplay = "संध्याकाळ";
                            } else {
                                // Fallback for unexpected formats
                                shiftDisplay = entry.timePeriod;
                            }
                        }

                        // Updated to show date and shift in separate lines
                        tvDate.setText("तारीख: " + dateOnly + "\nशिफ्ट: " + shiftDisplay);
                        tvAnimalType.setText("दूध प्रकार: " + ("1".equals(entry.mlkTypeCode) ? "म्हैस" : "गाय"));
                        tvLiters.setText("लिटर: " + String.format(Locale.getDefault(), "%.2f", entry.qty));
                        tvTotal.setText("एकूण: ₹" + String.format(Locale.getDefault(), "%.2f", entry.amt));

                        // Load customer name in background
                        Executors.newSingleThreadExecutor().execute(() -> {
                            CustomerDao customerDao = CustomerDatabase.getInstance(this).customerDao();
                            Customer customer = customerDao.getCustomerByNumber(entry.partyCode);

                            runOnUiThread(() -> {
                                String customerName = (customer != null) ? customer.name : "अज्ञात";
                                tvCustomerInfo.setText("ग्राहक: " + customerName + " (क्र.: " + entry.partyCode + ")");
                            });
                        });

                        if (index < 2) layout.addView(cardView);
                        index++;
                    }
                });
    }

    private void setupComparisonBarChart(List<MilkCollection> data) {
        double todayTotal = 0, yesterdayTotal = 0;

        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        for (MilkCollection record : data) {
            if (record.trDate != null) {
                if (record.trDate.startsWith(todayStr)) {
                    todayTotal += record.qty;
                } else if (record.trDate.startsWith(yesterdayStr)) {
                    yesterdayTotal += record.qty;
                }
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) yesterdayTotal));
        entries.add(new BarEntry(1, (float) todayTotal));

        BarDataSet dataSet = new BarDataSet(entries, "एकूण संकलन");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(14f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setTextSize(14f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(Arrays.asList("काल", "आज")));
        xAxis.setGranularity(1f);
        xAxis.setTextSize(14f);
        xAxis.setDrawGridLines(false);

        barChart.getAxisLeft().setTextSize(14f);
        barChart.getAxisRight().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void shareCardAsImage() {
        View card = findViewById(R.id.summaryCardView);
        if (card == null) {
            Toast.makeText(this, "सारांश कार्ड सापडले नाही!", Toast.LENGTH_SHORT).show();
            return;
        }

        card.measure(View.MeasureSpec.makeMeasureSpec(card.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(card.getHeight(), View.MeasureSpec.EXACTLY));
        card.layout(0, 0, card.getMeasuredWidth(), card.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(card.getWidth(), card.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        card.draw(canvas);

        try {
            File file = new File(getExternalCacheDir(), "summary.png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Uri uri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    file
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "सारांश शेअर करा"));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "सारांश शेअर करताना त्रुटी!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProfileDialog() {
        profileDialog = new Dialog(this);
        profileDialog.setContentView(R.layout.profile_popup);
        profileDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        profileDialog.setCancelable(true);
        profileDialog.setCanceledOnTouchOutside(true);

        ((TextView) profileDialog.findViewById(R.id.usernameText)).setText("कोड: " + softCustCode);
        ((TextView) profileDialog.findViewById(R.id.joiningDateText)).setText("मोबाईल: " + mobile);

        profileDialog.findViewById(R.id.logoutBtn).setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        profileDialog.findViewById(R.id.closePopupBtn).setOnClickListener(v -> profileDialog.dismiss());
        profileDialog.show();
    }
}