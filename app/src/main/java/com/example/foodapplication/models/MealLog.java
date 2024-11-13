package com.example.foodapplication.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MealLog {
    private String studentId;   // ID of the student who consumed the meal
    private Date date;          // Date of the meal
    private String mealType;    // Type of meal (e.g., Breakfast, Lunch, Dinner)
    private double price;       // Price of the meal

    // Default constructor (required for Firebase)
    public MealLog() {}

    // Parameterized constructor
    public MealLog(String studentId, Date date, String mealType, double price) {
        this.studentId = studentId;
        this.date = date;
        this.mealType = mealType;
        this.price = price;
    }

    // Getters and setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    // toMap function to convert MealLog object to a Map for Firebase or other databases
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("studentId", studentId);
        map.put("date", date);
        map.put("mealType", mealType);
        map.put("price", price);
        return map;
    }
}
