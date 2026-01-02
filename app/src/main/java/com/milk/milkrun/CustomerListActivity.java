package com.milk.milkrun;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.milk.milkrun.customerdatabase.Customer;
import com.milk.milkrun.customerdatabase.CustomerDatabase;

import java.util.List;

public class CustomerListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CustomerAdapter adapter;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);

        recyclerView = findViewById(R.id.customerRecyclerView);
        etSearch = findViewById(R.id.etSearch);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        LinearLayout btnHome = findViewById(R.id.btnHomeContainer);
        LinearLayout btnViewCustomers = findViewById(R.id.btnViewCustomersContainer);

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
        }
        if (btnViewCustomers != null) {
            btnViewCustomers.setOnClickListener(v -> startActivity(new Intent(this, BillListActivity.class)));
        }

        new Thread(() -> {
            List<Customer> customers = CustomerDatabase.getInstance(this).customerDao().getAllCustomers();

            runOnUiThread(() -> {
                adapter = new CustomerAdapter(this, customers);
                recyclerView.setAdapter(adapter);

                etSearch.setHint("नाव किंवा क्रमांकाने शोधा");
                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        adapter.filter(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            });
        }).start();
    }
}
