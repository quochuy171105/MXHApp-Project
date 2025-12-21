package huynguyen.com.MXHApp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserStatus, 2000);
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // **FIXED**: User not logged in, go to Login screen instead of Registration
            startActivity(new Intent(SplashActivity.this, Login.class));
            finish();
        } else {
            // User is logged in, check their role and status from Firestore
            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    String role = task.getResult().getString("role");
                    String accountStatus = task.getResult().getString("accountStatus");

                    if (!"active".equals(accountStatus)) {
                        // Go to status screen if account is not active
                        Intent intent = new Intent(SplashActivity.this, AccountStatusActivity.class);
                        intent.putExtra("reason", task.getResult().getString("statusReason"));
                        startActivity(intent);
                    } else if ("admin".equals(role)) {
                        // Go to admin screen
                        startActivity(new Intent(SplashActivity.this, AdminActivity.class));
                    } else {
                        // Go to home screen for normal users
                        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                    }
                } else {
                    // User data not found in Firestore, treat as new/invalid user, go to Login
                    startActivity(new Intent(SplashActivity.this, Login.class));
                }
                finish();
            });
        }
    }
}
