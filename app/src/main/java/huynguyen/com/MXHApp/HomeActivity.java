package huynguyen.com.MXHApp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.HashSet;
import java.util.Set;

import huynguyen.com.MXHApp.Fragments.FeedFragment;
import huynguyen.com.MXHApp.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private NavController navController;

    private final ActivityResultLauncher<Intent> postActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Post was created successfully, navigate to feed and refresh
                    binding.bottomNav.setSelectedItemId(R.id.feed);

                    // Get the current fragment and check if it's the FeedFragment
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

        Set<Integer> topLevelDestinations = new HashSet<>();
        topLevelDestinations.add(R.id.feed);
        topLevelDestinations.add(R.id.following);
        topLevelDestinations.add(R.id.search);
        topLevelDestinations.add(R.id.profile);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        binding.bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.post) {
                // Launch PostActivity for result
                Intent intent = new Intent(HomeActivity.this, PostActivity.class);
                postActivityLauncher.launch(intent);
                return false; // Do not navigate, just launch the activity
            }
            // Let NavigationUI handle other selections
            if (navController.getCurrentDestination() != null && item.getItemId() == navController.getCurrentDestination().getId()) {
                return false; // Don't reselect the same item
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
    }

    public ActivityHomeBinding getBinding() {
        return binding;
    }
}
