// Java: NewCustomerRegistrationActivity.java (Marathi localized)
package com.milk.milkrun;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.milk.milkrun.customerdatabase.Customer;
import com.milk.milkrun.customerdatabase.CustomerDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewCustomerRegistrationActivity extends AppCompatActivity {

    EditText etCustomerName, etCustomerNo, etCustomerAddress, etCustomerMobile, etDate;
    MaterialButton btnRegister, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_customer_registration);

        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerNo = findViewById(R.id.etCustomerNo);
        etCustomerAddress = findViewById(R.id.etCustomerAddress);
        etCustomerMobile = findViewById(R.id.etCustomerMobile);
        etDate = findViewById(R.id.etDate);
        btnRegister = findViewById(R.id.btnRegister);
        btnCancel = findViewById(R.id.btnCancel);
        LinearLayout btnHome = findViewById(R.id.btnHomeContainer);
        LinearLayout btnViewCustomers = findViewById(R.id.btnViewCustomersContainer);

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
        }
        if (btnViewCustomers != null) {
            btnViewCustomers.setOnClickListener(v -> startActivity(new Intent(this, CustomerListActivity.class)));
        }

        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        etDate.setText("दिनांक: " + currentDate);

        btnRegister.setOnClickListener(v -> showConfirmationDialog());

        btnCancel.setOnClickListener(v -> {
            Intent intent = new Intent(NewCustomerRegistrationActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void saveCustomerToDatabase() {
        String name = etCustomerName.getText().toString().trim();
        String number = etCustomerNo.getText().toString().trim();
        String address = etCustomerAddress.getText().toString().trim();
        String mobile = etCustomerMobile.getText().toString().trim();
        String date = etDate.getText().toString().replace("दिनांक: ", "").trim();

        if (name.isEmpty() || number.isEmpty() || address.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this, "कृपया सर्व माहिती भरा", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            CustomerDatabase db = CustomerDatabase.getInstance(getApplicationContext());
            Customer existingCustomer = db.customerDao().getCustomerByNumber(number);

            if (existingCustomer != null) {
                runOnUiThread(() ->
                        Toast.makeText(NewCustomerRegistrationActivity.this,
                                "हा ग्राहक क्रमांक आधीच वापरला आहे. कृपया दुसरा क्रमांक वापरा.",
                                Toast.LENGTH_LONG).show()
                );
            } else {
                Customer customer = new Customer();
                customer.name = name;
                customer.number = number;
                customer.address = address;
                customer.mobile = mobile;
                customer.date = date;

                db.customerDao().insert(customer);
                runOnUiThread(() -> {
                    Toast.makeText(NewCustomerRegistrationActivity.this, "ग्राहक नोंदणी यशस्वी!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(NewCustomerRegistrationActivity.this, DashboardActivity.class));
                    finish();
                });
            }
        }).start();
    }
    private void showConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("पुष्टी करा")
                .setMessage("तुम्हाला ग्राहक नोंदवायचा आहे का?")
                .setPositiveButton("हो", (dialog, which) -> saveCustomerToDatabase())
                .setNegativeButton("नाही", (dialog, which) -> dialog.dismiss())
                .show();
    }

}