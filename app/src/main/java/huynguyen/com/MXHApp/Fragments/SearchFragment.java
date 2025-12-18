package huynguyen.com.MXHApp.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import huynguyen.com.MXHApp.Adapter.SearchAdapter;
import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.R;
import huynguyen.com.MXHApp.databinding.FragmentSearchUsersBinding;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private FragmentSearchUsersBinding binding;
    private RecyclerView recyclerView;
    private SearchAdapter searchAdapter;
    private List<User> mUsers;

    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchUsersBinding.inflate(inflater, container, false);

        firestore = FirebaseFirestore.getInstance();

        recyclerView = binding.recyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUsers = new ArrayList<>();
        searchAdapter = new SearchAdapter(getContext(), mUsers);
        recyclerView.setAdapter(searchAdapter);

        // Initially read all users (or maybe top users, or nothing)
        readUsers();

        binding.searchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return binding.getRoot();
    }

    private void searchUsers(String s) {
        if (s.isEmpty()) {
            readUsers();
            return;
        }

        // Note: Firestore does not support native full-text search.
        // This is a simple prefix match. For better search, use Algolia or similar.
        firestore.collection("users")
                .orderBy("username")
                .startAt(s)
                .endAt(s + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        mUsers.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            mUsers.add(user);
                        }
                        searchAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "Error searching users", task.getException());
                    }
                });
    }

    private void readUsers() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // Limit to 50 users for performance if not searching
        firestore.collection("users").limit(50).get().addOnCompleteListener(task -> {
            if (binding == null) return;
            if (binding.searchUsers.getText().toString().equals("")) {
                mUsers.clear();
                if (task.isSuccessful() && task.getResult() != null) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
                        // Don't show current user in list
                        if (firebaseUser != null && user.getUser_id().equals(firebaseUser.getUid())) {
                            continue;
                        }
                        mUsers.add(user);
                    }
                    searchAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}