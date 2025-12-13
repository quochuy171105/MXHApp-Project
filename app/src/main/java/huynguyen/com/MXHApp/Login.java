package huynguyen.com.MXHApp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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

        // TODO: Initialize Firebase Auth and Firestore

        // TODO: Configure Google Sign In

        // TODO: Set up click listeners for login, Google sign-in, and sign-up navigation
    }

    private void loginUser() {
        // TODO: Get email and password, then perform Firebase email/password sign-in
    }

    private void signInWithGoogle() {
        // TODO: Start the Google Sign-In intent
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // TODO: Handle the result from Google Sign-In intent
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        // TODO: Exchange Google account for a Firebase credential and sign in
    }

    private void checkIfNewGoogleUser(FirebaseUser user) {
        // TODO: Check if the Google user is new and create a profile if so
    }

    private void createNewGoogleUser(FirebaseUser user) {
        // TODO: Create a new user document in Firestore
    }

    private void checkUserRoleAndRedirect(FirebaseUser user) {
        // TODO: Check user's role and account status, then redirect accordingly
    }
}
