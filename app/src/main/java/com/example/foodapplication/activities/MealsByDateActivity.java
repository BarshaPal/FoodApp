package com.example.foodapplication.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodapplication.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MealsByDateActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView mealsTextView;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meals_by_date);

        db = FirebaseFirestore.getInstance();
        mealsTextView = findViewById(R.id.mealsTextView);

        // Get the selected date passed from AdminActivity
        selectedDate = getIntent().getStringExtra("selectedDate");

        // Log the selected date to check
        Log.d("MealsByDate", "Selected Date: " + selectedDate);

        // Call the function to fetch meals for the selected date
        fetchMealsForDate(selectedDate);
    }

    private void fetchMealsForDate(String date) {
        try {
            // Log the date before parsing
            Log.d("MealsByDate", "Fetching meals for date: " + date);

            // Parse the selected date to match Firestore date format
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date selectedDate = sdf.parse(date);

            // Log the parsed date
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

            // Query to get all meals for the selected date
            db.collection("meals")
                    .whereGreaterThanOrEqualTo("date", startOfDay)
                    .whereLessThan("date", endOfDay)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Map to group meals by studentId
                            Map<String, StringBuilder> studentMealsMap = new HashMap<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String studentId = document.getString("studentId");
                                String mealType = document.getString("mealType");
                                double price = document.getDouble("price");
                                Date mealDate = document.getDate("date");

                                // Log each meal
                                Log.d("MealsByDate", "Student ID: " + studentId +
                                        ", Meal: " + mealType + ", Price: ₹" + price);

                                // If the studentId is not in the map, initialize a new StringBuilder
                                studentMealsMap.putIfAbsent(studentId, new StringBuilder());

                                // Append meal details to the student's StringBuilder
                                studentMealsMap.get(studentId).append(String.format(Locale.getDefault(),
                                        "Meal: %s\nPrice: ₹%.2f\nDate: %s\n\n",
                                        mealType, price, mealDate.toString()));
                            }

                            // Create a final result to display all students and their meals
                            StringBuilder result = new StringBuilder();
                            for (Map.Entry<String, StringBuilder> entry : studentMealsMap.entrySet()) {
                                result.append("Student ID: ").append(entry.getKey()).append("\n")
                                        .append(entry.getValue().toString())
                                        .append("\n");
                            }

                            // Set the result text
                            mealsTextView.setText(result.length() > 0 ? result.toString() : "No meals found for the selected date.");
                        } else {
                            mealsTextView.setText("Failed to retrieve data");
                        }
                    });
        } catch (Exception e) {
            Log.e("MealsByDate", "Error parsing date", e);
            mealsTextView.setText("Error parsing date");
        }
    }
}
