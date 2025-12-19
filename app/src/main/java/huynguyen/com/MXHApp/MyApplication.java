package huynguyen.com.MXHApp;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize general Firebase App
        FirebaseApp.initializeApp(this);

        // ADDED: Enable offline persistence for Realtime Database (for Chat)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        
        // Initialize Firebase App Check
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        // --- START: ENHANCED OFFLINE CONFIGURATION FOR FIRESTORE ---
        // Get Firestore instance
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Build settings object
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                // Use this to explicitly enable persistence, even though it's on by default
                .setPersistenceEnabled(true)
                // Set the cache size to unlimited. This is crucial for offline-first apps.
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        
        // Apply the settings
        firestore.setFirestoreSettings(settings);
        // --- END: ENHANCED OFFLINE CONFIGURATION FOR FIRESTORE ---
    }
}
