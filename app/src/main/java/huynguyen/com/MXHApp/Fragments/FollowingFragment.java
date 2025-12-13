package huynguyen.com.MXHApp.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import huynguyen.com.MXHApp.Adapter.PostAdapter;
import huynguyen.com.MXHApp.HomeActivity;
import huynguyen.com.MXHApp.Model.PostItem;
import huynguyen.com.MXHApp.Model.Posts;
import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.NotificationActivity;
import huynguyen.com.MXHApp.PostActivity;
import huynguyen.com.MXHApp.R;
import huynguyen.com.MXHApp.SearchUsers;
import huynguyen.com.MXHApp.databinding.FragmentFollowingBinding;

public class FollowingFragment extends Fragment {

    private static final String TAG = "FollowingFragment";

    private FragmentFollowingBinding binding;
    private PostAdapter adapter;
    private List<PostItem> postItemList;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private ListenerRegistration postsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFollowingBinding.inflate(inflater, container, false);

        // TODO: Initialize Firebase instances

        // TODO: Set up RecyclerView

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: Set up click listeners, load user data, and check following list
    }

    @Override
    public void onPause() {
        super.onPause();
        // TODO: Remove Firestore listener
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO: Re-check the following list
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupClicks() {
        // TODO: Implement click listeners for toolbar icons
    }

    private void loadUserData() {
        // TODO: Load current user's profile image
    }

    private void checkFollowing() {
        // TODO: Get the list of users the current user is following
    }

    private void loadFollowingPosts(List<String> followingList) {
        // TODO: Load posts only from the users in the following list
    }

    private CompletableFuture<PostItem> fetchPostDetails(Posts post) {
        // TODO: Implement logic to fetch all details for a single post item
        return new CompletableFuture<>();
    }
}
