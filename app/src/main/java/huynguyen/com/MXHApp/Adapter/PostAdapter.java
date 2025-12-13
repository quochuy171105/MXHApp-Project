package huynguyen.com.MXHApp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import huynguyen.com.MXHApp.CommentActivity;
import huynguyen.com.MXHApp.HomeActivity;
import huynguyen.com.MXHApp.Model.PostItem;
import huynguyen.com.MXHApp.R;
import huynguyen.com.MXHApp.ShowList;
import huynguyen.com.MXHApp.databinding.PostItemBinding;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    public Context mContext;
    public List<PostItem> mPost;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;

    public PostAdapter(Context mContext, List<PostItem> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PostItemBinding binding = PostItemBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final PostItem postItem = mPost.get(position);

        // TODO: Bind all post and user data to the views in post_item.xml
        // (e.g., profile image, username, post image, description, etc.)

        // TODO: Set up click listeners for all interactive elements:
        // - Like button
        // - Comment button
        // - Save button
        // - Profile image/username to navigate to user's profile
        // - Likes count text to show list of users who liked
        // - Options menu (if any)

    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public PostItemBinding binding;

        public ViewHolder(PostItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private void isLiked(String postid, ImageView imageView) {
        // TODO: Check if the current user has liked this post
    }

    private void isSaved(String postid, ImageView imageView) {
        // TODO: Check if the current user has saved this post
    }

    private void nrLikes(TextView likes, String postid) {
        // TODO: Get and display the total number of likes for this post
    }

    private void getComments(String postid, TextView comments) {
        // TODO: Get and display the total number of comments for this post
    }
}
