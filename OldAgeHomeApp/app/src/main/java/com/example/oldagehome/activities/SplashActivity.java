package com.example.oldagehome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.oldagehome.R;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN = 3000;

    Animation topAnim, bottomAnim;
    ImageView image;
    TextView logo, slogan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make full screen
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        // Animations
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        // Hooks
        image = findViewById(R.id.logo_img);
        logo = findViewById(R.id.logo_text);
        slogan = findViewById(R.id.slogan_text);

        // Set animation
        image.setAnimation(topAnim);
        logo.setAnimation(bottomAnim);
        slogan.setAnimation(bottomAnim);

        // Delay and go to Login
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            // Pair[] pairs = new Pair[2];
            // pairs[0] = new Pair<View, String>(image, "logo_image");
            // pairs[1] = new Pair<View, String>(logo, "logo_text");
            // ActivityOptions options =
            // ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this, pairs);
            // startActivity(intent, options.toBundle());
            startActivity(intent);
            finish();
        }, SPLASH_SCREEN);
    }
}
