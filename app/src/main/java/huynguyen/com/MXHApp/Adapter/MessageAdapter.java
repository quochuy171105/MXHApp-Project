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

import java.util.List;

import huynguyen.com.MXHApp.FullImageActivity; // Make sure this import is correct or will be created
import huynguyen.com.MXHApp.Model.Chat;
import huynguyen.com.MXHApp.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageUrl; // Other user's avatar
    private String myImageUrl; // Current user's avatar

    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageUrl, String myImageUrl) {
        this.mContext = mContext;
        this.mChat = mChat;
        this.imageUrl = imageUrl;
        this.myImageUrl = myImageUrl;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chat chat = mChat.get(position);
        if (chat == null) return;

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        String type = chat.getType();
        String message = chat.getMessage();

        if (type != null && type.equals("text")) {
            holder.show_message.setVisibility(View.VISIBLE);
            holder.show_image.setVisibility(View.GONE);
            holder.show_message.setText(message != null ? message : "");
        } else if (type != null && type.equals("image")) {
            holder.show_message.setVisibility(View.GONE);
            holder.show_image.setVisibility(View.VISIBLE);
            if (message != null && !message.isEmpty()) {
                Glide.with(mContext).load(message).into(holder.show_image);

                holder.show_image.setOnClickListener(v -> {
                    Intent intent = new Intent(mContext, FullImageActivity.class);
                    intent.putExtra("imageUrl", message);
                    mContext.startActivity(intent);
                });
            }
        } else {
            holder.show_message.setVisibility(View.VISIBLE);
            holder.show_image.setVisibility(View.GONE);
            holder.show_message.setText(message != null ? message : "");
        }

        // Logic to show avatar
        if (getItemViewType(position) == MSG_TYPE_RIGHT) {
            // My avatar
            if (holder.profile_image != null) {
                if (myImageUrl != null && !myImageUrl.equals("default") && !myImageUrl.isEmpty()) {
                    try {
                        Glide.with(mContext).load(myImageUrl).placeholder(R.mipmap.ic_launcher).into(holder.profile_image);
                    } catch (Exception e) {
                        holder.profile_image.setImageResource(R.mipmap.ic_launcher);
                    }
                } else {
                    holder.profile_image.setImageResource(R.mipmap.ic_launcher);
                }
            }
        } else {
            // Other user's avatar
            if (holder.profile_image != null) {
                if (imageUrl != null && !imageUrl.equals("default") && !imageUrl.isEmpty()) {
                    try {
                        Glide.with(mContext).load(imageUrl).placeholder(R.mipmap.ic_launcher).into(holder.profile_image);
                    } catch (Exception e) {
                        holder.profile_image.setImageResource(R.mipmap.ic_launcher);
                    }
                } else {
                    holder.profile_image.setImageResource(R.mipmap.ic_launcher);
                }
            }
        }

        if (position == mChat.size() - 1) {
            if (chat.isIsseen()) {
                holder.txt_seen.setText("Seen");
            } else {
                holder.txt_seen.setText("Delivered");
            }
            holder.txt_seen.setVisibility(View.VISIBLE);
        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public ImageView profile_image;
        public TextView txt_seen;
        public ImageView show_image;

        public ViewHolder(View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            show_image = itemView.findViewById(R.id.show_image);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        Chat chat = mChat.get(position);
        if (fuser != null && chat != null && chat.getSender() != null && chat.getSender().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
