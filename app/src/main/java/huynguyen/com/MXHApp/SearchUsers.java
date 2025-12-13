package huynguyen.com.MXHApp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import huynguyen.com.MXHApp.Adapter.SearchAdapter;
import huynguyen.com.MXHApp.Model.User;

public class SearchUsers extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private List<User> mUsers;

    EditText search;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);

        // TODO: Initialize Views

        // TODO: Set up RecyclerView

        // TODO: Add TextWatcher to the search EditText

        // TODO: Load initial users
    }

    private void searchUsers(String s) {
        // TODO: Implement Firestore query to search for users based on input string
    }

    private void readUsers() {
        // TODO: Implement Firestore query to load all users initially
    }
}
