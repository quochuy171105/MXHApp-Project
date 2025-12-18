package huynguyen.com.MXHApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import huynguyen.com.MXHApp.Adapter.PhotosAdapter;
import huynguyen.com.MXHApp.Model.Posts;
import huynguyen.com.MXHApp.databinding.ActivityProfileBinding;

public class OthersProfile extends AppCompatActivity {

    private static final String TAG = "OthersProfile";

    private ActivityProfileBinding binding;
    private PhotosAdapter adapter;
    private List<Posts> postsList;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private String profileId;

    private final List<ListenerRegistration> listeners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        profileId = getIntent().getStringExtra("uid");
        if (profileId == null || user == null) {
            Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        setupClickListeners();

        // Hide buttons if viewing own profile, otherwise show them
        if (profileId.equals(user.getUid())) {
            binding.btnFollowAction.setVisibility(View.GONE);
            binding.btnMessage.setVisibility(View.GONE);
        } else {
            binding.btnFollowAction.setVisibility(View.VISIBLE);
            binding.btnMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadProfileData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeListeners();
    }

    private void removeListeners() {
        for (ListenerRegistration listener : listeners) {
            listener.remove();
        }
        listeners.clear();
    }

    private void setupRecyclerView() {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        postsList = new ArrayList<>();
        adapter = new PhotosAdapter(this, postsList);
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.followersLayout.setOnClickListener(v -> openShowList("followers"));
        binding.followingLayout.setOnClickListener(v -> openShowList("following"));

        binding.btnFollowAction.setOnClickListener(v -> {
            String currentText = binding.btnFollowAction.getText().toString();
            if (currentText.equalsIgnoreCase("Follow")) {
                followUser();
            } else if (currentText.equalsIgnoreCase("Following")) {
                unfollowUser();
            }
        });

        binding.btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(OthersProfile.this, ChatActivity.class);
            intent.putExtra("userId", profileId);
            startActivity(intent);
        });
    }

    private void loadProfileData() {
        removeListeners(); // Clear all previous listeners first
        
        // Re-establish all data listeners
        getUserData();
        getFollowerCount();
        getFollowingCount();
        getPosts();

        // FIX: Moved checkFollowStatus here to ensure its listener is active
        if (!profileId.equals(user.getUid())) {
             checkFollowStatus();
        }
    }

    private void getUserData() {
        ListenerRegistration userListener = firestore.collection("users").document(profileId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) { Log.e(TAG, "Listen failed.", error); return; }

                    if (snapshot != null && snapshot.exists()) {
                        binding.username.setText(snapshot.getString("username"));
                        binding.memer.setText(snapshot.getString("memer"));
                        Glide.with(this).load(snapshot.getString("profileUrl")).placeholder(R.drawable.profile_image).into(binding.profileImage);
                        Glide.with(this).load(snapshot.getString("background")).into(binding.background);
                    } else {
                        Log.d(TAG, "User not found: " + profileId);
                    }
                });
        listeners.add(userListener);
    }

    private void getFollowerCount(){
        ListenerRegistration followerListener = firestore.collection("users").document(profileId).collection("followers")
                .addSnapshotListener((value, error) -> {
                    if(error != null) return;
                    if(value != null) binding.followersCount.setText(String.valueOf(value.size()));
                });
        listeners.add(followerListener);
    }

    private void getFollowingCount(){
        ListenerRegistration followingListener = firestore.collection("users").document(profileId).collection("following")
                .addSnapshotListener((value, error) -> {
                    if(error != null) return;
                    if(value != null) binding.followingCount.setText(String.valueOf(value.size()));
                });
        listeners.add(followingListener);
    }

    private void getPosts(){
        CollectionReference postsRef = firestore.collection("posts");
        Query postsQuery = postsRef.whereEqualTo("publisher", profileId).orderBy("timestamp", Query.Direction.DESCENDING);

        ListenerRegistration postsListener = postsQuery.addSnapshotListener((snapshots, error) -> {
            if (error != null) return;
            postsList.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                Posts post = doc.toObject(Posts.class);
                postsList.add(post);
            }
            adapter.notifyDataSetChanged();
            binding.postsCount.setText(String.valueOf(postsList.size()));
        });
        listeners.add(postsListener);
    }

    private void checkFollowStatus() {
        ListenerRegistration checkFollowListener = firestore.collection("users").document(user.getUid()).collection("following").document(profileId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) return;
                    if (snapshot != null && snapshot.exists()) {
                        binding.btnFollowAction.setText("Following");
                    } else {
                        binding.btnFollowAction.setText("Follow");
                    }
                });
        listeners.add(checkFollowListener);
    }

    private void followUser() {
        WriteBatch batch = firestore.batch();
        DocumentReference followingRef = firestore.collection("users").document(user.getUid()).collection("following").document(profileId);
        batch.set(followingRef, new HashMap<>());
        DocumentReference followersRef = firestore.collection("users").document(profileId).collection("followers").document(user.getUid());
        batch.set(followersRef, new HashMap<>());
        batch.commit().addOnFailureListener(e -> Log.e(TAG, "Failed to follow user", e));
    }

    private void unfollowUser() {
        WriteBatch batch = firestore.batch();
        DocumentReference followingRef = firestore.collection("users").document(user.getUid()).collection("following").document(profileId);
        batch.delete(followingRef);
        DocumentReference followersRef = firestore.collection("users").document(profileId).collection("followers").document(user.getUid());
        batch.delete(followersRef);
        batch.commit().addOnFailureListener(e -> Log.e(TAG, "Failed to unfollow user", e));
    }

    private void openShowList(String title) {
        Intent intent = new Intent(this, ShowList.class);
        intent.putExtra("id", profileId);
        intent.putExtra("title", title);
        startActivity(intent);
    }
}
