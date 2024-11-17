package com.example.foodapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapplication.activities.AddStudentActivity;
import com.example.foodapplication.activities.AuthenticationActivity;
import com.example.foodapplication.adapter.StudentAdapter;
import com.example.foodapplication.models.Student;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView studentListRecyclerView;
    private FloatingActionButton fabAddStudent;
    private FirebaseFirestore db;
    private StudentAdapter adapter;
    private List<Student> studentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        studentListRecyclerView = findViewById(R.id.student_list_recycler_view);
        fabAddStudent = findViewById(R.id.fab_add_student);

        studentListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(this, studentList);
        studentListRecyclerView.setAdapter(adapter);

        fabAddStudent.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddStudentActivity.class);
            startActivity(intent);
        });

        loadStudents();  // Start real-time updates
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

    private void loadStudents() {
        // Real-time updates with snapshot listener
        db.collection("userList").addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Toast.makeText(this, "Failed to load students.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (snapshot != null) {
                studentList.clear();
                for (DocumentSnapshot document : snapshot.getDocuments()) {
                    Student student = document.toObject(Student.class);
                    student.setId(document.getId());
                    studentList.add(student);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void logoutUser() {
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, AuthenticationActivity.class));
        finish();
    }
}
