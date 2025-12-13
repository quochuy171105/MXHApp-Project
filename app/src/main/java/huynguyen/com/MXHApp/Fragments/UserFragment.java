package huynguyen.com.MXHApp.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import huynguyen.com.MXHApp.Adapter.PhotosAdapter;
import huynguyen.com.MXHApp.EditProfileActivity;
import huynguyen.com.MXHApp.Model.Posts;
import huynguyen.com.MXHApp.ShowList;
import huynguyen.com.MXHApp.databinding.FragmentUserBinding;

public class UserFragment extends Fragment {

    private FragmentUserBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private String profileid;

    private PhotosAdapter photosAdapter;
    private List<Posts> postList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(inflater, container, false);

        // TODO: Initialize Firebase instances

        // TODO: Get profileid from SharedPreferences

        // TODO: Set up RecyclerView for photos

        // TODO: Determine if it's the current user's profile or another user's
        // and set up buttons (Edit Profile vs Follow/Unfollow) accordingly

        // TODO: Set up click listeners for followers, following, and options

        // TODO: Load user info, follower counts, and posts

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO: Reload data when the fragment resumes
    }

    private void userInfo() {
        // TODO: Load user info from Firestore
    }

    private void checkFollow() {
        // TODO: Check if the current user is following this profile
    }

    private void getFollowers() {
        // TODO: Get follower and following counts
    }

    private void getNrPosts() {
        // TODO: Get the number of posts
    }

    private void myPhotos() {
        // TODO: Load the user's posts (photos)
    }

    private void getSavedPosts() {
        // TODO: Load the user's saved posts
    }
}
