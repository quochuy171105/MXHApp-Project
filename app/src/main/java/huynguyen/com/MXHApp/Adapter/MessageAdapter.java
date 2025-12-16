package huynguyen.com.MXHApp.Adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import huynguyen.com.MXHApp.Model.ChatMessage;
import huynguyen.com.MXHApp.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<ChatMessage> mChatMessages;
    private String mImageUrl;

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

        holder.messageText.setText(chatMessage.getMessage());

        // Set timestamp
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

        // Logic for seen status
        if (position == mChatMessages.size() - 1) {
            if (holder.seenStatus != null) { // Check if the view exists (it only exists for MSG_TYPE_RIGHT)
                if (chatMessage.isSeen()) {
                    holder.seenStatus.setText("Seen");
                    holder.seenStatus.setVisibility(View.VISIBLE);
                } else {
                    holder.seenStatus.setText("Delivered");
                    holder.seenStatus.setVisibility(View.VISIBLE);
                }
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
        public TextView messageTimestamp; // Added this

        public ViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            profileImage = itemView.findViewById(R.id.profile_image);
            seenStatus = itemView.findViewById(R.id.seen_status);
            messageTimestamp = itemView.findViewById(R.id.message_timestamp); // Added this
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
