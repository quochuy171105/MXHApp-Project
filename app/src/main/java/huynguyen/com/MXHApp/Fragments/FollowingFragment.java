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

import static android.content.Context.MODE_PRIVATE;

public class FollowingFragment extends Fragment {

    private static final String TAG = "FollowingFragment";

    // Use ViewBinding
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

        // Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        postItemList = new ArrayList<>();
        adapter = new PostAdapter(getActivity(), postItemList);
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
                    // This might need a safer way to navigate
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
        if (postsListener != null) {
            postsListener.remove();
        }
        firestore.collection("users").document(user.getUid()).collection("following")
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (binding == null) return; // Check if fragment is still alive
                    List<String> followingList = new ArrayList<>();
                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            followingList.add(doc.getId());
                        }
                        loadFollowingPosts(followingList);
                        binding.no.setVisibility(View.GONE);
                        binding.discover.setVisibility(View.GONE);
                    } else {
                        postItemList.clear();
                        if(adapter != null) adapter.notifyDataSetChanged();
                        binding.no.setVisibility(View.VISIBLE);
                        binding.discover.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void loadFollowingPosts(List<String> followingList) {
        if (followingList == null || followingList.isEmpty()) return;

        postsListener = firestore.collection("posts").whereIn("publisher", followingList)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (binding == null) return;
                    if (error != null) {
                        Log.e(TAG, "Listen for following posts failed", error);
                        return;
                    }
                    if (queryDocumentSnapshots == null) return;

                    List<CompletableFuture<PostItem>> futures = new ArrayList<>();
                    for (QueryDocumentSnapshot postSnapshot : queryDocumentSnapshots) {
                        Posts post = postSnapshot.toObject(Posts.class);
                        if (post.getPublisher() == null) continue;
                        futures.add(fetchPostDetails(post));
                    }

                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .thenRun(() -> {
                                if (getActivity() == null || binding == null) return;
                                getActivity().runOnUiThread(() -> {
                                    postItemList.clear();
                                    for (CompletableFuture<PostItem> f : futures) {
                                        try {
                                            postItemList.add(f.get());
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error getting post item from future", e);
                                        }
                                    }
                                    if(adapter != null) adapter.notifyDataSetChanged();
                                });
                            });
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
            if (user != null) {
                for (DocumentSnapshot doc : likes) {
                    if (doc.getId().equals(user.getUid())) {
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
        if (user != null) {
            firestore.collection("users").document(user.getUid()).collection("saved_posts").document(post.getPostid()).get().addOnSuccessListener(doc -> {
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
