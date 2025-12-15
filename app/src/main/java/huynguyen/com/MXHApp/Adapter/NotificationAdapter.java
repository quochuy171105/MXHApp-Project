package huynguyen.com.MXHApp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import huynguyen.com.MXHApp.Model.Notifications;
import huynguyen.com.MXHApp.Model.Posts;
import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.PostDetails;
import huynguyen.com.MXHApp.R;
import huynguyen.com.MXHApp.databinding.NotificationItemBinding;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context mContext;
    private List<Notifications> mNotifications;

    public NotificationAdapter(Context mContext, List<Notifications> mNotifications) {
        this.mContext = mContext;
        this.mNotifications = mNotifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NotificationItemBinding binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Notifications notification = mNotifications.get(position);

        holder.binding.comment.setText(notification.getComment());
        getUserInfo(holder);
        if (notification.isIspost()) {
            holder.binding.postImage.setVisibility(View.VISIBLE);
            getPostImage(holder);
        } else {
            holder.binding.postImage.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (notification.isIspost()) {
                // Correct way: Pass data through the Intent
                Intent intent = new Intent(mContext, PostDetails.class);
                intent.putExtra("postid", notification.getPostid());
                mContext.startActivity(intent);
            } else {
                // Handle click on follow notification if needed
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        NotificationItemBinding binding;

        public ViewHolder(NotificationItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private void getUserInfo(final ViewHolder holder) {
        String userid = mNotifications.get(holder.getAdapterPosition()).getUserid();
        FirebaseFirestore.getInstance().collection("users").document(userid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = task.getResult().toObject(User.class);
                if (user != null && mContext != null) {
                    holder.binding.username.setText(user.getUsername());
                    Glide.with(mContext).load(user.getProfileUrl()).into(holder.binding.profileImage);
                }
            }
        });
    }

    private void getPostImage(final ViewHolder holder) {
        String postid = mNotifications.get(holder.getAdapterPosition()).getPostid();
        FirebaseFirestore.getInstance().collection("posts").document(postid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Posts post = task.getResult().toObject(Posts.class);
                if (post != null && mContext != null) {
                    Glide.with(mContext).load(post.getPostImage()).into(holder.binding.postImage);
                }
            }
        });
    }
}
