package com.example.foodapplication.models;

public class Student {
    private String id;
    private String name;
    private double amount;
    private double advancePaid;
    private double avaialableBalance;
    private String phoneNumber ;


    public Student() {
        // Default constructor required for Firebase
    }

    public Student(String id, String name, double amount, double advancePaid, double avaialableBalance, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.advancePaid = advancePaid;
        this.avaialableBalance = avaialableBalance;
        this.phoneNumber = phoneNumber; // Initialize new field
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public double getAdvancePaid() {
        return advancePaid;
    }

    public double getAvaialableBalance() {
        return avaialableBalance;
    }

    public void setAvaialableBalance(double avaialableBalance) {
        this.avaialableBalance = avaialableBalance;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
