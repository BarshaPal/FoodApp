package com.example.foodapplication.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.foodapplication.R;

public class ApprovalActivity extends AppCompatActivity {

    private AlertDialog approvalDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval);

        // Show dialog when waiting for approval
        showApprovalDialog();

        // Register receiver to handle approval reply
        LocalBroadcastManager.getInstance(this).registerReceiver(approvalReceiver,
                new IntentFilter("com.example.foodapplication.APPROVAL_REPLY"));
    }

    private void showApprovalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Waiting for Approval")
                .setMessage("Please wait while the approver responds...")
                .setCancelable(false); // Prevent dismissal until the response is received
        approvalDialog = builder.create();
        approvalDialog.show();
    }

    private void dismissApprovalDialog() {
        if (approvalDialog != null && approvalDialog.isShowing()) {
            approvalDialog.dismiss();
        }
    }

    // BroadcastReceiver to handle approval reply
    private final BroadcastReceiver approvalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isApproved = intent.getBooleanExtra("isApproved", false);

            // Dismiss the waiting dialog
            dismissApprovalDialog();

            // Show success or rejection message
            if (isApproved) {
                Toast.makeText(ApprovalActivity.this, "Approval successful!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ApprovalActivity.this, "Approval rejected.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(approvalReceiver);
    }
}
