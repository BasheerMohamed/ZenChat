package com.basheer.whatsappclone.activities.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.basheer.whatsappclone.R;
import com.basheer.whatsappclone.activities.profile.ProfileActivity;
import com.basheer.whatsappclone.databinding.ActivitySettingsBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    FirebaseUser firebaseUser;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if(firebaseUser!=null) {
            getInfo();
        }

        initClickAction();

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void initClickAction() {

        binding.linearlayoutForProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
            }
        });

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void getInfo() {

        firestore.collection("Users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String userName = documentSnapshot.getString("userName");
                String profileImage = documentSnapshot.getString("imageProfile");
                String bio = documentSnapshot.getString("bio");

                binding.userName.setText(userName);
                assert bio != null;
                if (bio.isEmpty()) {
                    binding.userAbout.setVisibility(View.GONE);
                } else {
                    binding.userAbout.setText(bio);
                }
                Glide.with(SettingsActivity.this).load(profileImage).placeholder(R.drawable.avatar).into(binding.userProfileImage);

            }
        });

    }
}