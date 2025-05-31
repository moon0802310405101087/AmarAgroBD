package com.moon.farmingbd;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NewOrdersAdapter extends RecyclerView.Adapter<NewOrdersAdapter.ViewHolder> {

    private final List<Order> newOrders;

    public NewOrdersAdapter(List<Order> newOrders) {
        this.newOrders = newOrders;
    }

    @NonNull
    @Override
    public NewOrdersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewOrdersAdapter.ViewHolder holder, int position) {
        Order order = newOrders.get(position);

        holder.tvProductName.setText(order.getProductName());
        holder.tvQuantity.setText("Quantity: " + order.getQuantity());
        holder.tvTotalPrice.setText("Total Price: " + order.getTotalPrice() + " BDT");
        String location = order.getDivision() + ", " + order.getDistrict() + ", " + order.getCity();
        holder.tvLocation.setText("Location: " + location);
        holder.tvStatus.setText("Status: " + order.getOrderStatus());
    }

    @Override
    public int getItemCount() {
        return newOrders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvQuantity, tvTotalPrice, tvLocation, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
