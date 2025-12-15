package huynguyen.com.MXHApp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import huynguyen.com.MXHApp.Adapter.ShowAdapter;
import huynguyen.com.MXHApp.Model.User;

public class ShowList extends AppCompatActivity {

    private static final String TAG = "ShowListActivity";

    String id;
    String title;
    TextView title_tv;

    Toolbar toolbar;
    ImageView backButton;

    RecyclerView recyclerView;
    ShowAdapter adapter;
    List<User> userList; // Changed to List<User>

    FirebaseFirestore firestore;
    private ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_list);

        title_tv = findViewById(R.id.title);
        backButton = findViewById(R.id.back_button);
        toolbar = findViewById(R.id.toolbar);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        backButton.setOnClickListener(v -> finish());
        title_tv.setText(title);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        userList = new ArrayList<>();
        adapter = new ShowAdapter(this, userList); // Use the new constructor
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        getList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (listener != null) {
            listener.remove();
        }
    }

    private void getList() {
        if (id == null || title == null) return;

        String collectionPath = title.equalsIgnoreCase("Followers") ? "followers" : "following";
        CollectionReference listRef = firestore.collection("users").document(id).collection(collectionPath);

        listener = listRef.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                return;
            }
            List<String> ids = new ArrayList<>();
            if(snapshots != null){
                for (QueryDocumentSnapshot doc : snapshots) {
                    ids.add(doc.getId());
                }
            }
            if (!ids.isEmpty()) {
                showUsers(ids);
            } else {
                userList.clear();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showUsers(List<String> userIds) {
        // Firestore allows a maximum of 10 items in a single 'in' query.
        // If the list can be larger, this needs pagination or multiple queries.
        if(userIds.size() > 10){
            Log.w(TAG, "User list size is greater than 10, this query might fail or be incomplete.");
            // Handle this case, for example by querying 10 by 10.
        }

        firestore.collection("users").whereIn("user_id", userIds).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                        User user = doc.toObject(User.class);
                        userList.add(user);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user details", e);
                });
    }
}
