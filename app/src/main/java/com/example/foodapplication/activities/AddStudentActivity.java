package com.example.foodapplication.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapplication.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddStudentActivity extends AppCompatActivity {

    private EditText editTextName, editTextAmount, editTextAdvancePaid, editTextCountryCode, editTextPhone;
    private Button buttonSave;
    private FirebaseFirestore db;

    private boolean isEdit = false;
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        db = FirebaseFirestore.getInstance();

        editTextName = findViewById(R.id.editText_name);
        editTextAmount = findViewById(R.id.editText_amount);
        editTextAdvancePaid = findViewById(R.id.editText_advance);
        buttonSave = findViewById(R.id.button_save);
        editTextCountryCode = findViewById(R.id.editText_country_code);
        editTextPhone = findViewById(R.id.editText_phone);

        // Check if we are editing or adding a new student
        if (getIntent() != null && getIntent().hasExtra("isEdit")) {
            isEdit = getIntent().getBooleanExtra("isEdit", false);
            studentId = getIntent().getStringExtra("studentId");

            if (isEdit && studentId != null) {
                loadStudentData(studentId); // Load existing student data
            }
        }

        buttonSave.setOnClickListener(v -> {
            if (validateInputs()) {
                if (isEdit) {
                    updateStudent();
                } else {
                    saveStudent();
                }
            }
        });
    }

    private void loadStudentData(String studentId) {
        db.collection("userList").document(studentId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                editTextName.setText(documentSnapshot.getString("name"));
                editTextAmount.setText(String.valueOf(documentSnapshot.getDouble("amount")));
                editTextAdvancePaid.setText(String.valueOf(documentSnapshot.getDouble("advancePaid")));
                editTextCountryCode.setText(documentSnapshot.getString("countryCode"));
                editTextPhone.setText(documentSnapshot.getString("phoneNumber").substring(2)); // Extract phone number
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load data.", Toast.LENGTH_SHORT).show());
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(editTextName.getText())) {
            Toast.makeText(this, "Name is required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(editTextAmount.getText())) {
            Toast.makeText(this, "Amount is required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(editTextAdvancePaid.getText())) {
            Toast.makeText(this, "Advance Paid is required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(editTextPhone.getText())) {
            Toast.makeText(this, "Phone Number is required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveStudent() {
        String name = editTextName.getText().toString();
        double amount = Double.parseDouble(editTextAmount.getText().toString());
        double advancePaid = Double.parseDouble(editTextAdvancePaid.getText().toString());
        String countryCode = editTextCountryCode.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String fullPhoneNumber = countryCode + phone;

        Map<String, Object> student = new HashMap<>();
        student.put("name", name);
        student.put("amount", amount);
        student.put("advancePaid", advancePaid);
        student.put("availableBalance", advancePaid);
        student.put("countryCode", countryCode);
        student.put("phoneNumber", fullPhoneNumber);

        db.collection("userList").add(student).addOnSuccessListener(documentReference -> {
            Toast.makeText(this, "Student added successfully.", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to add student.", Toast.LENGTH_SHORT).show());
    }

    private void updateStudent() {
        String name = editTextName.getText().toString();
        double amount = Double.parseDouble(editTextAmount.getText().toString());
        double advancePaid = Double.parseDouble(editTextAdvancePaid.getText().toString());
        String countryCode = editTextCountryCode.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String fullPhoneNumber = countryCode + phone;

        Map<String, Object> student = new HashMap<>();
        student.put("name", name);
        student.put("amount", amount);
        student.put("advancePaid", advancePaid);
        student.put("availableBalance", advancePaid); // Reset available balance if needed
        student.put("countryCode", countryCode);
        student.put("phoneNumber", fullPhoneNumber);

        db.collection("userList").document(studentId).update(student).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Student updated successfully.", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to update student.", Toast.LENGTH_SHORT).show());
    }
}
