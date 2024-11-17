package com.example.foodapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapplication.R;
import com.example.foodapplication.activities.AddStudentActivity;
import com.example.foodapplication.activities.StudentDetailsActivity;
import com.example.foodapplication.models.Student;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<Student> studentList;
    private Context context;
    private FirebaseFirestore db;

    public StudentAdapter(Context context, List<Student> studentList) {
        this.context = context;
        this.studentList = studentList;
        this.db = FirebaseFirestore.getInstance(); // Initialize Firestore instance
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);

        // Bind basic data
        holder.textViewName.setText(student.getName());
        holder.textViewAmount.setText("Amount Due: " + student.getAmount());
        holder.textViewAdvancePaid.setText("Advance Paid: " + student.getAdvancePaid());

        // Fetch available balance from Firestore
        db.collection("userList").document(student.getId()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Double availableBalance = documentSnapshot.getDouble("availableBalance");
                if (availableBalance != null) {
                    if (availableBalance < 0) {
                        // Set text as Due Amount and change color to red
                        holder.textViewAvailableBalance.setText("Due Amount: " + Math.abs(availableBalance));
                        holder.textViewAvailableBalance.setTextColor(0xFFFF0000); // Red color
                    } else {
                        // Set text as Available Balance with default color
                        holder.textViewAvailableBalance.setText("Available Balance: " + availableBalance);
                        holder.textViewAvailableBalance.setTextColor(context.getResources().getColor(R.color.black)); // Default color
                    }
                }
            }
        });

        // Handle Edit Button
        holder.buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddStudentActivity.class);
            intent.putExtra("studentId", student.getId());
            intent.putExtra("isEdit", true);
            context.startActivity(intent);
        });

        // Handle Delete Button
        holder.buttonDelete.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                // If position is invalid, do nothing
                return;
            }

            db.collection("userList").document(student.getId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        studentList.remove(currentPosition);
                        notifyItemRemoved(currentPosition);
                        notifyItemRangeChanged(currentPosition, studentList.size());
                        Toast.makeText(context, "Student deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete student", Toast.LENGTH_SHORT).show());
        });

        // Handle Item Click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StudentDetailsActivity.class);
            intent.putExtra("studentId", student.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewAmount, textViewAdvancePaid, textViewAvailableBalance;
        Button buttonEdit, buttonDelete;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewAmount = itemView.findViewById(R.id.text_view_amount);
            textViewAdvancePaid = itemView.findViewById(R.id.text_view_advance_paid);
            textViewAvailableBalance = itemView.findViewById(R.id.text_view_available_balance);
            buttonEdit = itemView.findViewById(R.id.button_edit);
            buttonDelete = itemView.findViewById(R.id.button_delete);
        }
    }
}
