package huynguyen.com.MXHApp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.databinding.ActivityEditProfileBinding;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;

    private Uri profileImageUri, backgroundImageUri;
    private String imageTypeToUpdate = "";

    private String originalUsername = "";
    private String originalMemer = "";

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("uploads");

        loadUserInfo();
        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.close.setOnClickListener(v -> finish());
        binding.save.setOnClickListener(v -> updateProfile());
        binding.save.setEnabled(false); // Disable save button initially

        binding.changePhoto.setOnClickListener(v -> {
            imageTypeToUpdate = "profile";
            openImagePicker();
        });
        binding.changeBackground.setOnClickListener(v -> {
            imageTypeToUpdate = "background";
            openImagePicker();
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                checkForChanges();
            }
        };

        binding.username.addTextChangedListener(textWatcher);
        binding.memer.addTextChangedListener(textWatcher);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void loadUserInfo() {
        setUploadingState(true); // Use progress bar for loading
        if (firebaseUser == null) return;
        firestore.collection("users").document(firebaseUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null && !isDestroyed()) {
                    originalUsername = user.getUsername();
                    originalMemer = user.getMemer();

                    binding.username.setText(originalUsername);
                    binding.memer.setText(originalMemer);

                    Glide.with(this).load(user.getProfileUrl()).placeholder(R.drawable.profile_image).into(binding.profileImage);
                    Glide.with(this).load(user.getBackground()).placeholder(android.R.color.darker_gray).into(binding.backgroundImage);
                }
            }
            setUploadingState(false); // Hide progress bar after loading
        }).addOnFailureListener(e -> {
            setUploadingState(false);
            Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            if ("profile".equals(imageTypeToUpdate)) {
                profileImageUri = imageUri;
                binding.profileImage.setImageURI(profileImageUri);
            } else if ("background".equals(imageTypeToUpdate)) {
                backgroundImageUri = imageUri;
                binding.backgroundImage.setImageURI(backgroundImageUri);
            }
            checkForChanges();
        }
    }

    private void checkForChanges() {
        boolean usernameChanged = !binding.username.getText().toString().equals(originalUsername);
        boolean memerChanged = !binding.memer.getText().toString().equals(originalMemer);
        boolean imageChanged = profileImageUri != null || backgroundImageUri != null;

        binding.save.setEnabled(usernameChanged || memerChanged || imageChanged);
    }

    private void updateProfile() {
        setUploadingState(true);

        final Map<String, Object> updates = new HashMap<>();
        // Only add to map if changed
        if (!binding.username.getText().toString().equals(originalUsername)) {
            updates.put("username", binding.username.getText().toString().toLowerCase(Locale.ROOT));
        }
        if (!binding.memer.getText().toString().equals(originalMemer)) {
            updates.put("memer", binding.memer.getText().toString());
        }

        handleProfileUpload(updates);
    }

    private void handleProfileUpload(final Map<String, Object> updates) {
        if (profileImageUri != null) {
            final StorageReference fileReference = storageRef.child(System.currentTimeMillis() + "_profile.jpg");
            fileReference.putFile(profileImageUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    if (task.getException() != null) throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnSuccessListener(uri -> {
                updates.put("profileUrl", uri.toString());
                handleBackgroundUpload(updates);
            }).addOnFailureListener(e -> {
                setUploadingState(false);
                Toast.makeText(EditProfileActivity.this, "Profile image upload failed.", Toast.LENGTH_SHORT).show();
            });
        } else {
            handleBackgroundUpload(updates);
        }
    }

    private void handleBackgroundUpload(final Map<String, Object> updates) {
        if (backgroundImageUri != null) {
            final StorageReference fileReference = storageRef.child(System.currentTimeMillis() + "_background.jpg");
            fileReference.putFile(backgroundImageUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    if (task.getException() != null) throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnSuccessListener(uri -> {
                updates.put("background", uri.toString());
                updateFirestore(updates);
            }).addOnFailureListener(e -> {
                setUploadingState(false);
                Toast.makeText(EditProfileActivity.this, "Background image upload failed.", Toast.LENGTH_SHORT).show();
            });
        } else {
            updateFirestore(updates);
        }
    }

    private void updateFirestore(Map<String, Object> updates) {
        if (firebaseUser == null) {
            setUploadingState(false);
            return;
        }

        if (updates.isEmpty()) {
            // No changes to be made
            setUploadingState(false);
            return;
        }

        firestore.collection("users").document(firebaseUser.getUid()).update(updates)
                .addOnSuccessListener(aVoid -> {
                    setUploadingState(false);
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                    // Set result to notify the previous screen to refresh
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    setUploadingState(false);
                    Toast.makeText(EditProfileActivity.this, "Error updating profile.", Toast.LENGTH_SHORT).show();
                });
    }
    private void setUploadingState(boolean isUploading) {
        if (isUploading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.save.setEnabled(false); // Always disable save during upload
            binding.changePhoto.setEnabled(false);
            binding.changeBackground.setEnabled(false);
            binding.close.setEnabled(false);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            // Save button state is handled by checkForChanges(), but we re-enable other controls
            binding.changePhoto.setEnabled(true);
            binding.changeBackground.setEnabled(true);
            binding.close.setEnabled(true);
            checkForChanges(); // Re-evaluate save button state after upload attempt
        }
    }
}