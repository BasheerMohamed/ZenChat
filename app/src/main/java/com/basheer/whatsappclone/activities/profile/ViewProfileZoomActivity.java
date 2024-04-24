package com.basheer.whatsappclone.activities.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.basheer.whatsappclone.R;
import com.basheer.whatsappclone.common.Common;
import com.basheer.whatsappclone.databinding.ActivityViewProfileZoomBinding;
import com.bumptech.glide.Glide;

public class ViewProfileZoomActivity extends AppCompatActivity {

    ActivityViewProfileZoomBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewProfileZoomBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.zoomView.setImageBitmap(Common.IMAGE_BITMAP);

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//
//        Intent intent = getIntent();
//        String imgurl = intent.getStringExtra("imageurl");
//
//        Glide.with(this).load(imgurl).into(binding.zoomView);

    }
}