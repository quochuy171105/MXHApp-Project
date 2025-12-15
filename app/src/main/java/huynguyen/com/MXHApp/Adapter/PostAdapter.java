package huynguyen.com.MXHApp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.List;

import huynguyen.com.MXHApp.CommentActivity;
import huynguyen.com.MXHApp.HomeActivity;
import huynguyen.com.MXHApp.Model.Posts;
import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.PostActivity;
import huynguyen.com.MXHApp.R;

import static android.content.Context.MODE_PRIVATE;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private static final String TAG = "PostAdapter";

    private Context mContext;
    private List<Posts> mPosts;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;

    public PostAdapter(Context mContext, List<Posts> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Posts post = mPosts.get(position);

        // --- 1. Set basic info ---
        Glide.with(mContext).load(post.getPostImage()).into(holder.postImage);
        if (post.getDescription() != null && !post.getDescription().isEmpty()) {
            holder.description.setText(post.getDescription());
            holder.description.setVisibility(View.VISIBLE);
        } else {
            holder.description.setVisibility(View.GONE);
        }

        if (post.getTimestamp() != null) {
            holder.postTime.setText(DateUtils.getRelativeTimeSpanString(post.getTimestamp().getTime()));
        } else {
            holder.postTime.setText("");
        }

        // --- 2. Fetch related data ---
        publisherInfo(holder.profileImage, holder.username, post.getPublisher());
        nrLikes(holder.likesCount, post.getPostid());
        getComments(holder.commentsCount, post.getPostid());
        isLiked(post.getPostid(), holder.like);
        isSaved(post.getPostid(), holder.save);

        // --- 3. Click Listeners ---
        holder.like.setOnClickListener(v -> toggleLike(post.getPostid(), holder.like));
        holder.save.setOnClickListener(v -> toggleSave(post.getPostid(), holder.save));

        View.OnClickListener commentClickListener = v -> {
            Intent intent = new Intent(mContext, CommentActivity.class);
            intent.putExtra("postid", post.getPostid());
            intent.putExtra("publisher", post.getPublisher());
            mContext.startActivity(intent);
        };
        holder.comment.setOnClickListener(commentClickListener);
        holder.commentsCount.setOnClickListener(commentClickListener);

        // REFACTORED: The click listener now calls the improved openProfile method
        holder.profileImage.setOnClickListener(v -> openProfile(post.getPublisher()));
        holder.username.setOnClickListener(v -> openProfile(post.getPublisher()));

        // --- 4. Post Options (Edit/Delete) ---
        if (firebaseUser != null && post.getPublisher().equals(firebaseUser.getUid())) {
            holder.more.setVisibility(View.VISIBLE);
            holder.more.setOnClickListener(v -> showPostOptionsDialog(post));
        } else {
            holder.more.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    // --- ViewHolder and Data Fetching Methods ---

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImage, postImage, like, comment, save, more;
        public TextView username, likesCount, publisher, description, commentsCount, postTime;

        ListenerRegistration likesListener, commentsListener, savedListener, userListener, isLikedListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            postImage = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            more = itemView.findViewById(R.id.more);
            username = itemView.findViewById(R.id.username);
            likesCount = itemView.findViewById(R.id.likes_count);
            publisher = itemView.findViewById(R.id.publisher);
            description = itemView.findViewById(R.id.description);
            commentsCount = itemView.findViewById(R.id.comments_count);
            postTime = itemView.findViewById(R.id.post_time);
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.likesListener != null) holder.likesListener.remove();
        if (holder.commentsListener != null) holder.commentsListener.remove();
        if (holder.savedListener != null) holder.savedListener.remove();
        if (holder.userListener != null) holder.userListener.remove();
        if (holder.isLikedListener != null) holder.isLikedListener.remove();
    }

    private void publisherInfo(ImageView profileImage, TextView username, String userid) {
        if (userid == null) return;
        firestore.collection("users").document(userid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    username.setText(user.getUsername());
                    Glide.with(mContext).load(user.getProfileUrl()).placeholder(R.drawable.profile_image).into(profileImage);
                }
            }
        });
    }

    private void nrLikes(TextView likes, String postid) {
        firestore.collection("posts").document(postid).collection("likes").addSnapshotListener((snapshots, e) -> {
            if (e != null) { Log.e(TAG, "Listen failed.", e); return; }
            if (snapshots != null) {
                likes.setText(snapshots.size() + " likes");
            }
        });
    }

    private void getComments(TextView comments, String postid) {
        firestore.collection("posts").document(postid).collection("comments").addSnapshotListener((snapshots, e) -> {
            if (e != null) { Log.e(TAG, "Listen failed.", e); return; }
            if (snapshots != null) {
                comments.setText("View all " + snapshots.size() + " comments");
            }
        });
    }

    private void isLiked(String postid, ImageView imageView) {
        if (firebaseUser == null) return;
        firestore.collection("posts").document(postid).collection("likes").document(firebaseUser.getUid())
            .addSnapshotListener((snapshot, e) -> {
                if (e != null) { Log.e(TAG, "Listen failed.", e); return; }
                if (snapshot != null && snapshot.exists()) {
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            });
    }

    private void isSaved(String postid, ImageView imageView) {
        if (firebaseUser == null) return;
        firestore.collection("users").document(firebaseUser.getUid()).collection("saved_posts").document(postid)
            .addSnapshotListener((snapshot, e) -> {
                if (e != null) { Log.e(TAG, "Listen failed.", e); return; }
                if (snapshot != null && snapshot.exists()) {
                    imageView.setImageResource(R.drawable.ic_saved);
                    imageView.setTag("saved");
                } else {
                    imageView.setImageResource(R.drawable.ic_save);
                    imageView.setTag("save");
                }
            });
    }

    private void toggleLike(String postid, ImageView imageView) {
        if (firebaseUser == null) return;
        DocumentReference likeRef = firestore.collection("posts").document(postid).collection("likes").document(firebaseUser.getUid());
        if (imageView.getTag().equals("like")) {
            likeRef.set(new HashMap<>());
        } else {
            likeRef.delete();
        }
    }

    private void toggleSave(String postid, ImageView imageView) {
        if (firebaseUser == null) return;
        DocumentReference saveRef = firestore.collection("users").document(firebaseUser.getUid()).collection("saved_posts").document(postid);
        if (imageView.getTag().equals("save")) {
            saveRef.set(new HashMap<>());
        } else {
            saveRef.delete();
        }
    }

    // REFACTORED: This method now also triggers navigation
    private void openProfile(String userId) {
        if (mContext == null || userId == null) return;
        
        // 1. Save the target profile ID
        SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("profileid", userId);
        editor.apply();

        // 2. Trigger navigation
        if (mContext instanceof HomeActivity) {
            // If we are in a fragment hosted by HomeActivity, we can switch tabs directly.
            ((HomeActivity) mContext).getBinding().bottomNav.setSelectedItemId(R.id.profile);
        } else {
            // If we are in another activity (e.g., PostDetails), start HomeActivity fresh.
            Intent intent = new Intent(mContext, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        }
    }
    
    private void showPostOptionsDialog(final Posts post) {
        String[] options = {"Edit", "Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Edit
                Intent intent = new Intent(mContext, PostActivity.class);
                intent.putExtra("postIdToEdit", post.getPostid());
                mContext.startActivity(intent);
            } else if (which == 1) {
                // Delete
                new AlertDialog.Builder(mContext)
                    .setTitle("Delete Post")
                    .setMessage("Are you sure you want to delete this post?")
                    .setPositiveButton("Delete", (d, w) -> deletePost(post.getPostid()))
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
        builder.show();
    }

    private void deletePost(String postid) {
        if (postid == null) {
            Toast.makeText(mContext, "Error: Post ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        firestore.collection("posts").document(postid)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(mContext, "Post deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(mContext, "Failed to delete post", Toast.LENGTH_SHORT).show());
    }
}
