package com.moon.farmingbd;

public class Order {
    private String id;
    private String productId;
    private String productName;
    private String ownerId;
    private String customerId;
    private int quantity;
    private String division;
    private String district;
    private String city;
    private String address;
    private String phone;
    private String orderStatus;
    private long timestamp;
    private long deliveryTimestamp;
    private double totalPrice;
    private String deliveryManEmail;

    public Order() {

    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getDeliveryTimestamp() {
        return deliveryTimestamp;
    }

    public void setDeliveryTimestamp(long deliveryTimestamp) {
        this.deliveryTimestamp = deliveryTimestamp;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getDeliveryManEmail() {
        return deliveryManEmail;
    }

    public void setDeliveryManEmail(String deliveryManEmail) {
        this.deliveryManEmail = deliveryManEmail;
    }

    @Override
    public String toString() {
        return "Order ID: " + id + ", Product: " + productName + ", Quantity: " + quantity +
                ", Total Price: " + totalPrice + ", Status: " + orderStatus;
    }
}
