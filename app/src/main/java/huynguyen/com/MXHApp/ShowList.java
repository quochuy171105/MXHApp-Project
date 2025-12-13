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

    // TODO: Declare UI elements and other variables
    private RecyclerView recyclerView;
    private ShowAdapter adapter;
    private List<User> userList;
    private String id;
    private String title;
    private ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_list);

        // TODO: Get id and title from intent

        // TODO: Initialize views and set up Toolbar

        // TODO: Set up RecyclerView

        // TODO: Call method to load the list of users
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TODO: Remove Firestore listener to avoid memory leaks
    }

    private void getList() {
        // TODO: Determine whether to fetch "followers" or "following"
        // TODO: Add a Firestore snapshot listener to get the list of user IDs
    }

    private void showUsers(List<String> userIds) {
        // TODO: Query the "users" collection to get User objects from the list of IDs
    }
}
