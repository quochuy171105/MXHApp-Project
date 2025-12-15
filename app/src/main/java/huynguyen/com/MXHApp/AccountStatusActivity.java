package huynguyen.com.MXHApp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import huynguyen.com.MXHApp.databinding.ActivityAccountStatusBinding;

public class AccountStatusActivity extends AppCompatActivity {

    private ActivityAccountStatusBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        String reason = getIntent().getStringExtra("reason");
        binding.reasonTextView.setText(reason);

        binding.logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(AccountStatusActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
