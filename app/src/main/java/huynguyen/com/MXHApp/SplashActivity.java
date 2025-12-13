package huynguyen.com.MXHApp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // TODO: Use a Handler to delay, then check user session
    }

    private void checkUser() {
        // TODO: Check if a Firebase user is already logged in
        // If yes, go to HomeActivity
        // If no, go to LoginActivity
    }
}
