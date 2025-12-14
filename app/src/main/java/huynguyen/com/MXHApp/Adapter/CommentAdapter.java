package huynguyen.com.MXHApp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import huynguyen.com.MXHApp.MainActivity;
import huynguyen.com.MXHApp.Model.Comment;
import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.R;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private static final String TAG = "CommentAdapter";

    private Context mContext;
    private List<Comment> mComment;
    private String postid;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;

    public CommentAdapter(Context mContext, List<Comment> mComment, String postid) {
        this.mContext = mContext;
        this.mComment = mComment;
        this.postid = postid;
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

        View.OnClickListener profileClickListener = v -> {
            Intent intent = new Intent(mContext, MainActivity.class); // Or OthersProfileActivity
            intent.putExtra("publisherid", comment.getPublisher());
            mContext.startActivity(intent);
        };

        holder.username.setOnClickListener(profileClickListener);
        holder.profile_image.setOnClickListener(profileClickListener);
    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profile_image;
        public TextView username, comment;

        public ViewHolder(View itemView) {
            super(itemView);
            profile_image = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
        }
    }

    private void getUserInfo(final ImageView imageView, final TextView username, String publisherid) {
        if (publisherid == null || publisherid.isEmpty()) return;

        DocumentReference userRef = firestore.collection("users").document(publisherid);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
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
        }).addOnFailureListener(e -> Log.e(TAG, "Error getting user info for comment", e));
    }
}
