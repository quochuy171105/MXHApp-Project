package huynguyen.com.MXHApp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import huynguyen.com.MXHApp.databinding.ActivityPostBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    private static final String TAG = "PostActivity";

    private ActivityPostBinding binding;

    private Uri postUri;

    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;

    // Edit Mode State
    private boolean isEditMode = false;
    private String postIdToEdit = null;

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
        storageReference = FirebaseStorage.getInstance().getReference().child("Posts");

        if (user == null) {
            Toast.makeText(this, "You must be logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check for Edit Mode
        if (getIntent().hasExtra("postIdToEdit")) {
            isEditMode = true;
            postIdToEdit = getIntent().getStringExtra("postIdToEdit");
            prepareEditMode();
        } else {
            isEditMode = false;
            prepareCreateMode();
        }

        binding.cancelPost.setOnClickListener(v -> finish());
    }

    private void prepareCreateMode() {
        binding.postUpload.setText("Post");
        binding.postUpload.setEnabled(false); // Disable post button initially
        binding.pick.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
        binding.postUpload.setOnClickListener(v -> uploadPost());
    }

    private void prepareEditMode() {
        binding.postUpload.setText("Update");
        binding.postUpload.setEnabled(false); // Keep disabled until data is loaded
        binding.pick.setClickable(false); // Disable changing the image in edit mode
        loadPostForEditing();
        binding.postUpload.setOnClickListener(v -> updatePost());
    }

    private void loadPostForEditing() {
        firestore.collection("posts").document(postIdToEdit).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String imageUrl = documentSnapshot.getString("postImage");
                    String description = documentSnapshot.getString("description");

                    binding.postDescription.setText(description);
                    if (imageUrl != null && !isDestroyed()) {
                        Glide.with(this).load(imageUrl).into(binding.pick);
                    }
                    binding.postUpload.setEnabled(true); // Enable button after data is loaded
                } else {
                    Toast.makeText(this, "Post not found.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load post for editing.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading post", e);
                finish();
            });
    }

    private void updatePost() {
        setUploadingState(true);
        String newDescription = binding.postDescription.getText().toString();
        firestore.collection("posts").document(postIdToEdit)
            .update("description", newDescription)
            .addOnSuccessListener(aVoid -> {
                setUploadingState(false);
                Toast.makeText(this, "Post updated!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                setUploadingState(false);
                Toast.makeText(this, "Failed to update post.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error updating post", e);
            });
    }

    private void uploadPost() {
        if (postUri == null) {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        String extension = fileExtension(postUri);
        if (extension == null || extension.isEmpty()) {
            Toast.makeText(this, "Cannot determine file type.", Toast.LENGTH_SHORT).show();
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
        map.put("timestamp", FieldValue.serverTimestamp());

        firestore.collection("posts").document(postid).set(map).addOnCompleteListener(task -> {
            setUploadingState(false);
            if (task.isSuccessful()) {
                Toast.makeText(PostActivity.this, "New post added!", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
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
        if(!isEditMode) {
            binding.pick.setEnabled(!isUploading);
        }
    }
}
