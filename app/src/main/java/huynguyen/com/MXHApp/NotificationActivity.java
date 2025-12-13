package huynguyen.com.MXHApp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import huynguyen.com.MXHApp.Adapter.NotificationAdapter;
import huynguyen.com.MXHApp.Model.Notifications;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    NotificationAdapter adapter;
    List<Notifications> notificationsList;

    ImageView back;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // TODO: Initialize Views

        // TODO: Set up Toolbar and back button

        // TODO: Set up RecyclerView

        // TODO: Load notifications from Firestore
    }

    private void readNotifications() {
        // TODO: Implement Firestore query to load notifications for the current user
    }
}
