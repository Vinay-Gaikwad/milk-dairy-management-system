// FULLY UPDATED EntryActivity.java with real-time calculations and decimal precision
package com.milk.milkrun;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.milk.milkrun.api.milkrate.*;
import com.milk.milkrun.collectiondatabase.*;
import com.milk.milkrun.customerdatabase.*;
import com.milk.milkrun.localrate.*;
import com.milk.milkrun.repository.MilkRepository;

import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EntryActivity extends AppCompatActivity {
    private TextView currentDateText, currentTimeText, tvSoftCustCode;
    private TextInputEditText etCustomerNo, etLiter, etFat, etRate, etTotal, etName, etSNF;
    private RadioGroup timePeriodGroup, milkTypeGroup;
    private RadioButton radioMorning, radioNight, radioCow, radioBuffalo;
    private LinearLayout layoutSNF;
    private MaterialButton btnSaveProceed, btnCancel;

    private CollectionDatabase collectionDatabase;
    private CustomerDatabase customerDatabase;
    private boolean isAutoFilling = false;
    private String softCustCode;
    private int fatTypePreference = 1;

    private List<MilkRate> milkRateList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        softCustCode = prefs.getString("SoftCustCode", "N/A");
        fatTypePreference = prefs.getInt("FatTypePref", -1);

        customerDatabase = CustomerDatabase.getInstance(this);
        collectionDatabase = CollectionDatabase.getInstance(this);

        initializeViews();
        checkFatTypePreference();
        setupDateTime();
        setupTextWatchers();
        setupButtonListeners();
        loadMilkRatesFromLocalDB();
        fetchMilkRateFromAPI(softCustCode);
    }

    private void initializeViews() {
        currentDateText = findViewById(R.id.currentDateText);
        currentTimeText = findViewById(R.id.currentTimeText);
        tvSoftCustCode = findViewById(R.id.tvSoftCustCode);
        etCustomerNo = findViewById(R.id.etCustomerNo);
        etName = findViewById(R.id.etName);
        etLiter = findViewById(R.id.etLiter);
        etFat = findViewById(R.id.etFat);
        etSNF = findViewById(R.id.etSNF);
        etRate = findViewById(R.id.etRate);
        etTotal = findViewById(R.id.etTotal);
        timePeriodGroup = findViewById(R.id.timePeriodGroup);
        milkTypeGroup = findViewById(R.id.milkTypeGroup);
        radioMorning = findViewById(R.id.radioMorning);
        radioNight = findViewById(R.id.radioNight);
        radioCow = findViewById(R.id.radioCow);
        radioBuffalo = findViewById(R.id.radioBuffalo);
        btnSaveProceed = findViewById(R.id.btnSaveProceed);
        btnCancel = findViewById(R.id.btnCancel);
        layoutSNF = findViewById(R.id.layoutSNF);

        tvSoftCustCode.setText("सॉफ्ट कोड: " + softCustCode);

        // Set input filters for decimal precision
        etLiter.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etFat.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etSNF.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    private void checkFatTypePreference() {
        if (fatTypePreference == -1) {
            new AlertDialog.Builder(this)
                    .setTitle("\u092b\u0945\u091f \u092a\u094d\u0930\u0915\u093e\u0930 \u0928\u093f\u0935\u0921\u093e")
                    .setMessage("\u0915\u0943\u092a\u092f\u093e \u092b\u0945\u091f \u0915\u093f\u0902\u0935\u093e \u0928\u093f\u0935\u0921\u093e: \n\u092b\u0915\u094d\u0924 \u092b\u0945\u091f \u0915\u093f\u0902\u0935\u093e \u0915\u093f\u0902\u0935\u093e \u0915\u093f\u0902\u0935\u093e \u090f\u0938\u090f\u0928\u090f\u092b?")
                    .setPositiveButton("\u092b\u0945\u091f", (dialog, which) -> saveFatTypePreference(1))
                    .setNegativeButton("\u092b\u0945\u091f + \u090f\u0938\u090f\u0928\u090f\u092b", (dialog, which) -> saveFatTypePreference(2))
                    .setCancelable(false)
                    .show();
        } else {
            applyFatTypeUI();
        }
    }

    private void saveFatTypePreference(int type) {
        SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
        editor.putInt("FatTypePref", type);
        editor.apply();
        fatTypePreference = type;
        applyFatTypeUI();
    }

    private void applyFatTypeUI() {
        if (fatTypePreference == 1) { // Fat Only
            layoutSNF.setVisibility(View.GONE);
            etSNF.setText("");
            // Recalculate rate if fat has value
            if (!etFat.getText().toString().isEmpty()) {
                applyRateFromFatOrSNF(true);
            }
        } else { // Fat + SNF (for any other value)
            layoutSNF.setVisibility(View.VISIBLE);
            // Recalculate rate if both fields have values
            if (!etFat.getText().toString().isEmpty() && !etSNF.getText().toString().isEmpty()) {
                applyRateFromFatAndSNF();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Clear input fields
        etCustomerNo.setText("");
        etName.setText("");
        etLiter.setText("");
        etFat.setText("");
        etSNF.setText("");
        etRate.setText("");
        etTotal.setText("");

        setupDateTime();
        radioCow.setChecked(true);
        applyFatTypeUI();
    }

    private void setupDateTime() {
        Calendar now = Calendar.getInstance();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.getTime());
        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now.getTime());
        currentDateText.setText("दिनांक: " + currentDate);
        currentTimeText.setText("वेळ: " + currentTime);

        int hour = now.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 15) {
            radioMorning.setChecked(true);
        } else {
            radioNight.setChecked(true);
        }
    }

    private void loadMilkRatesFromLocalDB() {
        new Thread(() -> {
            MilkRateDatabase db = MilkRateDatabase.getInstance(getApplicationContext());
            MilkRateDao dao = db.milkRateDao();
            List<LocalMilkRate> localRates = dao.getAllRates();
            List<MilkRate> result = new ArrayList<>();
            for (LocalMilkRate l : localRates) {
                MilkRate r = new MilkRate();
                r.MlkTypeCode = l.mlkTypeCode;
                r.Fat = l.fat;
                r.SNF = l.snf;
                r.Rate = l.rate;
                result.add(r);
            }
            milkRateList = result;
        }).start();
    }

    private void fetchMilkRateFromAPI(String softCustCode) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getMilkRates(new SoftCustCodeRequest(softCustCode)).enqueue(new Callback<MilkRateResponse>() {
            @Override
            public void onResponse(Call<MilkRateResponse> call, Response<MilkRateResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().status) {
                    List<MilkRate> apiRates = response.body().data;
                    new Thread(() -> {
                        MilkRateDatabase db = MilkRateDatabase.getInstance(getApplicationContext());
                        MilkRateDao dao = db.milkRateDao();
                        List<LocalMilkRate> localRates = dao.getAllRates();

                        boolean dataChanged = localRates.size() != apiRates.size();
                        if (!dataChanged) {
                            for (int i = 0; i < apiRates.size(); i++) {
                                MilkRate r = apiRates.get(i);
                                LocalMilkRate l = localRates.get(i);
                                if (!r.Rate.equals(l.rate)) {
                                    dataChanged = true;
                                    break;
                                }
                            }
                        }

                        if (dataChanged) {
                            dao.clearRates();
                            for (MilkRate rate : apiRates) {
                                LocalMilkRate newRate = new LocalMilkRate();
                                newRate.mlkTypeCode = rate.MlkTypeCode;
                                newRate.fat = rate.Fat;
                                newRate.snf = rate.SNF;
                                newRate.rate = rate.Rate;
                                dao.insert(newRate);
                            }
                            loadMilkRatesFromLocalDB();
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<MilkRateResponse> call, Throwable t) {
                Toast.makeText(EntryActivity.this, "रेट डेटा मिळवण्यात अयशस्वी", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyRateFromFatAndSNF() {
        if (milkRateList.isEmpty()) {
            etRate.setText("");
            return;
        }

        try {
            double fat = Double.parseDouble(etFat.getText().toString().trim());
            double snf = Double.parseDouble(etSNF.getText().toString().trim());
            int mlkTypeCode = (milkTypeGroup.getCheckedRadioButtonId() == R.id.radioCow) ? 2 : 1;

            // Find exact match first
            for (MilkRate rate : milkRateList) {
                if (rate.MlkTypeCode != mlkTypeCode) continue;

                double rateFat = Double.parseDouble(rate.Fat);
                double rateSNF = Double.parseDouble(rate.SNF);

                if (Math.abs(rateFat - fat) < 0.001 && Math.abs(rateSNF - snf) < 0.001) {
                    etRate.setText(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(rate.Rate)));
                    calculateTotal();
                    return;
                }
            }

            // If no exact match, find closest
            MilkRate closestRate = findClosestRate(mlkTypeCode, fat, snf);
            if (closestRate != null) {
                etRate.setText(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(closestRate.Rate)));
                calculateTotal();
            } else {
                etRate.setText("");
                etTotal.setText("");
            }
        } catch (Exception e) {
            etRate.setText("");
            etTotal.setText("");
        }
    }

    private MilkRate findClosestRate(int mlkTypeCode, double fat, double snf) {
        MilkRate closest = null;
        double minDistance = Double.MAX_VALUE;

        for (MilkRate rate : milkRateList) {
            if (rate.MlkTypeCode != mlkTypeCode) continue;

            try {
                double rateFat = Double.parseDouble(rate.Fat);
                double rateSNF = Double.parseDouble(rate.SNF);
                double distance = Math.sqrt(Math.pow(rateFat - fat, 2) + Math.pow(rateSNF - snf, 2));

                if (distance < minDistance) {
                    minDistance = distance;
                    closest = rate;
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }

        return closest;
    }

    private void applyRateFromFatOrSNF(boolean isFat) {
        if (milkRateList.isEmpty()) {
            etRate.setText("");
            return;
        }

        try {
            double value = Double.parseDouble(isFat ? etFat.getText().toString().trim() : etSNF.getText().toString().trim());
            int mlkTypeCode = (milkTypeGroup.getCheckedRadioButtonId() == R.id.radioCow) ? 2 : 1;

            // Find exact match first
            for (MilkRate rate : milkRateList) {
                if (rate.MlkTypeCode != mlkTypeCode) continue;

                double testValue = Double.parseDouble(isFat ? rate.Fat : rate.SNF);
                if (Math.abs(testValue - value) < 0.001) {
                    etRate.setText(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(rate.Rate)));
                    calculateTotal();
                    return;
                }
            }

            // If no exact match, find closest
            double closestValue = Double.MAX_VALUE;
            String closestRate = "0.00";

            for (MilkRate rate : milkRateList) {
                if (rate.MlkTypeCode != mlkTypeCode) continue;

                double testValue = Double.parseDouble(isFat ? rate.Fat : rate.SNF);
                double currentDiff = Math.abs(testValue - value);

                if (currentDiff < Math.abs(closestValue - value)) {
                    closestValue = testValue;
                    closestRate = rate.Rate;
                }
            }

            if (closestValue != Double.MAX_VALUE) {
                etRate.setText(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(closestRate)));
                calculateTotal();
            } else {
                etRate.setText("");
                etTotal.setText("");
            }
        } catch (Exception e) {
            etRate.setText("");
            etTotal.setText("");
        }
    }

    private void setupTextWatchers() {
        // Enhanced Fat TextWatcher
        etFat.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    etRate.setText("");
                    etTotal.setText("");
                    return;
                }
                if (fatTypePreference == 1) {
                    applyRateFromFatOrSNF(true);
                } else if (!etSNF.getText().toString().isEmpty()) {
                    applyRateFromFatAndSNF();
                }
                calculateTotal();
            }
        });

        // Enhanced SNF TextWatcher
        etSNF.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (fatTypePreference == 2) {
                    if (s.length() == 0) {
                        etRate.setText("");
                        etTotal.setText("");
                        return;
                    }
                    if (!etFat.getText().toString().isEmpty()) {
                        applyRateFromFatAndSNF();
                    }
                }
            }
        });

        // Enhanced Liter TextWatcher with decimal precision
        etLiter.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Ensure proper decimal format
                String text = s.toString();
                if (text.contains(".") && text.substring(text.indexOf(".") + 1).length() > 2) {
                    s.delete(text.length() - 1, text.length());
                }

                if (s.length() == 0) {
                    etTotal.setText("");
                    return;
                }
                calculateTotal();
            }
        });

        // Enhanced Rate TextWatcher
        etRate.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    etTotal.setText("");
                    return;
                }
                calculateTotal();
            }
        });

        // Customer number TextWatcher
        etCustomerNo.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (isAutoFilling) return;
                String number = s.toString().trim();
                if (!number.isEmpty()) new Thread(() -> {
                    Customer customer = customerDatabase.customerDao().getCustomerByNumber(number);
                    runOnUiThread(() -> {
                        isAutoFilling = true;
                        if (customer != null) {
                            etName.setText(customer.name);
                            if (fatTypePreference == -1 && (customer.fatType == 1 || customer.fatType == 2)) {
                                saveFatTypePreference(customer.fatType);
                            }
                        } else {
                            etName.setText("");
                        }
                        isAutoFilling = false;
                    });
                }).start();
            }
        });

        // Name TextWatcher
        etName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (isAutoFilling) return;
                String name = s.toString().trim();
                if (!name.isEmpty()) new Thread(() -> {
                    Customer customer = customerDatabase.customerDao().getCustomerByName(name);
                    runOnUiThread(() -> {
                        isAutoFilling = true;
                        if (customer != null) {
                            etCustomerNo.setText(customer.number);
                            if (fatTypePreference == -1 && (customer.fatType == 1 || customer.fatType == 2)) {
                                saveFatTypePreference(customer.fatType);
                            }
                        } else {
                            etCustomerNo.setText("");
                        }
                        isAutoFilling = false;
                    });
                }).start();
            }
        });
    }

    private void setupButtonListeners() {
        // Milk type change listener
        milkTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (!etFat.getText().toString().isEmpty()) {
                if (fatTypePreference == 1) {
                    applyRateFromFatOrSNF(true);
                } else if (!etSNF.getText().toString().isEmpty()) {
                    applyRateFromFatAndSNF();
                }
                calculateTotal();
            }
        });

        // Save button
        btnSaveProceed.setOnClickListener(v -> {
            String customerNo = etCustomerNo.getText().toString().trim();
            String name = etName.getText().toString().trim();
            if (customerNo.isEmpty() || name.isEmpty()) {
                showToast("कृपया ग्राहक क्रमांक आणि नाव प्रविष्ट करा");
                return;
            }

            String timePeriod = (timePeriodGroup.getCheckedRadioButtonId() == R.id.radioMorning) ? "सकाळ" : "संध्याकाळ";
            String milkType = (milkTypeGroup.getCheckedRadioButtonId() == R.id.radioCow) ? "गाय" : "म्हैस";
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

            new Thread(() -> {
                List<MilkCollection> entries = collectionDatabase.milkCollectionDao().getEntriesForCustomerAndDate(customerNo, today);
                String slotKey = timePeriod + "_" + milkType;
                boolean exists = entries.stream().anyMatch(e -> e.timePeriod.equals(slotKey));
                if (exists) {
                    runOnUiThread(() -> showToast("या ग्राहकासाठी या स्लॉटसाठी नोंद आधीच आहे"));
                } else {
                    runOnUiThread(() -> showConfirmationDialog(customerNo, name));
                }
            }).start();
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> {
            startActivity(new Intent(EntryActivity.this, DashboardActivity.class));
            finish();
        });
    }

    private void showConfirmationDialog(String customerNo, String name) {
        new AlertDialog.Builder(this)
                .setTitle("नोंद तपासा")
                .setMessage("आपण ही नोंद जतन करू इच्छिता?")
                .setPositiveButton("होय", (dialog, which) -> saveMilkData(customerNo, name))
                .setNegativeButton("रद्द करा", null)
                .show();
    }

    private void saveMilkData(String customerNo, String name) {
        new Thread(() -> {
            Customer matchedCustomer = customerDatabase.customerDao().getCustomerByNumber(customerNo);
            if (matchedCustomer == null || !matchedCustomer.name.equalsIgnoreCase(name)) {
                runOnUiThread(() -> showToast("ग्राहक सापडला नाही. कृपया तपासा."));
                return;
            }

            // Format liter to 2 decimal places
            String literStr = etLiter.getText().toString().trim();
            double liters = 0.0;
            try {
                liters = Double.parseDouble(literStr);
                literStr = String.format(Locale.getDefault(), "%.2f", liters);
            } catch (NumberFormatException e) {
                runOnUiThread(() -> showToast("अवैध लिटर मूल्य"));
                return;
            }

            String fat = etFat.getText().toString().trim();
            String snf = etSNF.getText().toString().trim();
            String rate = etRate.getText().toString().trim();
            String total = etTotal.getText().toString().trim();
            String dateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
            String timeNow = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());

            String timePeriod = (timePeriodGroup.getCheckedRadioButtonId() == R.id.radioMorning) ? "सकाळ" : "संध्याकाळ";
            int meCode = timePeriod.equals("सकाळ") ? 1 : 2;
            String milkType = (milkTypeGroup.getCheckedRadioButtonId() == R.id.radioCow) ? "गाय" : "म्हैस";
            int milkTypeCode = milkType.equals("म्हैस") ? 1 : 2;

            String currentDateCompact = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Calendar.getInstance().getTime());
            int randomNum = new Random().nextInt(8999) + 1000;
            String atrNo = "A" + currentDateCompact + randomNum;
            String trNo = "T" + currentDateCompact + randomNum;
            String mlkTrType = "2";

            MilkCollection milkCollection = new MilkCollection();
            milkCollection.softCustCode = softCustCode;
            milkCollection.trDate = dateOnly + " " + timeNow;
            milkCollection.time = timeNow;
            milkCollection.timePeriod = timePeriod + "_" + milkType;
            milkCollection.meCode = meCode;
            milkCollection.partyCode = customerNo;
            milkCollection.mlkTypeCode = String.valueOf(milkTypeCode);
            milkCollection.qty = liters;
            milkCollection.fat = fat.isEmpty() ? 0.0 : Double.parseDouble(fat);
            milkCollection.snf = snf.isEmpty() ? 0.0 : Double.parseDouble(snf);
            milkCollection.rate = rate.isEmpty() ? 0.0 : Double.parseDouble(rate);
            milkCollection.amt = total.isEmpty() ? 0.0 : Double.parseDouble(total);
            milkCollection.aTrNo = atrNo;
            milkCollection.trNo = trNo;
            milkCollection.mlkTrType = mlkTrType;

            long insertedId = collectionDatabase.milkCollectionDao().insert(milkCollection);
            runOnUiThread(() -> {
                if (insertedId > 0) {
                    showToast("यशस्वीरित्या जतन झाले!");
                    new MilkRepository(this).syncRecord(milkCollection);
                    launchBillActivity(milkCollection);
                } else {
                    showToast("जतन अयशस्वी.");
                }
            });
        }).start();
    }

    private void launchBillActivity(MilkCollection milkCollection) {
        Intent intent = new Intent(EntryActivity.this, BillActivity.class);
        intent.putExtra("customerNo", milkCollection.partyCode);
        intent.putExtra("name", etName.getText().toString().trim());
        intent.putExtra("liters", milkCollection.qty);
        intent.putExtra("fat", milkCollection.fat);
        intent.putExtra("snf", milkCollection.snf);
        intent.putExtra("rate", milkCollection.rate);
        intent.putExtra("total", milkCollection.amt);
        intent.putExtra("date", milkCollection.trDate);
        intent.putExtra("time", milkCollection.time);
        intent.putExtra("timePeriod", milkCollection.timePeriod);
        intent.putExtra("milkType", milkCollection.mlkTypeCode);
        startActivity(intent);
    }

    private void calculateTotal() {
        try {
            String literStr = etLiter.getText().toString();
            String rateStr = etRate.getText().toString();

            if (literStr.isEmpty() || rateStr.isEmpty()) {
                etTotal.setText("");
                return;
            }

            double liter = Double.parseDouble(literStr);
            double rate = Double.parseDouble(rateStr);
            double total = liter * rate;

            etTotal.setText(String.format(Locale.getDefault(), "%.2f", total));
        } catch (Exception e) {
            etTotal.setText("");
        }
    }

    private void showToast(String message) {
        Toast t = Toast.makeText(this, message, Toast.LENGTH_LONG);
        t.show();
        new android.os.Handler().postDelayed(t::cancel, 3500);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view instanceof TextInputEditText) {
                Rect outRect = new Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    view.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    abstract class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}