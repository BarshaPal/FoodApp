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

public class AdminActivity extends AppCompatActivity {

    private CalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        calendarView = findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Format the selected date to "yyyy-MM-dd"
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String selectedDate = sdf.format(calendar.getTime());

            // Pass the selected date to the next activity
            Intent intent = new Intent(AdminActivity.this, MealsByDateActivity.class);
            intent.putExtra("selectedDate", selectedDate);
            startActivity(intent);
        });
    }
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

            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
            // Clear any stored user session and redirect to login
            startActivity(new Intent(this, AuthenticationActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
