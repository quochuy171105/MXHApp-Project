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
import com.google.firebase.firestore.ListenerRegistration;
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
import huynguyen.com.MXHApp.databinding.FragmentFollowingBinding;

import static android.content.Context.MODE_PRIVATE;

// REFACTORED: This fragment now follows the same simplified logic as FeedFragment.
public class FollowingFragment extends Fragment {

    private static final String TAG = "FollowingFragment";

    private FragmentFollowingBinding binding;

    private PostAdapter adapter;
    // REFACTORED: Changed from List<PostItem> to List<Posts>
    private List<Posts> postList;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private ListenerRegistration postsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFollowingBinding.inflate(inflater, container, false);

        // Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        
        // REFACTORED: Initialize with the new data type
        postList = new ArrayList<>();
        adapter = new PostAdapter(getActivity(), postList);
        binding.recyclerView.setAdapter(adapter);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClicks();
        loadUserData();
        checkFollowing();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (postsListener != null) {
            postsListener.remove();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Re-check following list in case user followed someone new
        checkFollowing();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }

    private void setupClicks() {
        binding.search.setOnClickListener(v -> startActivity(new Intent(getActivity(), SearchUsers.class)));
        binding.note.setOnClickListener(v -> startActivity(new Intent(getActivity(), NotificationActivity.class)));
        binding.discover.setOnClickListener(v -> startActivity(new Intent(getActivity(), SearchUsers.class)));
        binding.postOne.setOnClickListener(v -> startActivity(new Intent(getActivity(), PostActivity.class)));

        binding.profileImage.setOnClickListener(v -> {
            if (getContext() != null && user != null) {
                SharedPreferences.Editor editor = getContext().getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileid", user.getUid());
                editor.apply();
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).getBinding().bottomNav.setSelectedItemId(R.id.profile);
                }
            }
        });
    }

    private void loadUserData() {
        if (user == null || getContext() == null) return;
        firestore.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (getContext() != null && snapshot != null && snapshot.exists()) {
                        String p = snapshot.getString("profileUrl");
                        if (p != null && !p.isEmpty()) {
                            Glide.with(getContext()).load(p).placeholder(R.drawable.profile_image).into(binding.profileImage);
                        }
                    }
                });
    }

    private void checkFollowing() {
        if (user == null) return;
        
        // Detach any previous listener before creating a new one
        if (postsListener != null) {
            postsListener.remove();
        }

        firestore.collection("users").document(user.getUid()).collection("following")
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (binding == null || getContext() == null) return; // Check if fragment is still alive
                    List<String> followingList = new ArrayList<>();
                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            followingList.add(doc.getId());
                        }
                        loadFollowingPosts(followingList);
                        binding.no.setVisibility(View.GONE);
                        binding.discover.setVisibility(View.GONE);
                    } else {
                        postList.clear();
                        if(adapter != null) adapter.notifyDataSetChanged();
                        binding.no.setVisibility(View.VISIBLE);
                        binding.discover.setVisibility(View.VISIBLE);
                    }
                });
    }

    // REFACTORED: Simplified post loading logic
    private void loadFollowingPosts(List<String> followingList) {
        if (followingList == null || followingList.isEmpty()) return;

        postsListener = firestore.collection("posts").whereIn("publisher", followingList)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (binding == null || getContext() == null) return;
                    if (error != null) {
                        Log.e(TAG, "Listen for following posts failed", error);
                        return;
                    }
                    if (snapshots == null) return;

                    postList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Posts post = doc.toObject(Posts.class);
                        postList.add(post);
                    }
                    if(adapter != null) adapter.notifyDataSetChanged();
                });
    }

    // REMOVED: The complex fetchPostDetails method is no longer needed here.
    // Its logic is now handled by the PostAdapter.
}
