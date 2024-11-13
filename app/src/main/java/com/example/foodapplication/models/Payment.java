package com.example.foodapplication.models;

import java.util.Date;

public class Payment {
    private String studentId;   // ID of the student who made the payment
    private Date paymentDate;   // Date of the payment
    private double amount;      // Amount paid

    // Default constructor (required for Firebase)
    public Payment() {}

    // Parameterized constructor
    public Payment(String studentId, Date paymentDate, double amount) {
        this.studentId = studentId;
        this.paymentDate = paymentDate;
        this.amount = amount;
    }

    // Getters and setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
