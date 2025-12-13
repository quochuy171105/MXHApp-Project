package huynguyen.com.MXHApp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import huynguyen.com.MXHApp.Fragments.FeedFragment;
import huynguyen.com.MXHApp.Fragments.FollowingFragment;
import huynguyen.com.MXHApp.Fragments.PostPlaceholderFragment;
import huynguyen.com.MXHApp.Fragments.UserFragment;
import huynguyen.com.MXHApp.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private Fragment selectedFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TODO: Set up BottomNavigationView listener

        // TODO: Set the initial fragment
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            item -> {
                // TODO: Implement fragment switching logic based on item ID
                // Remember to handle the post button placeholder

                return true;
            };
}
