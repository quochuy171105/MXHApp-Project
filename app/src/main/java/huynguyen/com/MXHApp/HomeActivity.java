package huynguyen.com.MXHApp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.badge.BadgeDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Set;

import huynguyen.com.MXHApp.Fragments.FeedFragment;
import huynguyen.com.MXHApp.Model.Conversation;
import huynguyen.com.MXHApp.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private NavController navController;

    private ValueEventListener unreadChatsListener;
    private DatabaseReference userChatsRef;

    private final ActivityResultLauncher<Intent> postActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    binding.bottomNav.setSelectedItemId(R.id.feed);
                    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                    if (navHostFragment != null) {
                        Fragment currentFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
                        if (currentFragment instanceof FeedFragment) {
                            ((FeedFragment) currentFragment).refreshPosts();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        setupUnreadChatBadges(); // ADDED: Setup for chat badge
    }

    private void setupNavigation() {
        Set<Integer> topLevelDestinations = new HashSet<>();
        topLevelDestinations.add(R.id.feed);
        topLevelDestinations.add(R.id.following);
        topLevelDestinations.add(R.id.chat);
        topLevelDestinations.add(R.id.search);
        topLevelDestinations.add(R.id.profile);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        binding.bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.post) {
                Intent intent = new Intent(HomeActivity.this, PostActivity.class);
                postActivityLauncher.launch(intent);
                return false;
            }
            if (navController.getCurrentDestination() != null && item.getItemId() == navController.getCurrentDestination().getId()) {
                return false;
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        binding.bottomNav.setOnItemReselectedListener(item -> {
            if (item.getItemId() == R.id.profile) {
                SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                editor.apply();
                navController.popBackStack(R.id.profile, true);
                navController.navigate(R.id.profile);
            }
        });
    }

    private void setupUnreadChatBadges() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser == null) return;

        userChatsRef = FirebaseDatabase.getInstance().getReference("user-chats").child(fUser.getUid());
        BadgeDrawable chatBadge = binding.bottomNav.getOrCreateBadge(R.id.chat);

        unreadChatsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unreadCount = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Conversation conversation = snapshot.getValue(Conversation.class);
                    if (conversation != null && !conversation.isSeen() &&
                            conversation.getLastMessageSenderId() != null && !conversation.getLastMessageSenderId().equals(fUser.getUid())) {
                        unreadCount++;
                    }
                }
                chatBadge.setVisible(unreadCount > 0);
                chatBadge.setNumber(unreadCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        userChatsRef.addValueEventListener(unreadChatsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener to prevent memory leaks
        if (userChatsRef != null && unreadChatsListener != null) {
            userChatsRef.removeEventListener(unreadChatsListener);
        }
    }

    public ActivityHomeBinding getBinding() {
        return binding;
    }
}
