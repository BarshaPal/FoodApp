package com.example.foodapplication.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.foodapplication.R;

public class PaymentActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private double amountDue;
    private String studentId;
    private TextView totalTextView;
    private Button completePaymentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        db = FirebaseFirestore.getInstance();
        completePaymentButton = findViewById(R.id.completePaymentButton);
        totalTextView = findViewById(R.id.totalTextView);

        // Get the amount due and student ID from intent
        amountDue = getIntent().getDoubleExtra("amountDue", 0);
        studentId = getIntent().getStringExtra("studentId");

        // Set the amount due to totalTextView as text
        totalTextView.setText("Total Payment Due: ₹" + amountDue);

        completePaymentButton.setOnClickListener(v -> startUPIPayment(amountDue, studentId));
    }

    private void startUPIPayment(double amount, String studentId) {
        String payeeVPA = "example@upi";  // Replace with your UPI ID (for testing, use a test ID)
        String note = "Meal Payment for Student ID: " + studentId;  // Optional note

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", payeeVPA)      // Payee VPA (UPI ID)
                .appendQueryParameter("pn", "FoodApp")     // Payee Name (Optional)
                .appendQueryParameter("am", String.valueOf(amount))        // Amount
                .appendQueryParameter("cu", "INR")         // Currency (INR)
                .appendQueryParameter("note", note)        // Note or Message
                .build();

        Intent upiPaymentIntent = new Intent(Intent.ACTION_VIEW);
        upiPaymentIntent.setData(uri);

        // Try to open UPI apps
        Intent chooser = Intent.createChooser(upiPaymentIntent, "Pay with");
        if (chooser.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chooser, 1);  // Start UPI payment
        } else {
            Toast.makeText(this, "No UPI app found", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle UPI response in onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String response = data.getStringExtra("response");
                    handleUPIResponse(response);
                }
            }
        }
    }

    // Function to handle UPI response
    private void handleUPIResponse(String response) {
        if (response != null) {
            String status = "";
            String[] responseArray = response.split("&");

            for (String res : responseArray) {
                String[] resParts = res.split("=");
                if (resParts[0].equalsIgnoreCase("Status")) {
                    status = resParts[1];
                    break;
                }
            }

            if (status.equals("SUCCESS")) {
                // If payment is successful, update the student's dueAmount in Firestore
                updateStudentPaymentStatus();
            } else if (status.equals("FAILURE")) {
                Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Payment Status Unknown", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No Response from UPI", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStudentPaymentStatus() {
        db.collection("students").document(studentId)
                .update("dueAmount", 0) // Reset the due amount after successful payment
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();

                    // Return the result to the calling activity (e.g., AddMealActivity)
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("paymentSuccessful", true);
                    setResult(RESULT_OK, resultIntent);

                    finish(); // Close the PaymentActivity
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update due amount", Toast.LENGTH_SHORT).show());
    }
}
