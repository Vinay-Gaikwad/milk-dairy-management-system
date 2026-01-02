package com.milk.milkrun;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.milk.milkrun.collectiondatabase.CollectionDatabase;
import com.milk.milkrun.collectiondatabase.MilkCollection;
import com.milk.milkrun.customerdatabase.Customer;
import com.milk.milkrun.customerdatabase.CustomerDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class BillListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BillListAdapter adapter;
    private EditText etSearch;
    private ImageView ivSearch;
    private TextView tvNoRecords;

    private List<MilkCollection> allBills = new ArrayList<>();
    private List<MilkCollection> filteredBills = new ArrayList<>();
    private HashMap<String, String> customerMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_list);

        recyclerView = findViewById(R.id.recyclerViewBills);
        etSearch = findViewById(R.id.etSearch);
        ivSearch = findViewById(R.id.ivSearch);
        tvNoRecords = findViewById(R.id.tvNoRecords);
        LinearLayout btnHome = findViewById(R.id.btnHomeContainer);
        LinearLayout btnViewCustomers = findViewById(R.id.btnViewCustomersContainer);

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
        }
        if (btnViewCustomers != null) {
            btnViewCustomers.setOnClickListener(v -> startActivity(new Intent(this, CustomerListActivity.class)));
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BillListAdapter(filteredBills, this::onBillClick, this::onBillEditClick, customerMap);
        recyclerView.setAdapter(adapter);

        preloadCustomers();
        observeBills();
        setupSearch();
    }

    private void preloadCustomers() {
        new Thread(() -> {
            List<Customer> customers = CustomerDatabase.getInstance(this).customerDao().getAllCustomers();
            for (Customer customer : customers) {
                customerMap.put(customer.number, customer.name);
            }
        }).start();
    }

    private void observeBills() {
        LiveData<List<MilkCollection>> liveBills = CollectionDatabase.getInstance(this)
                .milkCollectionDao()
                .getAllCollectionsLive();

        liveBills.observe(this, collections -> {
            allBills.clear();
            if (collections != null) {
                allBills.addAll(collections);
                Collections.sort(allBills, (o1, o2) -> {
                    if (o2.trDate != null && o1.trDate != null) {
                        return o2.trDate.compareTo(o1.trDate);
                    } else {
                        return 0;
                    }
                });
            }

            String currentQuery = etSearch.getText().toString().trim();
            performSearch(currentQuery);
        });
    }

    private void setupSearch() {
        etSearch.setFocusable(false);
        etSearch.setClickable(true);

        etSearch.setOnClickListener(v -> openDatePicker());

        ivSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            performSearch(query);
        });
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    etSearch.setText(formattedDate);

                    AlertDialog loadingDialog = new AlertDialog.Builder(this)
                            .setView(getLayoutInflater().inflate(R.layout.dialog_loading, null))
                            .setCancelable(false)
                            .create();
                    loadingDialog.show();

                    new Handler().postDelayed(() -> {
                        loadingDialog.dismiss();
                        performSearch(formattedDate);
                    }, 1000);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void performSearch(String query) {
        filteredBills.clear();

        if (query.isEmpty()) {
            filteredBills.addAll(allBills);
        } else {
            for (MilkCollection bill : allBills) {
                if (bill.trDate != null) {
                    String billDate = bill.trDate.split(" ")[0];
                    if (billDate.equals(query)) {
                        filteredBills.add(bill);
                    }
                }
            }
        }

        if (filteredBills.isEmpty()) {
            showNoRecords();
        } else {
            showBills();
        }

        adapter.notifyDataSetChanged();
    }

    private void showNoRecords() {
        tvNoRecords.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showBills() {
        tvNoRecords.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void onBillClick(MilkCollection milkCollection) {
        String customerName = customerMap.getOrDefault(milkCollection.partyCode, milkCollection.partyCode);

        Intent intent = new Intent(this, BillActivity.class);
        intent.putExtra("customerNo", milkCollection.partyCode);
        intent.putExtra("name", customerName);
        intent.putExtra("liters", milkCollection.qty);
        intent.putExtra("fat", milkCollection.fat);
        intent.putExtra("snf", milkCollection.snf);
        intent.putExtra("rate", milkCollection.rate);
        intent.putExtra("total", milkCollection.amt);
        intent.putExtra("date", milkCollection.trDate.split(" ")[0]);
        intent.putExtra("time", milkCollection.time);
        intent.putExtra("timePeriod", milkCollection.timePeriod);
        intent.putExtra("milkType", milkCollection.mlkTypeCode);
        startActivity(intent);
    }

    private void onBillEditClick(MilkCollection milkCollection) {
        Intent intent = new Intent(this, UpdateBillActivity.class);
        intent.putExtra("milkCollectionId", milkCollection.id);
        startActivity(intent);
    }
}
