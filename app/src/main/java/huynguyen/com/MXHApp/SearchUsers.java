package huynguyen.com.MXHApp;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

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

    private String generateSearchableString(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String withoutAccents = pattern.matcher(normalized).replaceAll("");
        return withoutAccents.toLowerCase(Locale.ROOT);
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUsers(generateSearchableString(query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUsers(generateSearchableString(newText));
                return false;
            }
        });
    }

    private void searchUsers(String searchText) {
        Query query;
        if (searchText.isEmpty()) {
            // For initial list, just get some users. We will filter admins on client.
            query = firestore.collection("users").limit(20);
        } else {
            // For search, query on the searchable field. We will filter admins on client.
            query = firestore.collection("users")
                    .whereGreaterThanOrEqualTo("searchableName", searchText)
                    .whereLessThanOrEqualTo("searchableName", searchText + "\uf8ff")
                    .limit(20);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (isDestroyed()) return;
            userList.clear();
            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                User user = snapshot.toObject(User.class);
                
                // **FIXED**: Client-side filtering for role and account status.
                boolean isNotAdmin = user.getRole() == null || !user.getRole().equals("admin");
                boolean isActive = user.getAccountStatus() != null && user.getAccountStatus().equals("active");

                if (isNotAdmin && isActive) {
                    userList.add(user);
                }
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error searching users (Ask Gemini)", e);
        });
    }
}
