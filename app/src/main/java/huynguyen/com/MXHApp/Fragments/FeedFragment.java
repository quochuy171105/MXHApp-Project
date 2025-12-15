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
import huynguyen.com.MXHApp.databinding.FragmentFeedBinding;

import static android.content.Context.MODE_PRIVATE;

public class FeedFragment extends Fragment {

    private static final String TAG = "FeedFragment";

    private FragmentFeedBinding binding;
    private PostAdapter postAdapter;
    private List<PostItem> postItemList;

    private FirebaseFirestore firestore;
    private FirebaseUser firebaseUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postItemList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postItemList);
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

    private void loadPosts() {
        if (getActivity() == null) return;

        firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (getActivity() == null || binding == null) return;
                    postItemList.clear();
                    List<CompletableFuture<PostItem>> futures = new ArrayList<>();

                    for (QueryDocumentSnapshot postSnapshot : queryDocumentSnapshots) {
                        Posts post = postSnapshot.toObject(Posts.class);
                        if (post.getPublisher() == null) continue;

                        CompletableFuture<PostItem> future = fetchPostDetails(post);
                        futures.add(future);
                    }

                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .thenRun(() -> {
                                if (getActivity() == null) return;
                                getActivity().runOnUiThread(() -> {
                                    postItemList.clear();
                                    for(CompletableFuture<PostItem> f : futures) {
                                        try {
                                            postItemList.add(f.get());
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error getting post item from future", e);
                                        }
                                    }
                                    postAdapter.notifyDataSetChanged();
                                });
                            });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching posts", e));
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
            if (firebaseUser != null) {
                for (DocumentSnapshot doc : likes) {
                    if (doc.getId().equals(firebaseUser.getUid())) {
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
        if (firebaseUser != null) {
            firestore.collection("users").document(firebaseUser.getUid()).collection("saved_posts").document(post.getPostid()).get().addOnSuccessListener(doc -> {
                isSavedFuture.complete(doc.exists());
            }).addOnFailureListener(isSavedFuture::completeExceptionally);
        } else {
            isSavedFuture.complete(false);
        }

        CompletableFuture.allOf(userFuture, likeCountFuture, isLikedFuture, commentCountFuture, isSavedFuture)
                .thenRun(() -> {
                    try {
                        User user = userFuture.get();
                        long likeCount = likeCountFuture.get();
                        boolean isLiked = isLikedFuture.get();
                        long commentCount = commentCountFuture.get();
                        boolean isSaved = isSavedFuture.get();
                        future.complete(new PostItem(post, user, likeCount, commentCount, isLiked, isSaved));
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                });

        return future;
    }
}
