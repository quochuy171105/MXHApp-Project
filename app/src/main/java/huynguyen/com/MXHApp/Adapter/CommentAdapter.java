package huynguyen.com.MXHApp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;

import huynguyen.com.MXHApp.Model.Comment;
import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.OthersProfile;
import huynguyen.com.MXHApp.R;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private static final String TAG = "CommentAdapter";

    private Context mContext;
    private List<Comment> mComment;
    private String postId;
    private String postPublisherId;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;

    public CommentAdapter(Context mContext, List<Comment> mComment, String postId, String postPublisherId) {
        this.mContext = mContext;
        this.mComment = mComment;
        this.postId = postId;
        this.postPublisherId = postPublisherId;
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Comment comment = mComment.get(position);

        holder.comment.setText(comment.getComment());
        getUserInfo(holder.profile_image, holder.username, comment.getPublisher());

        // --- Like Comment Logic ---
        isCommentLiked(comment.getCommentid(), holder.like_comment_image);
        nrCommentLikes(comment.getCommentid(), holder.like_comment_count);
        holder.like_comment_image.setOnClickListener(v -> {
            if (firebaseUser != null) {
                toggleCommentLike(comment, holder.like_comment_image);
            }
        });

        // --- Profile Click Logic ---
        View.OnClickListener profileClickListener = v -> {
            if (firebaseUser != null && comment.getPublisher().equals(firebaseUser.getUid())) {
                // User clicked their own profile, do nothing.
            } else {
                Intent intent = new Intent(mContext, OthersProfile.class);
                intent.putExtra("uid", comment.getPublisher());
                mContext.startActivity(intent);
            }
        };
        holder.username.setOnClickListener(profileClickListener);
        holder.profile_image.setOnClickListener(profileClickListener);

        // --- Delete Comment Logic ---
        holder.itemView.setOnLongClickListener(v -> {
            if (firebaseUser != null && comment.getPublisher() != null) {
                if (comment.getPublisher().equals(firebaseUser.getUid()) || postPublisherId.equals(firebaseUser.getUid())) {
                    showDeleteDialog(comment);
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profile_image, like_comment_image;
        public TextView username, comment, like_comment_count;

        public ViewHolder(View itemView) {
            super(itemView);
            profile_image = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            like_comment_image = itemView.findViewById(R.id.like_comment_image);
            like_comment_count = itemView.findViewById(R.id.like_comment_count);
        }
    }

    private void isCommentLiked(String commentId, ImageView imageView) {
        if (firebaseUser == null || postId == null || commentId == null) return;
        firestore.collection("posts").document(postId).collection("comments").document(commentId)
                .collection("likes").document(firebaseUser.getUid())
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

    private void nrCommentLikes(String commentId, TextView textView) {
        if(postId == null || commentId == null) return;
        firestore.collection("posts").document(postId).collection("comments").document(commentId)
                .collection("likes").addSnapshotListener((snapshots, e) -> {
                    if (e != null) { Log.e(TAG, "Listen failed.", e); return; }
                    if (snapshots != null) {
                        int likeCount = snapshots.size();
                        if (likeCount > 0) {
                            textView.setText(String.valueOf(likeCount));
                            textView.setVisibility(View.VISIBLE);
                        } else {
                            textView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void toggleCommentLike(Comment comment, ImageView imageView) {
        if (firebaseUser == null || postId == null || comment.getCommentid() == null) return;
        DocumentReference likeRef = firestore.collection("posts").document(postId).collection("comments").document(comment.getCommentid())
                .collection("likes").document(firebaseUser.getUid());

        if (imageView.getTag().equals("like")) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("timestamp", FieldValue.serverTimestamp());
            likeRef.set(map);

            if (!comment.getPublisher().equals(firebaseUser.getUid())) {
                addLikeNotification(comment.getPublisher(), postId, comment.getComment());
            }
        } else {
            likeRef.delete();
        }
    }

    private void addLikeNotification(String commentPublisherId, String postId, String commentText) {
        if (firebaseUser == null || commentPublisherId == null || postId == null) return;
        String notificationText = "liked your comment: " + (commentText.length() > 20 ? commentText.substring(0, 20) + "..." : commentText);
        HashMap<String, Object> map = new HashMap<>();
        map.put("userid", firebaseUser.getUid());
        map.put("comment", notificationText);
        map.put("postid", postId);
        // REFACTORED: Changed field name for better deserialization
        map.put("post", true);
        map.put("timestamp", FieldValue.serverTimestamp());
        firestore.collection("users").document(commentPublisherId).collection("notifications").add(map);
    }

    private void getUserInfo(final ImageView imageView, final TextView username, String publisherid) {
        if (publisherid == null || publisherid.isEmpty()) return;
        firestore.collection("users").document(publisherid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    username.setText(user.getUsername());
                    if (user.getProfileUrl() != null && !user.getProfileUrl().isEmpty()) {
                        Glide.with(mContext).load(user.getProfileUrl()).into(imageView);
                    } else {
                        imageView.setImageResource(R.drawable.profile_image);
                    }
                }
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error getting user info", e));
    }

    private void showDeleteDialog(final Comment comment) {
        new AlertDialog.Builder(mContext)
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Delete", (dialog, which) -> deleteComment(comment))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteComment(Comment comment) {
        if (postId == null || comment.getCommentid() == null) {
            Toast.makeText(mContext, "Error: Cannot find comment to delete.", Toast.LENGTH_SHORT).show();
            return;
        }
        firestore.collection("posts").document(postId)
                .collection("comments").document(comment.getCommentid())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(mContext, "Comment deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(mContext, "Failed to delete comment", Toast.LENGTH_SHORT).show());
    }
}
