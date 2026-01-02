package com.milk.milkrun;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.milk.milkrun.customerdatabase.Customer;
import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {

    private List<Customer> fullList;
    private List<Customer> filteredList;
    private Context context;

    public CustomerAdapter(Context context, List<Customer> customerList) {
        this.context = context;
        this.fullList = customerList;
        this.filteredList = new ArrayList<>(customerList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_customer_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Customer customer = filteredList.get(position);
        holder.name.setText("ग्राहकाचे नाव: " + customer.name);
        holder.number.setText("क्रमांक: " + customer.number);
        holder.mobile.setText("मोबाईल: " + customer.mobile);
        holder.address.setText("पत्ता: " + customer.address);

    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            query = query.toLowerCase();
            for (Customer customer : fullList) {
                if (customer.name.toLowerCase().contains(query) || customer.number.contains(query)) {
                    filteredList.add(customer);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, number, mobile, address;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvCustomerName);
            number = itemView.findViewById(R.id.tvCustomerNumber);
            mobile = itemView.findViewById(R.id.tvCustomerMobile);
            address = itemView.findViewById(R.id.tvCustomerAddress);
        }
    }
}
