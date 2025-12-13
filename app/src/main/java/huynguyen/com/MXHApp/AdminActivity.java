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

        // TODO: Initialize Firebase instances and set up Toolbar

        // TODO: Set up RecyclerView

        // TODO: Load users from Firestore
    }

    private void loadUsers() {
        // TODO: Implement Firestore snapshot listener to load users
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO: Inflate the admin menu
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // TODO: Handle menu item clicks, e.g., logout
        return super.onOptionsItemSelected(item);
    }
}
