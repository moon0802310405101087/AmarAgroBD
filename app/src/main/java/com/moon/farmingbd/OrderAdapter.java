package com.moon.farmingbd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends ArrayAdapter<Order> {
    private final Context context;
    private final List<Order> orders;
    private final String userRole;

    public OrderAdapter(Context context, List<Order> orders, String userRole) {
        super(context, 0, orders);
        this.context = context;
        this.orders = orders;
        this.userRole = userRole;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Order order = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.order_item, parent, false);
        }


        TextView productNameText = convertView.findViewById(R.id.productNameText);
        TextView quantityText = convertView.findViewById(R.id.quantityText);
        TextView divisionText = convertView.findViewById(R.id.divisionText);
        TextView districtText = convertView.findViewById(R.id.districtText);
        TextView cityText = convertView.findViewById(R.id.cityText);
        TextView addressText = convertView.findViewById(R.id.addressText);
        TextView phoneText = convertView.findViewById(R.id.phoneText);
        TextView statusText = convertView.findViewById(R.id.statusText);
        TextView totalPriceText = convertView.findViewById(R.id.totalPriceText);
        TextView orderTimeText = convertView.findViewById(R.id.orderTimeText);
        TextView deliveryTimeText = convertView.findViewById(R.id.deliveryTimeText);

        EditText deliveryManEmailInput = convertView.findViewById(R.id.deliveryManEmailInput);
        Button assignDeliveryManBtn = convertView.findViewById(R.id.assignDeliveryManBtn);
        Button markDeliveredBtn = convertView.findViewById(R.id.markDeliveredBtn);


        productNameText.setText("Product: " + order.getProductName());
        quantityText.setText("Quantity: " + order.getQuantity());
        divisionText.setText("Division: " + order.getDivision());
        districtText.setText("District: " + order.getDistrict());
        cityText.setText("City: " + order.getCity());
        addressText.setText("Address: " + order.getAddress());
        phoneText.setText("Phone: " + order.getPhone());
        statusText.setText("Status: " + order.getOrderStatus());
        totalPriceText.setText("Total Price: " + order.getTotalPrice());

        long timestamp = order.getTimestamp();
        if (timestamp > 0) {
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            orderTimeText.setText("Ordered at: " + sdf.format(date));
            orderTimeText.setVisibility(View.VISIBLE);
        } else {
            orderTimeText.setVisibility(View.GONE);
        }

        long deliveryTimestamp = order.getDeliveryTimestamp();
        if (deliveryTimestamp > 0) {
            Date date = new Date(deliveryTimestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            deliveryTimeText.setText("Delivered at: " + sdf.format(date));
            deliveryTimeText.setVisibility(View.VISIBLE);
        } else {
            deliveryTimeText.setVisibility(View.GONE);
        }

        deliveryManEmailInput.setVisibility(View.GONE);
        assignDeliveryManBtn.setVisibility(View.GONE);
        markDeliveredBtn.setVisibility(View.GONE);

        if ("owner".equals(userRole)) {

            if ("Cancelled".equalsIgnoreCase(order.getOrderStatus())) {
                statusText.setText("Status: Cancelled (No delivery assignment allowed)");

            } else if ("Delivered".equalsIgnoreCase(order.getOrderStatus())) {

            } else {

                deliveryManEmailInput.setVisibility(View.VISIBLE);
                assignDeliveryManBtn.setVisibility(View.VISIBLE);

                if (order.getDeliveryManEmail() != null) {
                    deliveryManEmailInput.setText(order.getDeliveryManEmail());
                } else {
                    deliveryManEmailInput.setText("");
                }

                assignDeliveryManBtn.setOnClickListener(v -> {
                    String email = deliveryManEmailInput.getText().toString().trim();
                    if (!email.isEmpty()) {
                        order.setDeliveryManEmail(email);
                        FirebaseDatabase.getInstance().getReference("orders")
                                .child(order.getId())
                                .child("deliveryManEmail")
                                .setValue(email)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(context, "Deliveryman assigned.", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(context, "Enter deliveryman email.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else if ("delivery".equals(userRole)) {

            if (!"Delivered".equalsIgnoreCase(order.getOrderStatus()) &&
                    !"Cancelled".equalsIgnoreCase(order.getOrderStatus())) {
                markDeliveredBtn.setVisibility(View.VISIBLE);
            }

            markDeliveredBtn.setOnClickListener(v -> {
                order.setOrderStatus("Delivered");
                long deliveryTime = System.currentTimeMillis();
                order.setDeliveryTimestamp(deliveryTime);

                FirebaseDatabase.getInstance().getReference("orders")
                        .child(order.getId())
                        .child("orderStatus")
                        .setValue("Delivered");

                FirebaseDatabase.getInstance().getReference("orders")
                        .child(order.getId())
                        .child("deliveryTimestamp")
                        .setValue(deliveryTime)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(context, "Marked as Delivered", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });
        }

        return convertView;
    }
}
