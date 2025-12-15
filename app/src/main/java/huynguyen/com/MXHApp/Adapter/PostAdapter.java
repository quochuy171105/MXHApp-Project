package huynguyen.com.MXHApp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.Map;

import huynguyen.com.MXHApp.CommentActivity;
import huynguyen.com.MXHApp.Model.PostItem;
import huynguyen.com.MXHApp.Model.Posts;
import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.R;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context mContext;
    private List<PostItem> mPostItems;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;

    public PostAdapter(Context mContext, List<PostItem> mPostItems) {
        this.mContext = mContext;
        this.mPostItems = mPostItems;
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
        final PostItem postItem = mPostItems.get(position);
        final Posts post = postItem.getPost();
        final User user = postItem.getUser();

        if (post == null || user == null) return;

        // Set Post Image
        Glide.with(mContext).load(post.getPostImage()).into(holder.postImage);

        // Set Description
        if (post.getDescription() != null && !post.getDescription().isEmpty()) {
            holder.description.setText(post.getDescription());
            holder.description.setVisibility(View.VISIBLE);
        } else {
            holder.description.setVisibility(View.GONE);
        }

        // Set Publisher Info
        holder.username.setText(user.getUsername());
        Glide.with(mContext).load(user.getProfileUrl()).placeholder(R.drawable.profile_image).into(holder.profileImage);

        // Set Post Time
        if (post.getTimestamp() != null) {
            holder.postTime.setText(DateUtils.getRelativeTimeSpanString(post.getTimestamp().getTime()));
        } else {
            holder.postTime.setText("");
        }

        // Set Like Info
        holder.likesCount.setText(postItem.getLikeCount() + " likes");
        holder.like.setImageResource(postItem.isLiked() ? R.drawable.ic_liked : R.drawable.ic_like);
        holder.like.setTag(postItem.isLiked() ? "liked" : "like");

        // Set Comment Info
        holder.commentsCount.setText("View all " + postItem.getCommentCount() + " comments");

        // Set Save Info
        holder.save.setImageResource(postItem.isSaved() ? R.drawable.ic_saved : R.drawable.ic_save);
        holder.save.setTag(postItem.isSaved() ? "saved" : "save");

        // --- Click Listeners ---
        holder.like.setOnClickListener(v -> toggleLike(postItem, holder));
        holder.comment.setOnClickListener(v -> openComments(post.getPostid(), post.getPublisher()));
        holder.commentsCount.setOnClickListener(v -> openComments(post.getPostid(), post.getPublisher()));
        holder.save.setOnClickListener(v -> toggleSave(postItem, holder));

    }

    @Override
    public int getItemCount() {
        return mPostItems.size();
    }

    private void toggleLike(PostItem postItem, ViewHolder holder) {
        if (firebaseUser == null) return;
        DocumentReference likeRef = firestore.collection("posts").document(postItem.getPost().getPostid()).collection("likes").document(firebaseUser.getUid());

        boolean currentlyLiked = postItem.isLiked();
        postItem.setLiked(!currentlyLiked);
        postItem.setLikeCount(postItem.getLikeCount() + (currentlyLiked ? -1 : 1));
        notifyItemChanged(holder.getAdapterPosition(), "PAYLOAD_LIKE");

        if (!currentlyLiked) {
            Map<String, Object> likeMap = new HashMap<>();
            likeMap.put("timestamp", FieldValue.serverTimestamp());
            likeRef.set(likeMap);
            if (!postItem.getPost().getPublisher().equals(firebaseUser.getUid())) {
                addNotification(postItem.getPost().getPostid(), postItem.getPost().getPublisher(), "liked your post");
            }
        } else {
            likeRef.delete();
        }
    }

    private void toggleSave(PostItem postItem, ViewHolder holder) {
        if(firebaseUser == null) return;
        DocumentReference saveRef = firestore.collection("users").document(firebaseUser.getUid()).collection("saved_posts").document(postItem.getPost().getPostid());

        boolean currentlySaved = postItem.isSaved();
        postItem.setSaved(!currentlySaved);
        notifyItemChanged(holder.getAdapterPosition(), "PAYLOAD_SAVE");

        if(!currentlySaved) {
            Map<String, Object> saveMap = new HashMap<>();
            saveMap.put("saved_at", FieldValue.serverTimestamp());
            saveRef.set(saveMap);
        } else {
            saveRef.delete();
        }
    }

    private void openComments(String postId, String publisherId) {
        Intent intent = new Intent(mContext, CommentActivity.class);
        intent.putExtra("postid", postId);
        intent.putExtra("publisher", publisherId);
        mContext.startActivity(intent);
    }

    private void addNotification(String postid, String publisherid, String comment) {
        if (firebaseUser == null || publisherid == null) return;
        HashMap<String, Object> map = new HashMap<>();
        map.put("userid", firebaseUser.getUid());
        map.put("comment", comment);
        map.put("postid", postid);
        map.put("ispost", true);
        map.put("timestamp", FieldValue.serverTimestamp());
        firestore.collection("users").document(publisherid).collection("notifications").add(map);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImage, postImage, like, comment, save, more;
        public TextView username, likesCount, publisher, description, commentsCount, postTime;

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
}
