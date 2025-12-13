package huynguyen.com.MXHApp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import huynguyen.com.MXHApp.Adapter.PhotosAdapter;
import huynguyen.com.MXHApp.Model.Posts;
import huynguyen.com.MXHApp.Model.User;

public class OthersProfile extends AppCompatActivity {

    private static final String TAG = "OthersProfile";

    // TODO: Declare UI elements and Firebase instances
    private RecyclerView recyclerView;
    private PhotosAdapter photosAdapter;
    private List<Posts> postsList;

    private String profileId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // TODO: Get profileId from the intent

        // TODO: Initialize UI elements

        // TODO: Set up RecyclerView for photos

        // TODO: Set up click listeners for follow/unfollow button

        // TODO: Load user info, follower counts, and posts
    }

    private void userInfo() {
        // TODO: Load user information from Firestore and display it
    }

    private void checkFollow() {
        // TODO: Check if the current user is following this profile
    }

    private void getFollowers() {
        // TODO: Get the number of followers and following
    }

    private void getNrPosts() {
        // TODO: Get the number of posts by this user
    }

    private void myPhotos() {
        // TODO: Load all photos posted by this user
    }
}
