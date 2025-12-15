package huynguyen.com.MXHApp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import huynguyen.com.MXHApp.Adapter.AdminUserAdapter;
import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.databinding.ActivityAdminBinding;

public class AdminActivity extends AppCompatActivity {

    private static final String TAG = "AdminActivity";

    private ActivityAdminBinding binding;
    private AdminUserAdapter adapter;
    private List<User> userList;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setSupportActionBar(binding.toolbarAdmin);

        binding.recyclerViewUsers.setHasFixedSize(true);
        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        adapter = new AdminUserAdapter(this, userList);
        binding.recyclerViewUsers.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        firestore.collection("users")
                .whereNotEqualTo("role", "admin") // <-- Added this line to filter out admins
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }

                    if (snapshots != null) {
                        userList.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            User user = doc.toObject(User.class);
                            userList.add(user);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.admin_logout) {
            auth.signOut();
            Intent intent = new Intent(AdminActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
