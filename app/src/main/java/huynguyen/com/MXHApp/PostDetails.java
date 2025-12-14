package huynguyen.com.MXHApp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import huynguyen.com.MXHApp.Adapter.PostAdapter;
import huynguyen.com.MXHApp.Model.PostItem;
import huynguyen.com.MXHApp.Model.Posts;
import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.databinding.ActivityPostDetailsBinding;

public class PostDetails extends AppCompatActivity {

    private static final String TAG = "PostDetailsActivity";

    private ActivityPostDetailsBinding binding;
    private String postId;
    private PostAdapter adapter;
    private List<PostItem> postItemList;

    private FirebaseFirestore firestore;
    private FirebaseUser firebaseUser;

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
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        postId = intent.getStringExtra("postid");

        // Setup RecyclerView
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postItemList = new ArrayList<>();
        adapter = new PostAdapter(this, postItemList);
        binding.recyclerView.setAdapter(adapter);

        getPostDetails();
    }

    private void getPostDetails() {
        if (postId == null) {
            Toast.makeText(this, "Post not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestore.collection("posts").document(postId).get()
                .addOnSuccessListener(postSnapshot -> {
                    if (postSnapshot != null && postSnapshot.exists()) {
                        Posts post = postSnapshot.toObject(Posts.class);
                        if (post.getPublisher() == null) return;

                        fetchPostDetails(post).thenAccept(postItem -> {
                            if(isDestroyed()) return;
                            runOnUiThread(() -> {
                                postItemList.clear();
                                postItemList.add(postItem);
                                adapter.notifyDataSetChanged();
                            });
                        }).exceptionally(ex -> {
                            Log.e(TAG, "Failed to fetch post details", ex);
                            return null;
                        });

                    } else {
                        Log.d(TAG, "No such document");
                        Toast.makeText(this, "Post has been deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching post", e);
                    Toast.makeText(this, "Failed to load post.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private CompletableFuture<PostItem> fetchPostDetails(Posts post) {
        CompletableFuture<PostItem> future = new CompletableFuture<>();
        CompletableFuture<User> userFuture = new CompletableFuture<>();
        firestore.collection("users").document(post.getPublisher()).get().addOnSuccessListener(userDoc -> {
            userFuture.complete(userDoc.toObject(User.class));
        }).addOnFailureListener(userFuture::completeExceptionally);

        CompletableFuture<Long> likeCountFuture = new CompletableFuture<>();
        CompletableFuture<Boolean> isLikedFuture = new CompletableFuture<>();
        firestore.collection("posts").document(post.getPostid()).collection("likes").get().addOnSuccessListener(likes -> {
            likeCountFuture.complete((long) likes.size());
            boolean liked = false;
            if (firebaseUser != null) {
                for (DocumentSnapshot doc : likes) {
                    if (doc.getId().equals(firebaseUser.getUid())) {
                        liked = true;
                        break;
                    }
                }
            }
            isLikedFuture.complete(liked);
        }).addOnFailureListener(e -> {
            likeCountFuture.completeExceptionally(e);
            isLikedFuture.completeExceptionally(e);
        });

        CompletableFuture<Long> commentCountFuture = new CompletableFuture<>();
        firestore.collection("posts").document(post.getPostid()).collection("comments").get().addOnSuccessListener(comments -> {
            commentCountFuture.complete((long) comments.size());
        }).addOnFailureListener(commentCountFuture::completeExceptionally);

        CompletableFuture<Boolean> isSavedFuture = new CompletableFuture<>();
        if (firebaseUser != null) {
            firestore.collection("users").document(firebaseUser.getUid()).collection("saved_posts").document(post.getPostid()).get().addOnSuccessListener(doc -> {
                isSavedFuture.complete(doc.exists());
            }).addOnFailureListener(isSavedFuture::completeExceptionally);
        } else {
            isSavedFuture.complete(false);
        }

        CompletableFuture.allOf(userFuture, likeCountFuture, isLikedFuture, commentCountFuture, isSavedFuture)
                .thenRun(() -> {
                    try {
                        User postUser = userFuture.get();
                        long likeCount = likeCountFuture.get();
                        boolean isLiked = isLikedFuture.get();
                        long commentCount = commentCountFuture.get();
                        boolean isSaved = isSavedFuture.get();
                        future.complete(new PostItem(post, postUser, likeCount, commentCount, isLiked, isSaved));
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                });

        return future;
    }
}
