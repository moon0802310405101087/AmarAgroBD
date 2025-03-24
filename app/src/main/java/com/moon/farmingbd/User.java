package com.moon.farmingbd;

public class User {
    private String email;
    private String role;

    // Constructor
    public User(String email, String role) {
        this.email = email;
        this.role = role;
    }

    // Getters and setters (if needed)
    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
