package huynguyen.com.MXHApp;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

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

    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    private StorageReference storageReference;

    // ADDED: For storing usernames
    private String senderUsername;
    private String receiverUsername;
    private String receiverImageUrl;

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

        storageReference = FirebaseStorage.getInstance().getReference("chat_images");

        chatRoomId = createChatRoomId(fUser.getUid(), receiverId);
        messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(chatRoomId);

        setupRecyclerView();
        loadUserInfos(); // MODIFIED: Load both users info

        binding.sendButton.setOnClickListener(v -> {
            String message = binding.messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(message)) {
                // Ensure usernames are loaded before sending
                if (senderUsername != null && receiverUsername != null) {
                    sendMessage(message, "text");
                } else {
                    Toast.makeText(this, "User info not loaded yet, please wait.", Toast.LENGTH_SHORT).show();
                }
            }
            binding.messageInput.setText("");
        });

        binding.attachButton.setOnClickListener(v -> {
            openImage();
        });
    }

    // MODIFIED: Now includes usernames
    private void sendMessage(String message, String type) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("senderId", fUser.getUid());
        messageMap.put("senderUsername", senderUsername); // ADDED
        messageMap.put("receiverId", receiverId);
        messageMap.put("receiverUsername", receiverUsername); // ADDED
        messageMap.put("message", message);
        messageMap.put("timestamp", ServerValue.TIMESTAMP);
        messageMap.put("isSeen", false);
        messageMap.put("type", type);

        databaseReference.child("chats").child(chatRoomId).push().setValue(messageMap);

        DatabaseReference senderConvRef = databaseReference.child("user-chats").child(fUser.getUid()).child(receiverId);
        Map<String, Object> senderConvMap = new HashMap<>();
        senderConvMap.put("id", receiverId);
        senderConvMap.put("username", receiverUsername); // ADDED
        senderConvMap.put("lastMessage", type.equals("image") ? "[Image]" : message);
        senderConvMap.put("timestamp", ServerValue.TIMESTAMP);
        senderConvMap.put("lastMessageSenderId", fUser.getUid());
        senderConvMap.put("seen", true);
        senderConvRef.updateChildren(senderConvMap);

        DatabaseReference receiverConvRef = databaseReference.child("user-chats").child(receiverId).child(fUser.getUid());
        Map<String, Object> receiverConvMap = new HashMap<>();
        receiverConvMap.put("id", fUser.getUid());
        receiverConvMap.put("username", senderUsername); // ADDED
        receiverConvMap.put("lastMessage", type.equals("image") ? "[Image]" : message);
        receiverConvMap.put("timestamp", ServerValue.TIMESTAMP);
        receiverConvMap.put("lastMessageSenderId", fUser.getUid());
        receiverConvMap.put("seen", false);
        receiverConvRef.updateChildren(receiverConvMap);
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        if (imageUri != null) {
            if (senderUsername == null || receiverUsername == null) {
                Toast.makeText(this, "User info not loaded yet, please wait.", Toast.LENGTH_SHORT).show();
                return;
            }
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                String mUri = downloadUri.toString();
                sendMessage(mUri, "image");
            }).addOnFailureListener(e -> {
                Toast.makeText(ChatActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
            })).addOnFailureListener(e -> {
                Toast.makeText(ChatActivity.this, "Failed to upload image!", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(this, "Upload in progress...", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }

    private void readMessages() {
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

    // MODIFIED: Load info for both sender and receiver
    private void loadUserInfos() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get receiver info
        db.collection("users").document(receiverId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User receiverUser = documentSnapshot.toObject(User.class);
                if (receiverUser != null) {
                    this.receiverUsername = receiverUser.getUsername();
                    this.receiverImageUrl = receiverUser.getProfileUrl();
                    binding.username.setText(this.receiverUsername);

                    if (this.receiverImageUrl != null && !this.receiverImageUrl.equals("default")) {
                        Glide.with(getApplicationContext()).load(this.receiverImageUrl).into(binding.profileImage);
                    } else {
                        binding.profileImage.setImageResource(R.drawable.profile_image);
                    }

                    // Now get sender info
                    db.collection("users").document(fUser.getUid()).get().addOnSuccessListener(senderSnapshot -> {
                        if (senderSnapshot.exists()) {
                            User senderUser = senderSnapshot.toObject(User.class);
                            if (senderUser != null) {
                                this.senderUsername = senderUser.getUsername();

                                // Both users are loaded, now we can read messages
                                readMessages();
                            }
                        }
                    });
                }
            }
        });
    }
}
