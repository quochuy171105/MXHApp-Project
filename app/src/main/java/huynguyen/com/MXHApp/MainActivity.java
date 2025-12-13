package huynguyen.com.MXHApp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
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
import java.util.Locale;
import java.util.Map;

import huynguyen.com.MXHApp.databinding.ActivityMainBinding;

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

        // TODO: Initialize Firebase Auth and Firestore

        // TODO: Configure Google Sign In

        // TODO: Set up click listeners for sign-up, Google sign-up, and login navigation

        // TODO: Add text watchers for input validation
    }

    private void performEmailRegistration() {
        // TODO: Get user input, validate it, and create user with email/password
    }

    private void createNewUserInFirestore(FirebaseUser firebaseUser, String username, String memerName) {
        // TODO: Create a new user document in Firestore after registration
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

    private boolean validateInput(String userStr, String emailStr, String passStr) {
        // TODO: Implement input validation logic
        return true;
    }

    private void addTextWatchers() {
        // TODO: Add TextChangedListeners to clear errors on input change
    }

    private void setInProgress(boolean inProgress) {
        // TODO: Show/hide progress bar and disable/enable buttons
    }

    private void redirectToHome() {
        // TODO: Start HomeActivity and clear the task stack
    }
}
