package huynguyen.com.MXHApp.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
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
import huynguyen.com.MXHApp.AdminActivity;
import huynguyen.com.MXHApp.EditProfileActivity;
import huynguyen.com.MXHApp.Login;
import huynguyen.com.MXHApp.Model.Posts;
import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.R;
import huynguyen.com.MXHApp.ShowList;
import huynguyen.com.MXHApp.databinding.FragmentUserBinding;

public class UserFragment extends Fragment {

    private static final String TAG = "UserFragment";
    private FragmentUserBinding binding;

    private List<Posts> myPostsList;
    private List<String> savedPostsIdList;
    private List<Posts> savedPostsList;
    private PhotosAdapter myPostsAdapter;
    private PhotosAdapter savedPostsAdapter;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private String profileid;

    private final List<ListenerRegistration> listeners = new ArrayList<>();
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register for activity result
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Profile was updated, reload data
                        loadProfileData();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        if (getContext() != null) {
            SharedPreferences preferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
            profileid = preferences.getString("profileid", user != null ? user.getUid() : "");
        }

        setupRecyclerViews();
        setupClickListeners();
        setupToolbarAnimation();
        setupTabs();

        loadProfileData();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data on resume to catch any external changes
        loadProfileData();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeAllListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecyclerViews() {
        if (getContext() == null) return;
        binding.recyclerViewPosts.setHasFixedSize(true);
        binding.recyclerViewPosts.setLayoutManager(new GridLayoutManager(getContext(), 3));

        myPostsList = new ArrayList<>();
        myPostsAdapter = new PhotosAdapter(getContext(), myPostsList);

        savedPostsList = new ArrayList<>();
        savedPostsAdapter = new PhotosAdapter(getContext(), savedPostsList);
        savedPostsIdList = new ArrayList<>();

        binding.recyclerViewPosts.setAdapter(myPostsAdapter);
    }

    private void setupClickListeners() {
        binding.followersLayout.setOnClickListener(v -> openShowList("followers"));
        binding.followingLayout.setOnClickListener(v -> openShowList("following"));
    }

    private void loadProfileData() {
        if (user == null || profileid == null || profileid.isEmpty() || getContext() == null)
            return;
        removeAllListeners();

        getUserData();
        getFollowerCount();
        getFollowingCount();
        getMyPosts();

        if (profileid.equals(user.getUid())) {
            binding.tabLayout.getTabAt(1).view.setVisibility(View.VISIBLE);
            getSavedPosts();
        } else {
            binding.tabLayout.getTabAt(1).view.setVisibility(View.GONE);
        }
        setupProfileInteraction();
    }

    private void removeAllListeners() {
        for (ListenerRegistration listener : listeners) {
            listener.remove();
        }
        listeners.clear();
    }

    private void setupProfileInteraction() {
        if (profileid.equals(user.getUid())) {
            binding.profileActionButton.setText("Edit Profile");
            binding.optionsMenu.setVisibility(View.VISIBLE);
            binding.optionsMenu.setOnClickListener(this::showOptionsMenu);
        } else {
            binding.optionsMenu.setVisibility(View.GONE);
            checkFollowStatus();
        }

        binding.profileActionButton.setOnClickListener(v -> {
            String buttonText = binding.profileActionButton.getText().toString();
            if (buttonText.equals("Edit Profile")) {
                // Launch EditProfileActivity using the new launcher
                Intent intent = new Intent(getContext(), EditProfileActivity.class);
                editProfileLauncher.launch(intent);
            } else if (buttonText.equals("Follow")) {
                followUser();
            } else if (buttonText.equals("Following")) {
                unfollowUser();
            }
        });
    }

    private void setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    binding.recyclerViewPosts.setAdapter(myPostsAdapter);
                } else {
                    binding.recyclerViewPosts.setAdapter(savedPostsAdapter);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupToolbarAnimation() {
        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (binding == null) return;
            float percentage = ((float) Math.abs(verticalOffset)) / appBarLayout.getTotalScrollRange();
            binding.toolbarUsername.setAlpha(percentage);
        });
    }

    private void showOptionsMenu(View view) {
        if (getContext() == null || user == null) return;
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.profile_options_menu, popupMenu.getMenu());

        firestore.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");
                if ("admin".equals(role)) {
                    popupMenu.getMenu().findItem(R.id.admin_mode_option).setVisible(true);
                }
            }
        });

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.logout_option) {
                logoutUser();
                return true;
            } else if (itemId == R.id.admin_mode_option) {
                startActivity(new Intent(getContext(), AdminActivity.class));
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void logoutUser() {
        if (getContext() != null) {
            SharedPreferences.Editor editor = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
            editor.remove("profileid");
            editor.apply();
        }

        auth.signOut();
        Intent intent = new Intent(getActivity(), Login.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void getUserData() {
        if (profileid == null) return;
        ListenerRegistration userListener = firestore.collection("users").document(profileid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        return;
                    }
                    if (getContext() == null || binding == null) return;

                    if (snapshot != null && snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        if (user != null) {
                            binding.toolbarUsername.setText(user.getUsername());
                            binding.username.setText(user.getUsername());
                            binding.memer.setText(user.getMemer());
                            Glide.with(getContext()).load(user.getProfileUrl()).placeholder(R.drawable.profile_image).into(binding.profileImage);
                            Glide.with(getContext()).load(user.getBackground()).placeholder(R.drawable.edittext).into(binding.background);
                        }
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                });
        listeners.add(userListener);
    }

    private void getFollowerCount() {
        if (profileid == null) return;
        ListenerRegistration followerListener = firestore.collection("users").document(profileid).collection("followers")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }
                    if (binding != null && value != null) {
                        binding.followersCount.setText(String.valueOf(value.size()));
                    }
                });
        listeners.add(followerListener);
    }

    private void getFollowingCount() {
        if (profileid == null) return;
        ListenerRegistration followingListener = firestore.collection("users").document(profileid).collection("following")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }
                    if (binding != null && value != null) {
                        binding.followingCount.setText(String.valueOf(value.size()));
                    }
                });
        listeners.add(followingListener);
    }

    private void getMyPosts() {
        if (profileid == null) return;
        CollectionReference postsRef = firestore.collection("posts");
        Query postsQuery = postsRef.whereEqualTo("publisher", profileid)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        ListenerRegistration postsListener = postsQuery.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                return;
            }
            if (binding == null) return;

            myPostsList.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                Posts post = doc.toObject(Posts.class);
                myPostsList.add(post);
            }
            myPostsAdapter.notifyDataSetChanged();
            binding.postsCount.setText(String.valueOf(myPostsList.size()));
        });
        listeners.add(postsListener);
    }

    private void getSavedPosts() {
        if (user == null) return;
        savedPostsIdList.clear();
        ListenerRegistration savedIdsListener = firestore.collection("users").document(user.getUid()).collection("saved_posts")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen for saved posts failed.", error);
                        return;
                    }
                    if (snapshots != null) {
                        savedPostsIdList.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            savedPostsIdList.add(doc.getId());
                        }
                        fetchSavedPosts();
                    }
                });
        listeners.add(savedIdsListener);
    }

    private void fetchSavedPosts() {
        savedPostsList.clear();
        if (savedPostsIdList.isEmpty()) {
            savedPostsAdapter.notifyDataSetChanged();
            return;
        }
        for (String id : savedPostsIdList) {
            ListenerRegistration postListener = firestore.collection("posts").document(id).addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    Log.w(TAG, "Fetch saved post failed: " + id, error);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    Posts post = snapshot.toObject(Posts.class);
                    if (post != null) {
                        boolean exists = false;
                        for (int i = 0; i < savedPostsList.size(); i++) {
                            if (savedPostsList.get(i).getPostid().equals(post.getPostid())) {
                                savedPostsList.set(i, post); // Update existing post
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            savedPostsList.add(post);
                        }
                    }
                    savedPostsAdapter.notifyDataSetChanged();
                }
            });
            listeners.add(postListener);
        }
    }

    private void checkFollowStatus() {
        if (user == null || profileid == null) return;
        ListenerRegistration checkFollowListener = firestore.collection("users").document(user.getUid()).collection("following").document(profileid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }
                    if (binding != null) {
                        binding.profileActionButton.setText(snapshot != null && snapshot.exists() ? "Following" : "Follow");
                    }
                });
        listeners.add(checkFollowListener);
    }

    private void followUser() {
        if (user == null || profileid == null) return;
        WriteBatch batch = firestore.batch();

        DocumentReference followingRef = firestore.collection("users").document(user.getUid()).collection("following").document(profileid);
        batch.set(followingRef, new HashMap<>());

        DocumentReference followersRef = firestore.collection("users").document(profileid).collection("followers").document(user.getUid());
        batch.set(followersRef, new HashMap<>());

        batch.commit().addOnFailureListener(e -> Log.e(TAG, "Failed to follow user", e));
    }

    private void unfollowUser() {
        if (user == null || profileid == null) return;
        WriteBatch batch = firestore.batch();

        DocumentReference followingRef = firestore.collection("users").document(user.getUid()).collection("following").document(profileid);
        batch.delete(followingRef);

        DocumentReference followersRef = firestore.collection("users").document(profileid).collection("followers").document(user.getUid());
        batch.delete(followersRef);

        batch.commit().addOnFailureListener(e -> Log.e(TAG, "Failed to unfollow user", e));
    }
    private void openShowList(String title) {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), ShowList.class);
        intent.putExtra("id", profileid);
        intent.putExtra("title", title);
        startActivity(intent);
    }
}
