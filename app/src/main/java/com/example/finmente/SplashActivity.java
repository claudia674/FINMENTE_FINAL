package com.example.finmente;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finmente.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View content = findViewById(R.id.layout_content_splash);
        View progress = findViewById(R.id.progress_splash);

        content.setAlpha(0f);
        progress.setAlpha(0f);

        content.animate()
                .alpha(1f)
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(1200)
                .withEndAction(() -> {
                    content.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(800)
                            .start();
                })
                .start();

        progress.animate()
                .alpha(1f)
                .setStartDelay(1000)
                .setDuration(1000)
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 3500);
    }
}
