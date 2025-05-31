package com.moon.farmingbd;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DeliveredOrdersAdapter extends RecyclerView.Adapter<DeliveredOrdersAdapter.ViewHolder> {

    private final ArrayList<Order> orders;

    public DeliveredOrdersAdapter(ArrayList<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public DeliveredOrdersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delivered_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveredOrdersAdapter.ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.productName.setText("Product: " + order.getProductName());
        holder.quantity.setText("Quantity: " + order.getQuantity());
        holder.totalPrice.setText("Total Price: " + order.getTotalPrice());
        holder.status.setText("Status: " + order.getOrderStatus());
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, quantity, totalPrice, status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productNameText);
            quantity = itemView.findViewById(R.id.quantityText);
            totalPrice = itemView.findViewById(R.id.totalPriceText);
            status = itemView.findViewById(R.id.statusText);
        }
    }
}