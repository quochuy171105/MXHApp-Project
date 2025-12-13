package huynguyen.com.MXHApp;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import huynguyen.com.MXHApp.databinding.ActivityAccountStatusBinding;

public class AccountStatusActivity extends AppCompatActivity {

    private ActivityAccountStatusBinding binding;
    private String userId;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TODO: Get userId from intent

        // TODO: Initialize Firestore

        // TODO: Set up click listeners for block and unblock buttons
    }

    private void updateUserStatus(String status, String reason) {
        // TODO: Implement logic to update user status in Firestore
    }
}
