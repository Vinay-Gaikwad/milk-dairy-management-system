package com.milk.milkrun;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3500;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        VideoView videoView = findViewById(R.id.videoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.final_splash);
        videoView.setVideoURI(videoUri);
        videoView.start();
        videoView.setOnCompletionListener(mp -> videoView.start());

        handler.postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String code = prefs.getString("SoftCustCode", null);
            String mobile = prefs.getString("Mobile", null);

            if (code != null && mobile != null) {
                // 🔄 Load last screen
                SharedPreferences appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                String lastActivity = appPrefs.getString("lastActivity", "DashboardActivity");

                Intent intent;
                switch (lastActivity) {
                    case "EntryActivity":
                        intent = new Intent(this, EntryActivity.class);
                        break;
                    case "BillListActivity":
                        intent = new Intent(this, BillListActivity.class);
                        break;
                    case "UpdateBillActivity":
                        intent = new Intent(this, UpdateBillActivity.class);
                        break;
                    default:
                        intent = new Intent(this, DashboardActivity.class);
                        break;
                }
                startActivity(intent);
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }

            finish();
        }, SPLASH_TIME_OUT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
