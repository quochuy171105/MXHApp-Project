package huynguyen.com.MXHApp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import huynguyen.com.MXHApp.Adapter.PostAdapter;
import huynguyen.com.MXHApp.Model.Posts;
import huynguyen.com.MXHApp.databinding.ActivityPostDetailsBinding;

// REFACTORED: This activity now uses the new PostAdapter and simplified data fetching.
public class PostDetails extends AppCompatActivity {

    private static final String TAG = "PostDetailsActivity";

    private ActivityPostDetailsBinding binding;
    private String postId;
    private PostAdapter adapter;
    // REFACTORED: Changed from List<PostItem> to List<Posts>
    private List<Posts> postList;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        firestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        postId = intent.getStringExtra("postid");

        // Setup RecyclerView
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // REFACTORED: Initialize with the correct data types
        postList = new ArrayList<>();
        adapter = new PostAdapter(this, postList);
        binding.recyclerView.setAdapter(adapter);

        getPostDetails();
    }

    // REFACTORED: Simplified data fetching logic
    private void getPostDetails() {
        if (postId == null) {
            Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestore.collection("posts").document(postId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (isDestroyed()) return;

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Posts post = documentSnapshot.toObject(Posts.class);
                    if (post != null) {
                        postList.clear();
                        postList.add(post);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Log.d(TAG, "No such document");
                    Toast.makeText(this, "Post has been deleted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                if (isDestroyed()) return;
                Log.e(TAG, "Error fetching post", e);
                Toast.makeText(this, "Failed to load post.", Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    // REMOVED: The complex fetchPostDetails method is no longer needed.
    // The new PostAdapter handles all the sub-fetching internally.
}
