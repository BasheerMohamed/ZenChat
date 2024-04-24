package com.basheer.whatsappclone.activities.chat;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.basheer.whatsappclone.R;
import com.basheer.whatsappclone.databinding.ActivityViewChatImageZoomBinding;
import com.bumptech.glide.Glide;

public class ViewChatImageZoomActivity extends AppCompatActivity {

    ActivityViewChatImageZoomBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewChatImageZoomBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.backButton.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("imageUrl");

        Glide.with(this).load(imageUrl).into(binding.zoomView);

    }
}