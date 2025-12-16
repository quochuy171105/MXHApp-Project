package huynguyen.com.MXHApp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import huynguyen.com.MXHApp.Adapter.MessageAdapter;
import huynguyen.com.MXHApp.Model.ChatMessage;
import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.databinding.ActivityChatBinding;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private FirebaseUser fUser;
    private String receiverId;
    private String chatRoomId;

    private MessageAdapter messageAdapter;
    private List<ChatMessage> mChatMessages;

    private DatabaseReference messagesRef;
    private ValueEventListener seenListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        receiverId = getIntent().getStringExtra("userId");

        chatRoomId = createChatRoomId(fUser.getUid(), receiverId);
        messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(chatRoomId);

        setupRecyclerView();
        loadReceiverInfo();

        binding.sendButton.setOnClickListener(v -> {
            String message = binding.messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                sendMessage(message);
            }
        });
    }

    private void sendMessage(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("senderId", fUser.getUid());
        messageMap.put("receiverId", receiverId);
        messageMap.put("message", message);
        messageMap.put("timestamp", ServerValue.TIMESTAMP);
        messageMap.put("isSeen", false);

        databaseReference.child("chats").child(chatRoomId).push().setValue(messageMap);

        // Update conversation log for the sender
        DatabaseReference senderConvRef = databaseReference.child("user-chats").child(fUser.getUid()).child(receiverId);
        Map<String, Object> senderConvMap = new HashMap<>();
        senderConvMap.put("id", receiverId);
        senderConvMap.put("lastMessage", message);
        senderConvMap.put("timestamp", ServerValue.TIMESTAMP);
        senderConvMap.put("lastMessageSenderId", fUser.getUid());
        senderConvMap.put("seen", true); // Sender always sees their own message
        senderConvRef.updateChildren(senderConvMap);

        // Update conversation log for the receiver
        DatabaseReference receiverConvRef = databaseReference.child("user-chats").child(receiverId).child(fUser.getUid());
        Map<String, Object> receiverConvMap = new HashMap<>();
        receiverConvMap.put("id", fUser.getUid());
        receiverConvMap.put("lastMessage", message);
        receiverConvMap.put("timestamp", ServerValue.TIMESTAMP);
        receiverConvMap.put("lastMessageSenderId", fUser.getUid());
        receiverConvMap.put("seen", false); // Receiver has not seen it yet
        receiverConvRef.updateChildren(receiverConvMap);

        binding.messageInput.setText("");
    }

    private void readMessages(String receiverImageUrl) {
        mChatMessages = new ArrayList<>();
        messageAdapter = new MessageAdapter(ChatActivity.this, mChatMessages, receiverImageUrl);
        binding.recyclerViewMessages.setAdapter(messageAdapter);

        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChatMessages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                    if (chatMessage != null) {
                        mChatMessages.add(chatMessage);
                    }
                }
                if (messageAdapter != null) {
                    messageAdapter.notifyDataSetChanged();
                    binding.recyclerViewMessages.scrollToPosition(mChatMessages.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Failed to read messages.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void seenMessage(){
        seenListener = messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                    if (chatMessage != null && chatMessage.getReceiverId().equals(fUser.getUid()) && !chatMessage.isSeen()){
                        snapshot.getRef().child("seen").setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void updateConversationAsSeen() {
        DatabaseReference conversationRef = FirebaseDatabase.getInstance()
                .getReference("user-chats").child(fUser.getUid()).child(receiverId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("seen", true);
        conversationRef.updateChildren(updates);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateConversationAsSeen();
        seenMessage();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (seenListener != null) {
            messagesRef.removeEventListener(seenListener);
        }
    }

    // Other existing methods (createChatRoomId, setupRecyclerView, loadReceiverInfo) go here...
    private String createChatRoomId(String uid1, String uid2) {
        if (uid1.compareTo(uid2) > 0) {
            return uid1 + uid2;
        } else {
            return uid2 + uid1;
        }
    }

    private void setupRecyclerView() {
        binding.recyclerViewMessages.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        binding.recyclerViewMessages.setLayoutManager(linearLayoutManager);
    }

    private void loadReceiverInfo() {
        FirebaseFirestore.getInstance().collection("users").document(receiverId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            binding.username.setText(user.getUsername());
                            String imageUrl = user.getProfileUrl();
                            if (imageUrl != null && !imageUrl.equals("default")) {
                                Glide.with(getApplicationContext()).load(imageUrl).into(binding.profileImage);
                            } else {
                                binding.profileImage.setImageResource(R.drawable.profile_image);
                            }
                            readMessages(imageUrl);
                        }
                    }
                });
    }
}
