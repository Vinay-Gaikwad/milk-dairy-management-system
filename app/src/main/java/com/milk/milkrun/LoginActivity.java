package com.milk.milkrun;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.milk.milkrun.api2.ApiService;
import com.milk.milkrun.api2.LoginRequest;
import com.milk.milkrun.api2.LoginResponse;
import com.milk.milkrun.api2.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etSoftCustNumber, etMob;
    private Button btnLogin;
    private ApiService apiService;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etSoftCustNumber = findViewById(R.id.etSoftCustNumber);
        etMob = findViewById(R.id.etMob);
        btnLogin = findViewById(R.id.btnLogin);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        btnLogin.setOnClickListener(v -> {
            String code = etSoftCustNumber.getText().toString().trim();
            String mobile = etMob.getText().toString().trim();

            Log.d(TAG, "Login button clicked");
            Log.d(TAG, "Entered SoftCustCode: " + code);
            Log.d(TAG, "Entered Mobile: " + mobile);

            if (code.isEmpty() || mobile.isEmpty()) {
                Toast.makeText(this, "कृपया कोड व मोबाईल नंबर भरा", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(code, mobile);
        });
    }

    private void loginUser(String code, String mobile) {
        LoginRequest request = new LoginRequest(code, mobile);
        Log.d(TAG, "Sending API request...");

        Call<LoginResponse> call = apiService.getPartyDetails(request);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                Log.d(TAG, "API response received");

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Response body: " + new Gson().toJson(response.body()));

                    if (response.body().getStatus()) {
                        Toast.makeText(LoginActivity.this, "लॉगिन यशस्वी!", Toast.LENGTH_SHORT).show();

                        AlertDialog loadingDialog = new AlertDialog.Builder(LoginActivity.this)
                                .setView(LayoutInflater.from(LoginActivity.this).inflate(R.layout.dialog_loading, null))
                                .setCancelable(false)
                                .create();
                        loadingDialog.show();

                        new Handler().postDelayed(() -> {
                            loadingDialog.dismiss();

                            LoginResponse.LoginData userData = response.body().getData().get(0);

                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("SoftCustCode", code); // entered manually
                            editor.putString("Mobile", userData.getMob()); // from API
                            editor.putString("PartyCode", userData.getPartyCode());
                            editor.putString("PartyNameOth", userData.getPartyNameOth());
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();
                        }, 1500);

                    } else {
                        Toast.makeText(LoginActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Login failed: " + response.body().getMessage());
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "चुकीचे तपशील किंवा सर्व्हर त्रुटी", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Response unsuccessful or body is null");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "त्रुटी: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "API call failed", t);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            android.view.View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
