package com.milk.milkrun;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BillActivity extends AppCompatActivity {

    private TextView tvFirmName, tvDate, tvTime, tvShift, tvCustomerNo, tvMilk, tvFatSnf, tvRateTotal;
    private FloatingActionButton btnSharePdf, btnPrint;
    private File pdfFile;

    private String customerNo, name, date, time, timePeriod, milkType;
    private double liters, fat, snf, rate, total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        // Initialize views
        initializeViews();

        // Get data from intent
        getIntentData();

        // Convert to readable formats
        milkType = "1".equals(milkType) ? "म्हैस" : "गाय";
        String shift = determineShift();

        // Format time
        String formattedTime = formatTime(time);

        // Set all text views
        setTextViews(shift, formattedTime);

        // Set up button click listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        tvFirmName = findViewById(R.id.tvFirmName);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvShift = findViewById(R.id.tvShift);
        tvCustomerNo = findViewById(R.id.tvCustomerNo);
        tvMilk = findViewById(R.id.tvMilk);
        tvFatSnf = findViewById(R.id.tvFatSnf);
        tvRateTotal = findViewById(R.id.tvRateTotal);
        btnSharePdf = findViewById(R.id.btnSharePdf);
        btnPrint = findViewById(R.id.btnPrint);

        // Navigation buttons
        LinearLayout btnHome = findViewById(R.id.btnHomeContainer);
        LinearLayout btnViewCustomers = findViewById(R.id.btnViewCustomersContainer);

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
        }
        if (btnViewCustomers != null) {
            btnViewCustomers.setOnClickListener(v -> startActivity(new Intent(this, EntryActivity.class)));
        }
    }

    private void getIntentData() {
        Intent intent = getIntent();
        customerNo = intent.getStringExtra("customerNo");
        name = intent.getStringExtra("name");
        date = intent.getStringExtra("date");
        time = intent.getStringExtra("time");
        timePeriod = intent.getStringExtra("timePeriod");
        milkType = intent.getStringExtra("milkType");
        liters = intent.getDoubleExtra("liters", 0);
        fat = intent.getDoubleExtra("fat", 0);
        snf = intent.getDoubleExtra("snf", 0);
        rate = intent.getDoubleExtra("rate", 0);
        total = intent.getDoubleExtra("total", 0);

        Log.d("BILL_DEBUG", "Received timePeriod: " + timePeriod);
    }

    private String determineShift() {
        if (timePeriod != null) {
            // Check for both English and Marathi evening indicators
            if (timePeriod.toLowerCase().contains("evening") ||
                    timePeriod.toLowerCase().contains("संध्याकाळ")) {
                return "सायंकाळ";
            }
        }
        return "सकाळ";
    }

    private String formatTime(String time) {
        try {
            SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date dateObj = sdf24.parse(time);
            if (dateObj != null) {
                return sdf12.format(dateObj);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    private void setTextViews(String shift, String formattedTime) {
        tvFirmName.setText("दूध संस्था ढवळेवाडी");
        tvDate.setText("दिनांक : " + date.split(" ")[0]);
        tvTime.setText("वेळ : " + formattedTime);
        tvShift.setText("शिफ्ट : " + shift);
        tvCustomerNo.setText("सभासद : " + customerNo + " " + name);
        tvMilk.setText("दूध : " + liters + "         " + milkType);
        tvFatSnf.setText("फॅट : " + fat + "         SNF: " + snf);
        tvRateTotal.setText("दर  : ₹" + rate + "        रक्कम : ₹" + total);
    }

    private void setupButtonListeners() {
        btnSharePdf.setOnClickListener(v -> {
            try {
                createPdfFile();
                sharePdf();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        btnPrint.setOnClickListener(v -> {
            try {
                createPdfFile();
                printPdf();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void createPdfFile() throws IOException {
        int pageWidth = 150; // 2-inch width
        int pageHeight = 100; // Compact height

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        paint.setTextSize(8f);

        Paint boldCenter = new Paint();
        boldCenter.setTextAlign(Paint.Align.CENTER);
        boldCenter.setTextSize(8f);
        boldCenter.setFakeBoldText(true);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int centerX = pageWidth / 2;
        int y = 12;

        // Header
        canvas.drawText("दूध संस्था ढवळेवाडी", centerX, y, boldCenter);
        y += 10;

        // Date, Time, Shift
        String cleanTime = tvTime.getText().toString().replace("वेळ :", "").trim();
        String cleanShift = tvShift.getText().toString().replace("शिफ्ट :", "").trim();
        canvas.drawText("दिनांक: " + date.split(" ")[0], 10, y, paint); y += 9;
        canvas.drawText("वेळ: " + cleanTime, 10, y, paint); y += 9;
        canvas.drawText("शिफ्ट: " + cleanShift, 10, y, paint); y += 9;

        // Customer
        canvas.drawText("सभासद: " + customerNo + " " + name, 10, y, paint); y += 9;

        // Milk Data
        canvas.drawText("दूध: " + liters + "     " + milkType, 10, y, paint); y += 9;
        canvas.drawText("फॅट: " + fat + "     SNF: " + snf, 10, y, paint); y += 9;
        canvas.drawText("दर: ₹" + rate + "     रक्कम: ₹" + total, 10, y, paint); y += 12;

        // Footer
        boldCenter.setFakeBoldText(true);
        canvas.drawText("धन्यवाद!", centerX, y, boldCenter);

        pdfDocument.finishPage(page);

        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Bills");
        if (!dir.exists()) dir.mkdirs();

        pdfFile = new File(dir, "Milk Bill.pdf");
        pdfDocument.writeTo(new FileOutputStream(pdfFile));
        pdfDocument.close();
    }

    private void sharePdf() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                pdfFile
        ));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "PDF शेअर करा"));
    }

    private void printPdf() {
        PrintManager printManager = (PrintManager) getSystemService(PRINT_SERVICE);
        if (printManager != null) {
            PrintAttributes attributes = new PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.NA_INDEX_3X5)
                    .build();

            PrintHelperAdapter adapter = new PrintHelperAdapter(this, pdfFile);
            printManager.print("दूध पावती", adapter, attributes);
        }
    }
}