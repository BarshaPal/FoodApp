package com.example.foodapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.example.foodapplication.R;
import com.google.firebase.auth.FirebaseAuth;


import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapplication.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AdminActivity extends AppCompatActivity {

    private TextView textViewTotalAdvance, textViewAmountLeft;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        textViewTotalAdvance = findViewById(R.id.textView_total_advance);
        textViewAmountLeft = findViewById(R.id.textView_amount_left);

        db = FirebaseFirestore.getInstance();

        calculateTotals();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            logoutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void logoutUser() {
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, AuthenticationActivity.class));
        finish();
    }

    private void calculateTotals() {
        db.collection("userList").get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        double totalAdvance = 0.0;
                        double amountLeft = 0.0;

                        // Iterate through all documents
                        for (var document : querySnapshot) {
                            Double advancePaid = document.getDouble("advancePaid");
                            Double availableBalance = document.getDouble("availableBalance");

                            if (advancePaid != null) totalAdvance += advancePaid;
                            if (availableBalance != null) amountLeft += availableBalance;
                        }

                        // Update UI with calculated values
                        textViewTotalAdvance.setText("Total Advance Received: " + totalAdvance);
                        textViewAmountLeft.setText("Amount Left: " + amountLeft);
                    } else {
                        Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

