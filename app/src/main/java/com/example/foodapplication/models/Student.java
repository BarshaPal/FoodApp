package com.example.foodapplication.models;

public class Student {
    private String id;         // Unique ID for the student
    private String name;       // Student's name
    private double balance;    // Balance or total amount the student needs to pay

    // Default constructor (required for Firebase)
    public Student() {}

    // Parameterized constructor
    public Student(String id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}
