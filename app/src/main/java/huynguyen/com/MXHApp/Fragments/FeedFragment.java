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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import huynguyen.com.MXHApp.Adapter.PostAdapter;
import huynguyen.com.MXHApp.HomeActivity;
import huynguyen.com.MXHApp.Model.Posts;
import huynguyen.com.MXHApp.NotificationActivity;
import huynguyen.com.MXHApp.PostActivity;
import huynguyen.com.MXHApp.R;
import huynguyen.com.MXHApp.SearchUsers;
import huynguyen.com.MXHApp.databinding.FragmentFeedBinding;

import static android.content.Context.MODE_PRIVATE;

public class FeedFragment extends Fragment {

    private static final String TAG = "FeedFragment";

    private FragmentFeedBinding binding;
    private PostAdapter postAdapter;
    // REFACTORED: Changed from List<PostItem> to List<Posts>
    private List<Posts> postList;

    private FirebaseFirestore firestore;
    private FirebaseUser firebaseUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // REFACTORED: Initialize with the new data type
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        binding.recyclerView.setAdapter(postAdapter);

        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        loadPosts();
        loadUserData(); // Load current user's profile image for the toolbar

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClicks();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }

    public void refreshPosts() {
        if (getContext() != null) { // Check if fragment is attached
            loadPosts();
        }
    }

    private void setupClicks() {
        binding.search.setOnClickListener(v -> startActivity(new Intent(getActivity(), SearchUsers.class)));
        binding.note.setOnClickListener(v -> startActivity(new Intent(getActivity(), NotificationActivity.class)));
        binding.postOne.setOnClickListener(v -> startActivity(new Intent(getActivity(), PostActivity.class)));

        binding.profileImage.setOnClickListener(v -> {
            if (getContext() != null && firebaseUser != null) {
                SharedPreferences.Editor editor = getContext().getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileid", firebaseUser.getUid());
                editor.apply();
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).getBinding().bottomNav.setSelectedItemId(R.id.profile);
                }
            }
        });
    }

    private void loadUserData() {
        if (firebaseUser == null || getContext() == null) return;
        firestore.collection("users").document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (getContext() != null && binding != null && snapshot != null && snapshot.exists()) {
                        String p = snapshot.getString("profileUrl");
                        if (p != null && !p.isEmpty()) {
                            Glide.with(getContext()).load(p).placeholder(R.drawable.profile_image).into(binding.profileImage);
                        }
                    }
                });
    }

    // REFACTORED: Simplified loadPosts logic
    private void loadPosts() {
        if (getActivity() == null) return;

        firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error fetching posts", error);
                        return;
                    }
                    if (getActivity() == null || binding == null || snapshots == null) return;
                    
                    postList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Posts post = doc.toObject(Posts.class);
                        if (post.getPublisher() != null) {
                            postList.add(post);
                        }
                    }
                    postAdapter.notifyDataSetChanged();
                });
    }
    
    // REMOVED: The complex fetchPostDetails method is no longer needed here.
    // Its logic will be moved into the PostAdapter.
}
