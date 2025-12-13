package huynguyen.com.MXHApp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import huynguyen.com.MXHApp.Adapter.PostAdapter;
import huynguyen.com.MXHApp.Model.PostItem;
import huynguyen.com.MXHApp.Model.Posts;
import huynguyen.com.MXHApp.Model.User;

public class PostDetails extends AppCompatActivity {

    RecyclerView recyclerView;
    PostAdapter postAdapter;
    List<PostItem> postList;

    String postid;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        // TODO: Get postid from SharedPreferences

        // TODO: Initialize views

        // TODO: Set up RecyclerView

        // TODO: Load the specific post details
    }

    private void readPost() {
        // TODO: Implement Firestore query to load the single post based on postid
    }
}
