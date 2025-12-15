package huynguyen.com.MXHApp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.OthersProfile;
import huynguyen.com.MXHApp.R;

public class ShowAdapter extends RecyclerView.Adapter<ShowAdapter.ViewHolder> {

    private static final String TAG = "ShowAdapter";

    private Context context;
    private List<User> userList;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;

    public ShowAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user, parent, false); // FIX: Corrected layout file name
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = userList.get(position);
        if (user == null || user.getUser_id() == null) return;

        holder.username.setText(user.getUsername());
        holder.memer.setText(user.getMemer());

        if (user.getProfileUrl() != null && !user.getProfileUrl().isEmpty()) {
            Glide.with(context).load(user.getProfileUrl()).placeholder(R.drawable.profile_image).into(holder.profile);
        } else {
            holder.profile.setImageResource(R.drawable.profile_image);
        }

        // Hide follow buttons if it's the current user
        if (firebaseUser != null && user.getUser_id().equals(firebaseUser.getUid())) {
            holder.btn_follow.setVisibility(View.GONE);
        } else {
            holder.btn_follow.setVisibility(View.VISIBLE);
            isFollowing(user.getUser_id(), holder.btn_follow);
        }

        holder.btn_follow.setOnClickListener(v -> {
            if(holder.btn_follow.getText().toString().equalsIgnoreCase("Follow")){
                followUser(user.getUser_id());
            } else {
                unfollowUser(user.getUser_id());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OthersProfile.class);
            intent.putExtra("uid", user.getUser_id());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profile;
        TextView username, memer;
        Button btn_follow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            memer = itemView.findViewById(R.id.memer);
            btn_follow = itemView.findViewById(R.id.btn_follow);
        }
    }

    private void isFollowing(final String userid, final Button button) {
        if (firebaseUser == null) return;
        DocumentReference followingRef = firestore.collection("users").document(firebaseUser.getUid()).collection("following").document(userid);
        followingRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                button.setText("Following");
            } else {
                button.setText("Follow");
            }
        });
    }

    private void followUser(String userId) {
        if (firebaseUser == null) return;
        WriteBatch batch = firestore.batch();
        DocumentReference followingRef = firestore.collection("users").document(firebaseUser.getUid()).collection("following").document(userId);
        batch.set(followingRef, new HashMap<>());
        DocumentReference followersRef = firestore.collection("users").document(userId).collection("followers").document(firebaseUser.getUid());
        batch.set(followersRef, new HashMap<>());
        batch.commit().addOnFailureListener(e -> Log.e(TAG, "Failed to follow user", e));
    }

    private void unfollowUser(String userId) {
        if (firebaseUser == null) return;
        WriteBatch batch = firestore.batch();
        DocumentReference followingRef = firestore.collection("users").document(firebaseUser.getUid()).collection("following").document(userId);
        batch.delete(followingRef);
        DocumentReference followersRef = firestore.collection("users").document(userId).collection("followers").document(firebaseUser.getUid());
        batch.delete(followersRef);
        batch.commit().addOnFailureListener(e -> Log.e(TAG, "Failed to unfollow user", e));
    }
}
