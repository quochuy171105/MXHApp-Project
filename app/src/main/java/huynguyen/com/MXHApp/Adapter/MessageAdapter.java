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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import huynguyen.com.MXHApp.FullImageActivity; // We will create this activity next
import huynguyen.com.MXHApp.Model.ChatMessage;
import huynguyen.com.MXHApp.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<ChatMessage> mChatMessages;
    private String mImageUrl; // Receiver's image URL

    private FirebaseUser fUser;

    public MessageAdapter(Context mContext, List<ChatMessage> mChatMessages, String mImageUrl) {
        this.mContext = mContext;
        this.mChatMessages = mChatMessages;
        this.mImageUrl = mImageUrl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.message_item_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.message_item_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage chatMessage = mChatMessages.get(position);

        boolean isPhoto = chatMessage.getType() != null && chatMessage.getType().equals("image");

        if (isPhoto) {
            holder.messageText.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(chatMessage.getMessage()).into(holder.messageImage);

            holder.messageImage.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, FullImageActivity.class);
                intent.putExtra("imageUrl", chatMessage.getMessage());
                mContext.startActivity(intent);
            });

        } else {
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.GONE);
            holder.messageText.setText(chatMessage.getMessage());
        }

        if (holder.messageTimestamp != null && chatMessage.getTimestamp() > 0) {
            holder.messageTimestamp.setText(DateUtils.getRelativeTimeSpanString(chatMessage.getTimestamp()));
        }

        if (holder.profileImage != null) {
            if (mImageUrl != null && !mImageUrl.equals("default")) {
                Glide.with(mContext).load(mImageUrl).into(holder.profileImage);
            } else {
                holder.profileImage.setImageResource(R.drawable.profile_image);
            }
        }

        if (position == mChatMessages.size() - 1) {
            if (holder.seenStatus != null) {
                if (chatMessage.isSeen()) {
                    holder.seenStatus.setText("Seen");
                } else {
                    holder.seenStatus.setText("Delivered");
                }
                holder.seenStatus.setVisibility(View.VISIBLE);
            }
        } else {
            if (holder.seenStatus != null) {
                holder.seenStatus.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mChatMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public CircleImageView profileImage;
        public TextView seenStatus;
        public TextView messageTimestamp;
        public ImageView messageImage; // ADDED

        public ViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            profileImage = itemView.findViewById(R.id.profile_image);
            seenStatus = itemView.findViewById(R.id.seen_status);
            messageTimestamp = itemView.findViewById(R.id.message_timestamp);
            messageImage = itemView.findViewById(R.id.message_image); // ADDED
        }
    }

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChatMessages.get(position).getSenderId().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
