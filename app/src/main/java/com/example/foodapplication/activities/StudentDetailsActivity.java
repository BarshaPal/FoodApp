package com.example.foodapplication.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapplication.MainActivity;
import com.example.foodapplication.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StudentDetailsActivity extends AppCompatActivity {

    private TextView textViewName, textViewAdvancePaid, textViewDueAmount, textViewTotalAmount, textViewSelectedDate, textAvailableBalance,textPhoneNumber;
    private CheckBox checkboxLunch, checkboxDinner;
    private EditText editTextPoachedQty, editTextOmeletteQty, editTextBoiledQty;
    private Button btnSelectDate, btnRequestApproval;
    private Map<String, Object> foodSelectionMap = new HashMap<>();

    private FirebaseFirestore db;
    private String studentId;
    private int advancePaid;
    private int availableBalance;

    private int totalAmount = 0;
    private String selectedDate = "";
    private static final int SMS_PERMISSION_REQUEST_CODE = 101;
    private int generatedOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);

        initializeViews();
        db = FirebaseFirestore.getInstance();
        studentId = getIntent().getStringExtra("studentId");
        btnRequestApproval.setEnabled(false);

        loadStudentDetails();
        setupListeners();
    }

    private void initializeViews() {
        textViewName = findViewById(R.id.text_view_name);
        textViewAdvancePaid = findViewById(R.id.text_view_advance_paid);
        textViewDueAmount = findViewById(R.id.text_view_due_amount);
        textViewTotalAmount = findViewById(R.id.text_view_total_amount);
        textViewSelectedDate = findViewById(R.id.text_view_selected_date);
        textAvailableBalance = findViewById(R.id.text_view_available_balance);
        checkboxLunch = findViewById(R.id.checkbox_lunch);
        checkboxDinner = findViewById(R.id.checkbox_dinner);
        editTextPoachedQty = findViewById(R.id.edittext_poached_qty);
        editTextOmeletteQty = findViewById(R.id.edittext_omelette_qty);
        editTextBoiledQty = findViewById(R.id.edittext_boiled_qty);
        btnSelectDate = findViewById(R.id.btn_select_date);
        btnRequestApproval = findViewById(R.id.btn_update_amount);
        textPhoneNumber= findViewById(R.id.editText_phone);

    }

    private void loadStudentDetails() {
        db.collection("userList").document(studentId).get().addOnSuccessListener(document -> {
            if (document.exists()) {
                String name = document.getString("name");
                advancePaid = document.getLong("advancePaid").intValue();
                availableBalance = document.getLong("availableBalance").intValue();

                textViewName.setText(name);
                textViewAdvancePaid.setText("Advance Paid: " + advancePaid + " Rs");
                updateBalanceDisplay(availableBalance);
            }
        });
    }

    private void setupListeners() {
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());
        btnRequestApproval.setOnClickListener(v -> calculateAndRequestApproval());
        checkboxLunch.setOnCheckedChangeListener((buttonView, isChecked) -> updateButtonState());
        checkboxDinner.setOnCheckedChangeListener((buttonView, isChecked) -> updateButtonState());
        addTextWatcher(editTextPoachedQty);
        addTextWatcher(editTextOmeletteQty);
        addTextWatcher(editTextBoiledQty);
    }

    private void addTextWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateButtonState();
            }
        });
    }

    private void updateButtonState() {
        boolean hasInput = checkboxLunch.isChecked() || checkboxDinner.isChecked() ||
                !editTextPoachedQty.getText().toString().trim().isEmpty() ||
                !editTextOmeletteQty.getText().toString().trim().isEmpty() ||
                !editTextBoiledQty.getText().toString().trim().isEmpty();
        btnRequestApproval.setEnabled(hasInput && !selectedDate.isEmpty());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDate = sdf.format(calendar.getTime());
                    textViewSelectedDate.setText("Selected Date: " + selectedDate);
                    updateButtonState();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void calculateAndRequestApproval() {
        totalAmount = 0;

        if (checkboxLunch.isChecked()) totalAmount += 20;
        if (checkboxDinner.isChecked()) totalAmount += 20;

        int poachedQty = parseQuantity(editTextPoachedQty);
        int omeletteQty = parseQuantity(editTextOmeletteQty);
        int boiledQty = parseQuantity(editTextBoiledQty);

        totalAmount += (poachedQty * 2) + (omeletteQty * 5) + (boiledQty * 6);
        availableBalance -= totalAmount;
        db.collection("userList").document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String phoneNumber = documentSnapshot.getString("phoneNumber");
                        if (phoneNumber != null && !phoneNumber.isEmpty()) {
                            // Send OTP to the retrieved phone number
                            generateAndSendOTP(phoneNumber,availableBalance);
                        } else {
                            Toast.makeText(this, "Phone number not available for the student", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Student not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to retrieve student data", Toast.LENGTH_SHORT).show();
                });
        // Generate and send OTP
    }
    private void updateFoodSelectionMap() {
        // Reset the map before adding new selections
        foodSelectionMap.clear();

        // Check if each food item is selected and update the map accordingly
        foodSelectionMap.put("lunch", checkboxLunch.isChecked());
        foodSelectionMap.put("dinner", checkboxDinner.isChecked());
        foodSelectionMap.put("poachedEggs", parseQuantity(editTextPoachedQty) > 0);
        foodSelectionMap.put("omelette", parseQuantity(editTextOmeletteQty) > 0);
        foodSelectionMap.put("boiledEggs", parseQuantity(editTextBoiledQty) > 0);
    }
    private void generateAndSendOTP(String studentPhoneNumber, int availableBalance) {
        generatedOTP = (int) (Math.random() * 900000) + 100000;

        String message = "Total amount"+totalAmount+" OTP is: " + generatedOTP;

        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        } else {
            sendSMS(studentPhoneNumber, message,availableBalance);
        }
    }

    private void sendSMS(String phoneNumber, String message, int availableBalance) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "OTP sent successfully!", Toast.LENGTH_SHORT).show();
            showOTPVerificationDialog(availableBalance);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send OTP.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showOTPVerificationDialog(int availableBalance) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verify OTP");

        // Set up the input
        final EditText input = new EditText(this);
        input.setHint("Enter OTP");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Verify", (dialog, which) -> {
            int enteredOTP = Integer.parseInt(input.getText().toString().trim());
            if (enteredOTP == generatedOTP) {
                Toast.makeText(this, "OTP Verified!", Toast.LENGTH_SHORT).show();
                saveFoodRecordToFirestore(availableBalance);
            } else {
                Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveFoodRecordToFirestore(int availableBalance) {
        // Get the current date to save the record
       updateFoodSelectionMap();
        // Create a food record map with the food selections and other details
        Map<String, Object> foodRecord = new HashMap<>();
        foodRecord.put("date", selectedDate);
        foodRecord.put("foodSelection", foodSelectionMap);  // Map containing lunch, dinner, eggs, etc.
        foodRecord.put("totalAmount", totalAmount); // Optional: Add the total amount calculated earlier
        foodRecord.put("availableBalance", availableBalance); // Optional: Add available balance

        // Save the food record in the Firestore subcollection for food records
        db.collection("userList")
                .document(studentId)
                .collection("foodRecords")
                .document(this.selectedDate) // Use the date as document ID
                .set(foodRecord)
                .addOnSuccessListener(aVoid -> {
                    showSuccessDialog();

                    Toast.makeText(this, "Food record saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save food record.", Toast.LENGTH_SHORT).show();
                });
        db.collection("userList")
                .document(studentId)
                .collection("foodRecords")
                .document(this.selectedDate) // Use the date as document ID
                .set(foodRecord)
                .addOnSuccessListener(aVoid -> {
                    showSuccessDialog();

                    Toast.makeText(this, "Food record saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save food record.", Toast.LENGTH_SHORT).show();
                });
        db.collection("userList")
                .document(studentId) // Reference to the specific student document
                .update("availableBalance", availableBalance) // Field to update
                .addOnSuccessListener(aVoid -> {
                    // Optionally, show a toast or log success
                    Toast.makeText(this, "Available balance updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure of balance update
                    Toast.makeText(this, "Failed to update available balance.", Toast.LENGTH_SHORT).show();
                });


    }
    private void showSuccessDialog() {
        // Create a success dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Success")
                .setMessage("Food record saved successfully!")
                .setCancelable(false) // Make sure the dialog is not dismissible by tapping outside
                .setPositiveButton("OK", (dialog, id) -> {
                    // Navigate to MainActivity after closing the dialog
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // To clear the current activity stack
                    startActivity(intent);
                    finish(); // Close the current activity
                });

        // Show the dialog
        builder.create().show();
    }
    private void updateBalanceDisplay(int balance) {
        if (balance < 0) {
            textAvailableBalance.setText("Available Balance: 0");
            textAvailableBalance.setTextColor(0xFFFF0000);
            textViewDueAmount.setText("Due Amount: " + Math.abs(balance) + " Rs");
        } else {
            textAvailableBalance.setText("Available Balance: " + balance + " Rs");
            textAvailableBalance.setTextColor(0xFF000000);
            textViewDueAmount.setText("");
        }
        textViewTotalAmount.setText("Total Amount: " + totalAmount + " Rs");
    }

    private int parseQuantity(EditText editText) {
        String text = editText.getText().toString().trim();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }
}
