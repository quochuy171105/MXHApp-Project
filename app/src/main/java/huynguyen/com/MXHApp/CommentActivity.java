package huynguyen.com.MXHApp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import huynguyen.com.MXHApp.Adapter.CommentAdapter;
import huynguyen.com.MXHApp.Model.Comment;
import huynguyen.com.MXHApp.databinding.ActivityCommentBinding;

public class CommentActivity extends AppCompatActivity {

    private ActivityCommentBinding binding;
    private String postId, publisherId;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private CommentAdapter adapter;
    private List<Comment> commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TODO: Set up the toolbar

        // TODO: Get postId and publisherId from intent

        // TODO: Initialize Firebase and RecyclerView

        // TODO: Set up click listener for post button

        // TODO: Load user info and comments
    }

    private void loadUserInfo() {
        // TODO: Load current user's profile image
    }

    private void postComment() {
        // TODO: Implement logic to post a new comment to Firestore
    }

    private void readComments() {
        // TODO: Implement Firestore snapshot listener to load comments for the post
    }
}
