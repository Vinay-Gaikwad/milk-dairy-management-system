package com.milk.milkrun;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.milk.milkrun.collectiondatabase.MilkCollection;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class BillListAdapter extends RecyclerView.Adapter<BillListAdapter.BillViewHolder> {

    private List<MilkCollection> billList;
    private BillClickListener clickListener;
    private BillEditClickListener editListener;
    private Context context;
    private HashMap<String, String> customerMap;

    public interface BillClickListener {
        void onBillClick(MilkCollection milkCollection);
    }

    public interface BillEditClickListener {
        void onEditBillClick(MilkCollection milkCollection);
    }

    public BillListAdapter(List<MilkCollection> billList,
                           BillClickListener clickListener,
                           BillEditClickListener editListener,
                           HashMap<String, String> customerMap) {
        this.billList = billList;
        this.clickListener = clickListener;
        this.editListener = editListener;
        this.customerMap = customerMap;
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_bill, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        MilkCollection bill = billList.get(position);
        String customerName = customerMap.getOrDefault(bill.partyCode, "अज्ञात ग्राहक");

        // 🟢 Marathi labels
        holder.tvCustomerNo.setText("ग्राहक क्रमांक: " + bill.partyCode);
        holder.tvCustomerName.setText("ग्राहकाचे नाव: " + customerName);
        holder.tvMilkType.setText("दूध प्रकार: " + (bill.mlkTypeCode.equals("1") ? "म्हैस" : "गाय"));
        holder.tvDate.setText("तारीख: " + (bill.trDate != null ? bill.trDate.split(" ")[0] : ""));

        // Fixed shift display logic
        String shiftText = "शिफ्ट: ";
        if (bill.timePeriod != null) {
            // Check if timePeriod contains morning or evening
            if (bill.timePeriod.startsWith("सकाळ")) {
                shiftText += "सकाळ";
            } else if (bill.timePeriod.startsWith("संध्याकाळ")) {
                shiftText += "संध्याकाळ";
            } else {
                // Fallback for unexpected formats
                shiftText += bill.timePeriod;
            }
        } else {
            shiftText += "संध्याकाळ"; // Default if null
        }
        holder.tvShift.setText(shiftText);

        // Format liters to 2 decimal places
        double liters = bill.qty;
        holder.tvLiters.setText(String.format(Locale.getDefault(), "लिटर: %.2f", liters));

        holder.tvTotal.setText("एकूण रक्कम: ₹" + bill.amt);

        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onBillClick(bill);
            }
        });

        holder.ivMoreOptions.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.ivMoreOptions);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.menu_bill_item, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit) {
                    if (editListener != null) {
                        editListener.onEditBillClick(bill);
                    }
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return billList.size();
    }

    static class BillViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvCustomerNo, tvCustomerName, tvMilkType, tvDate, tvShift, tvLiters, tvTotal;
        ImageView ivMoreOptions;

        BillViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewBill);
            tvCustomerNo = itemView.findViewById(R.id.tvCustomerNo);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvMilkType = itemView.findViewById(R.id.tvMilkType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvShift = itemView.findViewById(R.id.tvShift);
            tvLiters = itemView.findViewById(R.id.tvLiters);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            ivMoreOptions = itemView.findViewById(R.id.ivMoreOptions);
        }
    }
}