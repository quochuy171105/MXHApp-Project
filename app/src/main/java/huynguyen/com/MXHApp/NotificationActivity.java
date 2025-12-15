package huynguyen.com.MXHApp;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import huynguyen.com.MXHApp.Adapter.NotificationAdapter;
import huynguyen.com.MXHApp.Model.Notifications;
import huynguyen.com.MXHApp.databinding.ActivityNotificationBinding;

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity";

    private ActivityNotificationBinding binding;
    private NotificationAdapter adapter;
    private List<Notifications> list;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private ListenerRegistration notificationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        binding.backButton.setOnClickListener(v -> finish());

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        adapter = new NotificationAdapter(this, list);
        binding.recyclerView.setAdapter(adapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        readNotifications();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (notificationListener != null) {
            notificationListener.remove();
        }
    }

    private void readNotifications() {
        if (firebaseUser == null) return;
        if (notificationListener != null) notificationListener.remove();

        notificationListener = firestore.collection("users").document(firebaseUser.getUid()).collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((snapshots, error) -> {
                    if (isDestroyed()) return;
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }

                    if (snapshots != null) {
                        list.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Notifications notification = doc.toObject(Notifications.class);
                            list.add(notification);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
