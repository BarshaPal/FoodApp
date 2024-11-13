package com.example.foodapplication.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.foodapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddMealActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private Button breakfastButton, lunchButton, dinnerButton, viewPaymentButton, detailPaymentButton;
    private TextView dueAmountTextView;
    private String studentId ; // Replace with actual student ID

    // Step 1: Define the ActivityResultLauncher to handle result from PaymentActivity
    private final ActivityResultLauncher<Intent> paymentLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getBooleanExtra("paymentSuccessful", false)) {
                        // Update due amount display if payment was successful
                        updateDueAmountDisplay();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            studentId = currentUser.getUid();

        } else {
            // Handle the case where the user is not authenticated
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
            return;
        }
        // Initialize UI elements
        breakfastButton = findViewById(R.id.breakfastButton);
        lunchButton = findViewById(R.id.lunchButton);
        dinnerButton = findViewById(R.id.dinnerButton);
        viewPaymentButton = findViewById(R.id.viewPaymentButton);
        detailPaymentButton = findViewById(R.id.DetailPaymentButton);
        dueAmountTextView = findViewById(R.id.dueAmountTextView);
        updateDueAmountDisplay();
        // Setup button click listeners
        breakfastButton.setOnClickListener(v -> logMeal("Breakfast", 30));
        lunchButton.setOnClickListener(v -> logMeal("Lunch", 50));
        dinnerButton.setOnClickListener(v -> logMeal("Dinner", 40));
        detailPaymentButton.setOnClickListener(v -> {
            String currentDate = getCurrentDateInLocaleFormat();
            fetchMealsForDate(currentDate);
        });
        viewPaymentButton.setOnClickListener(v -> navigateToPaymentPage());
    }

    // Inflate the menu for the Activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Handle item clicks on the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // If the logout option is selected, log the user out
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            logoutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getCurrentDateInLocaleFormat() {
        // Get the current date and time from the system
        Calendar calendar = Calendar.getInstance();

        // Create a SimpleDateFormat using the system's default locale
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Format the current date as a string
        return sdf.format(calendar.getTime());
    }

    // Log the selected meal and update the due amount
    private void logMeal(String mealName, double mealCost) {
        Toast.makeText(this, "User id"+studentId, Toast.LENGTH_SHORT).show();

        db.collection("students").document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        double currentDueAmount = documentSnapshot.getDouble("dueAmount");
                        double updatedDueAmount = currentDueAmount + mealCost;

                        // Update the due amount in the database
                        db.collection("students").document(studentId)
                                .update("dueAmount", updatedDueAmount)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(AddMealActivity.this, mealName + " logged successfully", Toast.LENGTH_SHORT).show();
                                    updateDueAmountDisplay();
                                    addMealToMealsCollection(mealName, mealCost);

                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(AddMealActivity.this, "Failed to log meal", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(AddMealActivity.this, "Failed to retrieve student data", Toast.LENGTH_SHORT).show());
    }
    private void addMealToMealsCollection(String mealName, double mealCost) {
        // Prepare the meal data to add to the "meals" collection
        Map<String, Object> mealData = new HashMap<>();
        mealData.put("studentId", String.valueOf(studentId));
        mealData.put("mealType", mealName);
        mealData.put("price", mealCost);
        mealData.put("date", new Date()); // Store the current date as meal time

        // Add the meal to the "meals" collection
        db.collection("meals")
                .add(mealData)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(AddMealActivity.this, "Meal added to meals collection", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(AddMealActivity.this, "Failed to add meal to meals collection", Toast.LENGTH_SHORT).show());
    }
    // Update the displayed due amount
    private void updateDueAmountDisplay() {
        db.collection("students").document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        double updatedDueAmount = documentSnapshot.getDouble("dueAmount");

                        // Update the TextView with the new due amount
                        dueAmountTextView.setText("Total Due: ₹" + updatedDueAmount);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(AddMealActivity.this, "Failed to refresh due amount", Toast.LENGTH_SHORT).show());
    }

    private void fetchMealsForDate(String date) {
        try {
            Log.d("MealsByDate", "Fetching meals for date: " + date);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date selectedDate = sdf.parse(date);

            Log.d("MealsByDate", "Parsed Date: " + selectedDate.toString());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectedDate);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date startOfDay = calendar.getTime();

            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date endOfDay = calendar.getTime();

            db.collection("meals")
                    .whereGreaterThanOrEqualTo("date", startOfDay)
                    .whereLessThan("date", endOfDay)
//                    .whereEqualTo("studentId",String.valueOf(studentId))

                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Map<String, StringBuilder> studentMealsMap = new HashMap<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String studentId = document.getString("studentId");
                                String mealType = document.getString("mealType");
                                double price = document.getDouble("price");
                                Date mealDate = document.getDate("date");

                                Log.d("MealsByDate", "Student ID: " + studentId +
                                        ", Meal: " + mealType + ", Price: ₹" + price);

                                studentMealsMap.putIfAbsent(studentId, new StringBuilder());

                                studentMealsMap.get(studentId).append(String.format(Locale.getDefault(),
                                        "Meal: %s\nPrice: ₹%.2f\nDate: %s\n\n",
                                        mealType, price, mealDate.toString()));
                            }

                            showMealsTakenDialog(studentMealsMap);
                        } else {
                            Log.d("MealsByDate", "Failed to retrieve data");
                        }
                    });
        } catch (ParseException e) {
            Log.e("MealsByDate", "Error parsing the date: " + e.getMessage());
        }
    }

    private void showMealsTakenDialog(Map<String, StringBuilder> studentMealsMap) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, StringBuilder> entry : studentMealsMap.entrySet()) {
            result.append("Student ID: ").append(entry.getKey()).append("\n")
                    .append(entry.getValue().toString())
                    .append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Meals Taken Today")
                .setMessage(result.length() > 0 ? result.toString() : "No meals found for the selected date.")
                .setPositiveButton("Make Payment", (dialog, which) -> navigateToPaymentPage())
                .setNegativeButton("Close", null)
                .show();
    }

    private void navigateToPaymentPage() {
        db.collection("students").document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        double amountDue = documentSnapshot.getDouble("dueAmount");

                        Intent intent = new Intent(AddMealActivity.this, PaymentActivity.class);
                        intent.putExtra("amountDue", amountDue);
                        intent.putExtra("studentId", studentId);
                        paymentLauncher.launch(intent);
                    } else {
                        Toast.makeText(AddMealActivity.this, "Student data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(AddMealActivity.this, "Failed to retrieve student data", Toast.LENGTH_SHORT).show());
    }

    private void logoutUser() {
        // Add your logout functionality here
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
        // Clear any stored user session and redirect to login
        startActivity(new Intent(this, AuthenticationActivity.class));
        finish();
    }
}
