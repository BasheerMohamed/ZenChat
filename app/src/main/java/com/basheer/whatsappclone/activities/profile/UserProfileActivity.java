package com.basheer.whatsappclone.activities.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.basheer.whatsappclone.R;
import com.basheer.whatsappclone.databinding.ActivityUserProfileBinding;
import com.bumptech.glide.Glide;

public class UserProfileActivity extends AppCompatActivity {

    ActivityUserProfileBinding binding;
    String receiverID;
    String userProfile;
    String userName;
    String mobileNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        receiverID = intent.getStringExtra("userID");
        userProfile = intent.getStringExtra("imageProfile");
        mobileNumber = intent.getStringExtra("mobileNumber");

        if (receiverID != null) {
            binding.titleName.setText(userName);
            binding.userName.setText(userName);
            binding.mobileNumber.setText(mobileNumber);
            if (userProfile == null || userProfile.isEmpty()) {
                binding.userProfileImage.setImageResource(R.drawable.avatar);
            } else {
                Glide.with(this).load(userProfile).into(binding.userProfileImage);
            }
        }

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}