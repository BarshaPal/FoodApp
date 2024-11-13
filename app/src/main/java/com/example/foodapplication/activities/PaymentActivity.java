package com.example.foodapplication.activities;

import android.content.Intent;
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

        completePaymentButton.setOnClickListener(v -> completePayment(studentId));
    }

    private void completePayment(String studentId) {
        db.collection("students").document(this.studentId)
                .update("dueAmount", 0)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();

                    // Return the result to AddMealActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("paymentSuccessful", true);
                    setResult(RESULT_OK, resultIntent);

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update due amount", Toast.LENGTH_SHORT).show());
    }

}
