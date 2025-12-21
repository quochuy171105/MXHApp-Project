package huynguyen.com.MXHApp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import huynguyen.com.MXHApp.databinding.ActivityLoginBinding;

public class Login extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.login.setOnClickListener(v -> loginUser());
        binding.signIn.setOnClickListener(v -> signInWithGoogle());
        binding.goToSignUp.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
        });

        binding.forgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");

        final EditText emailInput = new EditText(this);
        emailInput.setHint("Enter your email");
        builder.setView(emailInput);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(Login.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Login.this, "Failed to send reset email.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(Login.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void loginUser() {
        String email = binding.email.getText().toString();
        String password = binding.password.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkUserRoleAndRedirect(task.getResult().getUser());
                    } else {
                        Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        // Sign out first to ensure the account chooser always appears.
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        checkIfNewGoogleUser(user);
                    } else {
                        Toast.makeText(Login.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfNewGoogleUser(FirebaseUser user) {
        if (user == null) return;
        DocumentReference userRef = firestore.collection("users").document(user.getUid());
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                checkUserRoleAndRedirect(user);
            } else {
                createNewGoogleUser(user);
            }
        });
    }

    private void createNewGoogleUser(FirebaseUser user) {
        Map<String, Object> map = new HashMap<>();
        String displayName = user.getDisplayName() != null ? user.getDisplayName() : "";
        map.put("username", displayName.toLowerCase(Locale.ROOT));
        map.put("email", user.getEmail());
        map.put("memer", displayName);
        map.put("user_id", user.getUid());
        map.put("profileUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        map.put("background", "");
        map.put("accountStatus", "active");
        map.put("statusReason", "");
        map.put("role", "user"); // <-- Added this line to set default role

        firestore.collection("users").document(user.getUid()).set(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                checkUserRoleAndRedirect(user);
            } else {
                Toast.makeText(this, "Failed to create user profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserRoleAndRedirect(FirebaseUser user) {
        if (user == null) return;
        DocumentReference docRef = firestore.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String role = task.getResult().getString("role");
                String accountStatus = task.getResult().getString("accountStatus");

                // **FIXED**: Redirect to AccountStatusActivity if the account is not active.
                if (!"active".equals(accountStatus)) {
                    String reason = task.getResult().getString("statusReason");
                    Intent intent = new Intent(Login.this, AccountStatusActivity.class);
                    intent.putExtra("reason", reason);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return; // Stop further execution
                }

                Intent intent;
                if ("admin".equals(role)) {
                    intent = new Intent(Login.this, AdminActivity.class);
                } else {
                    intent = new Intent(Login.this, HomeActivity.class);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                // Handle case where user is authenticated but has no data in Firestore
                Toast.makeText(Login.this, "User data not found. Please contact support.", Toast.LENGTH_LONG).show();
                auth.signOut(); // Sign out the user to prevent inconsistent state
            }
        });
    }
}
