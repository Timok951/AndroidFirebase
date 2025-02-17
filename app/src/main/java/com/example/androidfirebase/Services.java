package com.example.androidfirebase;

public class Services {
    private String name;
    private String description;
    private int price;


    public Services(String name, String description, int price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    // Геттеры
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }
}
