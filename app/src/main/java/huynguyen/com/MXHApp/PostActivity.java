package huynguyen.com.MXHApp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.FieldValue;
import huynguyen.com.MXHApp.databinding.ActivityPostBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class PostActivity extends AppCompatActivity {
    private ActivityPostBinding binding;

    private Uri postUri;

    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;

    // Launcher mới để chọn ảnh bằng Photo Picker
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    postUri = uri;
                    binding.pick.setImageURI(postUri);
                    binding.postUpload.setEnabled(true); // Enable post button
                } else {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if (user == null) {
            Toast.makeText(this, "You must be logged in to post.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        storageReference = FirebaseStorage.getInstance().getReference().child("Posts");

        binding.postUpload.setEnabled(false); // Disable post button initially

        binding.pick.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        binding.postUpload.setOnClickListener(v -> uploadPost());

        binding.cancelPost.setOnClickListener(v -> finish());
    }

    private void uploadPost() {
        if (postUri == null) {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        String extension = fileExtension(postUri);
        if (extension == null || extension.isEmpty()) {
            Toast.makeText(this, "Cannot determine file type. Please select another image.", Toast.LENGTH_SHORT).show();
            return;
        }

        setUploadingState(true);

        final StorageReference sRef = storageReference.child(System.currentTimeMillis() + "." + extension);
        sRef.putFile(postUri).addOnSuccessListener(taskSnapshot -> sRef.getDownloadUrl().addOnSuccessListener(uri -> {
            savePostsDataInFirebase(uri.toString());
        }).addOnFailureListener(e -> {
            setUploadingState(false);
            Toast.makeText(PostActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        })).addOnProgressListener(snapshot -> {
            // You can show upload progress here if needed
        }).addOnFailureListener(e -> {
            setUploadingState(false);
            Toast.makeText(PostActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    public String fileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap map = MimeTypeMap.getSingleton();
        return map.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void savePostsDataInFirebase(final String url) {
        String postid = firestore.collection("posts").document().getId();
        HashMap<String, Object> map = new HashMap<>();
        map.put("postid", postid);
        map.put("postImage", url);
        map.put("description", binding.postDescription.getText().toString());
        map.put("publisher", user.getUid());
        map.put("timestamp", FieldValue.serverTimestamp()); // Use Firestore server timestamp

        firestore.collection("posts").document(postid).set(map).addOnCompleteListener(task -> {
            setUploadingState(false);
            if (task.isSuccessful()) {
                Toast.makeText(PostActivity.this, "New post added!", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK); // Set result for the previous screen
                finish();
            } else {
                Toast.makeText(PostActivity.this, "Failed to save post data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUploadingState(boolean isUploading) {
        binding.progressBarPost.setVisibility(isUploading ? View.VISIBLE : View.GONE);
        binding.postUpload.setEnabled(!isUploading);
        binding.cancelPost.setEnabled(!isUploading);
        binding.pick.setEnabled(!isUploading);
    }
}
