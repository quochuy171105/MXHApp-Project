package huynguyen.com.MXHApp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import huynguyen.com.MXHApp.ChatActivity;
import huynguyen.com.MXHApp.Model.Conversation;
import huynguyen.com.MXHApp.R;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ViewHolder> {

    private Context mContext;
    private List<Conversation> mConversations;
    private FirebaseUser fUser;

    public ConversationsAdapter(Context mContext, List<Conversation> mConversations) {
        this.mContext = mContext;
        this.mConversations = mConversations;
        this.fUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.conversation_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = mConversations.get(position);

        holder.username.setText(conversation.getUsername());
        holder.lastMessage.setText(conversation.getLastMessage());

        if (conversation.getTimestamp() > 0) {
            holder.timestamp.setText(DateUtils.getRelativeTimeSpanString(conversation.getTimestamp()));
        }

        if (conversation.getProfileUrl() != null && !conversation.getProfileUrl().equals("default")) {
            Glide.with(mContext).load(conversation.getProfileUrl()).into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.profile_image);
        }

        // Unread message indicator logic
        boolean isLastMessageFromOtherUser = conversation.getLastMessageSenderId() != null && !conversation.getLastMessageSenderId().equals(fUser.getUid());
        if (isLastMessageFromOtherUser && !conversation.isSeen()) {
            holder.username.setTypeface(null, Typeface.BOLD);
            holder.lastMessage.setTypeface(null, Typeface.BOLD);
            holder.lastMessage.setTextColor(ContextCompat.getColor(mContext, android.R.color.primary_text_light));
        } else {
            holder.username.setTypeface(null, Typeface.NORMAL);
            holder.lastMessage.setTypeface(null, Typeface.NORMAL);
            holder.lastMessage.setTextColor(ContextCompat.getColor(mContext, android.R.color.secondary_text_light));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.putExtra("userId", conversation.getId());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mConversations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public TextView lastMessage;
        public TextView timestamp;
        public CircleImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            lastMessage = itemView.findViewById(R.id.last_message);
            timestamp = itemView.findViewById(R.id.timestamp);
            profileImage = itemView.findViewById(R.id.profile_image);
        }
    }
}
