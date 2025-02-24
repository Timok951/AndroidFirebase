package com.example.androidfirebase;

public class Services {
    private String name;
    private String description;
    private int cost;  // Используем "cost" вместо "price"
    private String category;

    // Пустой конструктор для Firestore
    public Services() {
    }

    public Services(String name, String description, int cost, String category) {
        this.name = name;
        this.description = description;
        this.cost = cost; // Обновляем поле cost
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCost() {  // Геттер для cost
        return cost;
    }

    public String getCategory() {
        return category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCost(int cost) {  // Сеттер для cost
        this.cost = cost;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

