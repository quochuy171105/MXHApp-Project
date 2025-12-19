package huynguyen.com.MXHApp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class FullImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        ImageView fullImageView = findViewById(R.id.full_image_view);
        ImageButton backButton = findViewById(R.id.back_button);

        String imageUrl = getIntent().getStringExtra("imageUrl");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(fullImageView);
        }

        backButton.setOnClickListener(v -> finish());
        fullImageView.setOnClickListener(v -> finish()); // Also finish when clicking the image
    }
}
