package huynguyen.com.MXHApp;

import huynguyen.com.MXHApp.databinding.ActivityMainBinding;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;

    private ActivityMainBinding binding;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Click Listeners
        binding.goToLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        });
        binding.signUp.setOnClickListener(v -> performEmailRegistration());
        binding.signInWithGoogle.setOnClickListener(v -> signInWithGoogle());

        addTextWatchers();
    }

    private void performEmailRegistration() {
        String userStr = binding.username.getText().toString().trim();
        String emailStr = binding.email.getText().toString().trim();
        String passStr = binding.password.getText().toString().trim();

        if (!validateInput(userStr, emailStr, passStr)) {
            return;
        }

        setInProgress(true);

        auth.createUserWithEmailAndPassword(emailStr, passStr).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = auth.getCurrentUser();
                if (firebaseUser == null) {
                    setInProgress(false);
                    Toast.makeText(MainActivity.this, "Registration failed, user not created.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Use username as the initial memer name
                createNewUserInFirestore(firebaseUser, userStr, userStr);
            } else {
                setInProgress(false);
                String authError = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                Toast.makeText(MainActivity.this, "Registration Failed: " + authError, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createNewUserInFirestore(FirebaseUser firebaseUser, String username, String memerName) {
        String userId = firebaseUser.getUid();
        DocumentReference userRef = firestore.collection("users").document(userId);

        HashMap<String, Object> map = new HashMap<>();
        map.put("username", username.toLowerCase(Locale.ROOT));
        map.put("email", firebaseUser.getEmail());
        map.put("memer", memerName); // Use username as initial memer name
        map.put("user_id", userId);
        map.put("profileUrl", firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "");
        map.put("background", "");
        map.put("accountStatus", "active");
        map.put("statusReason", "");
        map.put("role", "user");

        userRef.set(map).addOnCompleteListener(dbTask -> {
            setInProgress(false);
            if (dbTask.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Registration Success..", Toast.LENGTH_SHORT).show();
                redirectToHome();
            } else {
                String dbError = dbTask.getException() != null ? dbTask.getException().getMessage() : "Could not save user data.";
                Toast.makeText(MainActivity.this, "Database Error: " + dbError, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
        setInProgress(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        checkIfNewGoogleUser(user);
                    } else {
                        setInProgress(false);
                        Toast.makeText(MainActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfNewGoogleUser(FirebaseUser user) {
        if (user == null) return;
        DocumentReference userRef = firestore.collection("users").document(user.getUid());
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                // User already exists, just log in
                setInProgress(false);
                redirectToHome();
            } else {
                // New user, create profile in Firestore
                String displayName = user.getDisplayName() != null ? user.getDisplayName() : "User";
                createNewUserInFirestore(user, displayName, displayName);
            }
        });
    }

    private boolean validateInput(String userStr, String emailStr, String passStr) {
        boolean isValid = true;
        if (userStr.isEmpty()) {
            binding.usernameLayout.setError("Username is required");
            isValid = false;
        } else {
            binding.usernameLayout.setError(null);
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            binding.emailLayout.setError("A valid email is required");
            isValid = false;
        } else {
            binding.emailLayout.setError(null);
        }

        if (passStr.length() < 6) {
            binding.passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            binding.passwordLayout.setError(null);
        }
        return isValid;
    }

    private void addTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.usernameLayout.setError(null);
                binding.emailLayout.setError(null);
                binding.passwordLayout.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        binding.username.addTextChangedListener(textWatcher);
        binding.email.addTextChangedListener(textWatcher);
        binding.password.addTextChangedListener(textWatcher);
    }

    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.signUp.setEnabled(false);
            binding.signInWithGoogle.setEnabled(false);
            binding.goToLogin.setEnabled(false);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.signUp.setEnabled(true);
            binding.signInWithGoogle.setEnabled(true);
            binding.goToLogin.setEnabled(true);
        }
    }

    private void redirectToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
