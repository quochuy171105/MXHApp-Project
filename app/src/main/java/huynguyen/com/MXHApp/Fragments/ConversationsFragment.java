package huynguyen.com.MXHApp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import huynguyen.com.MXHApp.Adapter.ConversationsAdapter;
import huynguyen.com.MXHApp.Model.Conversation;
import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.databinding.FragmentConversationsBinding;

public class ConversationsFragment extends Fragment {

    private FragmentConversationsBinding binding;
    private ConversationsAdapter conversationsAdapter;
    private List<Conversation> mConversations;
    private FirebaseUser fUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConversationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        setupRecyclerView();
        readConversations();
    }

    private void setupRecyclerView() {
        binding.recyclerViewConversations.setHasFixedSize(true);
        binding.recyclerViewConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        mConversations = new ArrayList<>();
        conversationsAdapter = new ConversationsAdapter(getContext(), mConversations);
        binding.recyclerViewConversations.setAdapter(conversationsAdapter);
    }

    private void readConversations() {
        DatabaseReference userChatsRef = FirebaseDatabase.getInstance().getReference("user-chats").child(fUser.getUid());
        userChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                    return;
                }

                Map<String, Conversation> conversationMap = new HashMap<>();
                List<String> userIds = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Conversation conv = snapshot.getValue(Conversation.class);
                    if (conv != null) {
                        conversationMap.put(conv.getId(), conv);
                        userIds.add(conv.getId());
                    }
                }

                if (userIds.isEmpty()) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                    return;
                }

                // Get user details from Firestore
                FirebaseFirestore.getInstance().collection("users").whereIn("user_id", userIds)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                mConversations.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    User user = document.toObject(User.class);
                                    Conversation conv = conversationMap.get(user.getUser_id());
                                    if (conv != null) {
                                        conv.setUsername(user.getUsername());
                                        conv.setProfileUrl(user.getProfileUrl());
                                        mConversations.add(conv);
                                    }
                                }

                                // Sort conversations by timestamp (newest first)
                                Collections.sort(mConversations, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));

                                conversationsAdapter.notifyDataSetChanged();
                                binding.emptyView.setVisibility(mConversations.isEmpty() ? View.VISIBLE : View.GONE);
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                binding.emptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
