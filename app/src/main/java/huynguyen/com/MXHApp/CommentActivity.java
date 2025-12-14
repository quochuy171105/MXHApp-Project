package huynguyen.com.MXHApp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import huynguyen.com.MXHApp.Adapter.CommentAdapter;
import huynguyen.com.MXHApp.Model.Comment;
import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.databinding.ActivityCommentBinding;

public class CommentActivity extends AppCompatActivity {

    private static final String TAG = "CommentActivity";

    private ActivityCommentBinding binding;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private String postId;
    private String publisherId;

    private ListenerRegistration commentsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        binding.backButton.setOnClickListener(v -> finish());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        postId = intent.getStringExtra("postid");
        publisherId = intent.getStringExtra("publisher");

        setupRecyclerView();
        setupClickListeners();
        loadCurrentUserImage();
        readComments();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (commentsListener != null) commentsListener.remove();
    }

    private void setupRecyclerView() {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, postId);
        binding.recyclerView.setAdapter(commentAdapter);
    }

    private void setupClickListeners() {
        binding.post.setOnClickListener(v -> {
            String commentText = binding.addComment.getText().toString();
            if (commentText.isEmpty()) {
                Toast.makeText(CommentActivity.this, "You can't send an empty comment", Toast.LENGTH_SHORT).show();
            } else {
                addComment(commentText);
            }
        });
    }

    private void addComment(String commentText) {
        if(firebaseUser == null || postId == null) return;

        CollectionReference commentsRef = firestore.collection("posts").document(postId).collection("comments");
        String commentId = commentsRef.document().getId();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", commentText);
        hashMap.put("publisher", firebaseUser.getUid());
        hashMap.put("commentid", commentId);
        hashMap.put("timestamp", FieldValue.serverTimestamp());

        commentsRef.document(commentId).set(hashMap).addOnSuccessListener(aVoid -> {
            if (!publisherId.equals(firebaseUser.getUid())) {
                addNotification(commentText);
            }
            binding.addComment.setText("");
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error adding comment", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error adding comment", e);
        });
    }

    private void addNotification(String commentText) {
        if (firebaseUser == null || publisherId == null || postId == null) return;

        HashMap<String, Object> map = new HashMap<>();
        map.put("userid", firebaseUser.getUid());
        map.put("comment", "commented: " + commentText);
        map.put("postid", postId);
        map.put("ispost", true);
        map.put("timestamp", FieldValue.serverTimestamp());

        firestore.collection("users").document(publisherId).collection("notifications").add(map);
    }

    private void loadCurrentUserImage() {
        if (firebaseUser == null) return;
        firestore.collection("users").document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null && snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        if (user != null && user.getProfileUrl() != null && !user.getProfileUrl().isEmpty() && !isDestroyed()) {
                            Glide.with(getApplicationContext()).load(user.getProfileUrl()).into(binding.profileImage);
                        }
                    }
                });
    }

    private void readComments() {
        if(postId == null) return;
        if (commentsListener != null) commentsListener.remove();

        CollectionReference commentsRef = firestore.collection("posts").document(postId).collection("comments");
        commentsListener = commentsRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if(isDestroyed()) return;
                    if (error != null) {
                        Log.w(TAG, "Read comments failed.", error);
                        return;
                    }
                    if (snapshots != null) {
                        commentList.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Comment comment = doc.toObject(Comment.class);
                            commentList.add(comment);
                        }
                        commentAdapter.notifyDataSetChanged();
                    }
                });
    }
}
