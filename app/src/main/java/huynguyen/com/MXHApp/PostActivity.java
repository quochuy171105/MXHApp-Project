package huynguyen.com.MXHApp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Map;

import huynguyen.com.MXHApp.databinding.ActivityPostBinding;

public class PostActivity extends AppCompatActivity {

    private ActivityPostBinding binding;
    private Uri imageUri;
    private String imageUrl = "";
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TODO: Initialize Firebase instances

        // TODO: Set up click listeners for close and post buttons

        // TODO: Start image cropper activity
    }

    private String getFileExtension(Uri uri) {
        // TODO: Implement logic to get file extension from Uri
        return null;
    }

    private void uploadImage() {
        // TODO: Implement image upload to Firebase Storage and then publish the post
    }

    private void publishPost(String imageUrl) {
        // TODO: Get description and create a new post document in Firestore
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // TODO: Handle result from CropImage activity
    }
}
