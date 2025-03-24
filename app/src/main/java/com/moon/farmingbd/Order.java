package com.moon.farmingbd;

public class Order {
    private String orderId;
    private String productName;
    private int quantity;
    private double totalPrice;

    public Order() {
        // Default constructor required for Firebase
    }

    public Order(String orderId, String productName, int quantity, double totalPrice) {
        this.orderId = orderId;
        this.productName = productName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    @Override
    public String toString() {
        return "Order ID: " + orderId + "\nProduct: " + productName + "\nQuantity: " + quantity + "\nTotal Price: $" + totalPrice;
    }
}
