package huynguyen.com.MXHApp;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import huynguyen.com.MXHApp.Adapter.SearchAdapter;
import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.databinding.ActivitySearchUsersBinding;

public class SearchUsers extends AppCompatActivity {

    private static final String TAG = "SearchUsers";

    private ActivitySearchUsersBinding binding;
    private SearchAdapter adapter;
    private List<User> userList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();

        // Setup Toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Setup RecyclerView
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        adapter = new SearchAdapter(this, userList);
        binding.recyclerView.setAdapter(adapter);

        setupSearchView();

        // Initial load
        searchUsers("");
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUsers(query.toLowerCase(Locale.ROOT));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUsers(newText.toLowerCase(Locale.ROOT));
                return false;
            }
        });
    }

    private void searchUsers(String searchText) {
        Query query;
        if (searchText.isEmpty()) {
            query = firestore.collection("users").limit(20);
        } else {
            query = firestore.collection("users")
                    .orderBy("username")
                    .startAt(searchText)
                    .endAt(searchText + "\uf8ff");
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if(isDestroyed()) return; // Avoid updating a destroyed activity
            userList.clear();
            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                User user = snapshot.toObject(User.class);
                userList.add(user);
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error searching users", e);
        });
    }
}
