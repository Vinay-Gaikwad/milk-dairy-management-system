// Fully Updated UpdateBillActivity.java with Offline Rate and Milk Type Support
package com.milk.milkrun;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.milk.milkrun.collectiondatabase.CollectionDatabase;
import com.milk.milkrun.collectiondatabase.MilkCollection;
import com.milk.milkrun.customerdatabase.Customer;
import com.milk.milkrun.customerdatabase.CustomerDatabase;
import com.milk.milkrun.localrate.LocalMilkRate;
import com.milk.milkrun.localrate.MilkRateDao;
import com.milk.milkrun.localrate.MilkRateDatabase;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class UpdateBillActivity extends AppCompatActivity {

    private TextView tvDateTime, tvRate, tvTotal;
    private EditText etCustomerNo, etCustomerName, etLiter, etFat, etSNF;
    private Button btnUpdate, btnCancel;

    private CollectionDatabase collectionDb;
    private CustomerDatabase customerDb;

    private MilkCollection bill;
    private int billId;
    private double rate = 0.0, total = 0.0;
    private List<LocalMilkRate> localRateList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_bill);

        initViews();

        collectionDb = CollectionDatabase.getInstance(this);
        customerDb = CustomerDatabase.getInstance(this);

        loadLocalRates();

        billId = getIntent().getIntExtra("milkCollectionId", -1);
        if (billId != -1) {
            loadBillData();
        }

        setupListeners();
    }

    private void initViews() {
        tvDateTime = findViewById(R.id.tvDateTime);
        tvRate = findViewById(R.id.tvRate);
        tvTotal = findViewById(R.id.tvTotal);
        etCustomerNo = findViewById(R.id.etCustomerNo);
        etCustomerName = findViewById(R.id.etCustomerName);
        etLiter = findViewById(R.id.etLiter);
        etFat = findViewById(R.id.etFat);
        etSNF = findViewById(R.id.etSNF);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void loadLocalRates() {
        Executors.newSingleThreadExecutor().execute(() -> {
            MilkRateDao dao = MilkRateDatabase.getInstance(this).milkRateDao();
            localRateList = dao.getAllRates();
        });
    }

    private void loadBillData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            bill = collectionDb.milkCollectionDao().getBillById(billId);
            if (bill != null) {
                runOnUiThread(() -> {
                    String formattedDate = bill.trDate != null ? bill.trDate.split(" ")[0] : "--";
                    String formattedTime = "--";
                    try {
                        SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
                        Date time = inputFormat.parse(bill.time);
                        formattedTime = outputFormat.format(time);
                    } catch (Exception ignored) {}

                    tvDateTime.setText("\uD83D\uDCC5 तारीख: " + formattedDate + "\n\uD83D\uDD52 वेळ: " + formattedTime);

                    etCustomerNo.setText(bill.partyCode);
                    etFat.setText(String.valueOf(bill.fat));
                    etSNF.setText(String.valueOf(bill.snf));
                    etLiter.setText(String.valueOf(bill.qty));

                    updateRateAndTotal();

                    Executors.newSingleThreadExecutor().execute(() -> {
                        Customer customer = customerDb.customerDao().findCustomerByNumber(bill.partyCode);
                        runOnUiThread(() -> {
                            if (customer != null) etCustomerName.setText(customer.name);
                        });
                    });
                });
            }
        });
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> finish());

        etCustomerNo.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String number = etCustomerNo.getText().toString().trim();
                Executors.newSingleThreadExecutor().execute(() -> {
                    Customer customer = customerDb.customerDao().findCustomerByNumber(number);
                    runOnUiThread(() -> {
                        if (customer != null) etCustomerName.setText(customer.name);
                    });
                });
            }
        });

        etCustomerName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String name = etCustomerName.getText().toString().trim();
                Executors.newSingleThreadExecutor().execute(() -> {
                    Customer customer = customerDb.customerDao().findCustomerByName(name);
                    runOnUiThread(() -> {
                        if (customer != null) etCustomerNo.setText(customer.number);
                    });
                });
            }
        });

        btnUpdate.setOnClickListener(v -> {
            String fatStr = etFat.getText().toString();
            String snfStr = etSNF.getText().toString();
            String literStr = etLiter.getText().toString();
            String partyCode = etCustomerNo.getText().toString().trim();
            String name = etCustomerName.getText().toString().trim();

            double fat = fatStr.isEmpty() ? 0.0 : Double.parseDouble(fatStr);
            double snf = snfStr.isEmpty() ? 0.0 : Double.parseDouble(snfStr);
            double qty = literStr.isEmpty() ? 0.0 : Double.parseDouble(literStr);

            bill.fat = fat;
            bill.snf = snf;
            bill.qty = qty;
            bill.partyCode = partyCode;
            bill.amt = qty * rate;

            Executors.newSingleThreadExecutor().execute(() -> {
                collectionDb.milkCollectionDao().updateMilkCollection(bill);

                Customer existing = customerDb.customerDao().findCustomerByNumber(partyCode);
                if (existing != null) {
                    existing.name = name;
                    customerDb.customerDao().updateCustomer(existing);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "माहिती यशस्वीरित्या अपडेट झाली!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateRateAndTotal();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etFat.addTextChangedListener(watcher);
        etSNF.addTextChangedListener(watcher);
        etLiter.addTextChangedListener(watcher);
    }

    private void updateRateAndTotal() {
        try {
            double fat = Double.parseDouble(etFat.getText().toString());
            double snf = Double.parseDouble(etSNF.getText().toString());
            double liter = Double.parseDouble(etLiter.getText().toString());
            int milkTypeCode = bill.mlkTypeCode != null && bill.mlkTypeCode.equals("2") ? 2 : 1;

            double minFat = Double.MAX_VALUE, maxFat = Double.MIN_VALUE;
            double minSNF = Double.MAX_VALUE, maxSNF = Double.MIN_VALUE;
            double minRate = Double.MAX_VALUE, maxRate = 0.0;

            for (LocalMilkRate rateObj : localRateList) {
                if (rateObj.mlkTypeCode != milkTypeCode) continue;
                double rFat = Double.parseDouble(rateObj.fat);
                double rSNF = Double.parseDouble(rateObj.snf);
                double rRate = Double.parseDouble(rateObj.rate);

                if (rFat < minFat) minFat = rFat;
                if (rFat > maxFat) maxFat = rFat;
                if (rSNF < minSNF) minSNF = rSNF;
                if (rSNF > maxSNF) maxSNF = rSNF;
                if (rRate < minRate) minRate = rRate;
                if (rRate > maxRate) maxRate = rRate;

                if (Math.abs(rFat - fat) < 0.001 && Math.abs(rSNF - snf) < 0.001) {
                    rate = rRate;
                    total = rate * liter;
                    updateRateText();
                    return;
                }
            }

            if (fat < minFat || snf < minSNF) rate = minRate;
            else if (fat > maxFat && snf > maxSNF) rate = maxRate;
            else rate = maxRate;

            total = rate * liter;
            updateRateText();
        } catch (Exception e) {
            tvRate.setText("रेट: 0.00");
            tvTotal.setText("एकूण: 0.00");
        }
    }

    private void updateRateText() {
        tvRate.setText("रेट: " + String.format("%.2f", rate));
        tvTotal.setText("एकूण: " + String.format("%.2f", total));
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view instanceof EditText) {
                Rect outRect = new Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    view.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

}