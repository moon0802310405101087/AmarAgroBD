package com.moon.farmingbd;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends ArrayAdapter<Order> {

    private final Context context;
    private final List<Order> orders;

    public OrderHistoryAdapter(Context context, List<Order> orders) {
        super(context, 0, orders);
        this.context = context;
        this.orders = orders;


        Collections.sort(this.orders, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return Long.compare(o2.getTimestamp(), o1.getTimestamp());
            }
        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Order order = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.order_history_item, parent, false);
        }

        TextView productNameText = convertView.findViewById(R.id.productNameText);
        TextView quantityText = convertView.findViewById(R.id.quantityText);
        TextView statusText = convertView.findViewById(R.id.statusText);
        TextView totalPriceText = convertView.findViewById(R.id.totalPriceText);
        TextView orderDateText = convertView.findViewById(R.id.orderDate);
        TextView deliveryDateText = convertView.findViewById(R.id.deliveryDate);
        Button cancelButton = convertView.findViewById(R.id.cancelOrderBtn);

        productNameText.setText("Product: " + order.getProductName());
        quantityText.setText("Quantity: " + order.getQuantity());
        statusText.setText("Status: " + order.getOrderStatus());
        totalPriceText.setText("Total Price: ৳" + order.getTotalPrice());

        long orderTimestamp = order.getTimestamp();
        if (orderTimestamp > 0) {
            Date date = new Date(orderTimestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            String formattedOrderDate = sdf.format(date);
            orderDateText.setText("Ordered on: " + formattedOrderDate);
            orderDateText.setVisibility(View.VISIBLE);
        } else {
            orderDateText.setVisibility(View.GONE);
        }


        long deliveryTimestamp = order.getDeliveryTimestamp();
        if (deliveryTimestamp > 0) {
            Date deliveryDate = new Date(deliveryTimestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            String formattedDeliveryDate = sdf.format(deliveryDate);
            deliveryDateText.setText("Delivered on: " + formattedDeliveryDate);
            deliveryDateText.setVisibility(View.VISIBLE);
        } else {
            deliveryDateText.setVisibility(View.GONE);
        }

        if ("Cancelled".equalsIgnoreCase(order.getOrderStatus())) {
            cancelButton.setText("Cancelled");
            cancelButton.setEnabled(false);
        } else if ("Delivered".equalsIgnoreCase(order.getOrderStatus())) {
            cancelButton.setVisibility(View.GONE);
        } else {
            cancelButton.setText("Cancel");
            cancelButton.setEnabled(true);
            cancelButton.setVisibility(View.VISIBLE);
        }

        cancelButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Cancel Order")
                    .setMessage("Are you sure you want to cancel this order?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseDatabase.getInstance().getReference("orders")
                                .child(order.getId())
                                .child("orderStatus")
                                .setValue("Cancelled")
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Order cancelled successfully", Toast.LENGTH_SHORT).show();
                                    order.setOrderStatus("Cancelled");
                                    notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to cancel order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        return convertView;
    }
}
