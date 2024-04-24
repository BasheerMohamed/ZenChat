package com.basheer.whatsappclone.activities.startup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.basheer.whatsappclone.R;
import com.basheer.whatsappclone.activities.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Handler handler = new Handler();

        // Determine which activity to start after the delay
        Class<?> destinationClass = firebaseUser != null ? MainActivity.class : WelcomeScreenActivity.class;
        Intent intent = new Intent(SplashScreenActivity.this, destinationClass);

        handler.postDelayed(() -> {
            startActivity(intent);
            finish();
        }, 2000);  // Delay of 2 seconds

    }

}