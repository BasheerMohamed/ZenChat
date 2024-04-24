package com.basheer.whatsappclone.activities.profile;

import static com.google.common.io.Files.getFileExtension;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.FileProvider;
import com.basheer.whatsappclone.R;
import com.basheer.whatsappclone.common.Common;
import com.basheer.whatsappclone.databinding.ActivityProfileBinding;
import com.basheer.whatsappclone.activities.startup.SplashScreenActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;
    FirebaseUser firebaseUser;
    FirebaseFirestore firestore;
    BottomSheetDialog bottomSheetDialog, bottomSheetDialogEditName, bottomSheetDialogEditAbout;
    Uri imageUri;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating..");
        dialog.setCancelable(false);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if(firebaseUser!=null) {
            getInfo();
        }

        initActionClick();

        binding.backButton.setOnClickListener(v -> finish());

    }

    public void initActionClick() {
        binding.floating.setOnClickListener(v -> showBottomSheetPickPhoto());

        binding.linearLayoutforname.setOnClickListener(v -> showBottomSheetEditName());

        binding.linearLayoutforabout.setOnClickListener(v -> showBottomSheetEditAbout());

        binding.userProfileImage.setOnClickListener(v -> {
            binding.userProfileImage.invalidate();
            Drawable dr = binding.userProfileImage.getDrawable();
            Common.IMAGE_BITMAP = ((BitmapDrawable)dr.getCurrent()).getBitmap();
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(ProfileActivity.this, binding.userProfileImage, "image");
            Intent intent = new Intent(ProfileActivity.this, ViewProfileZoomActivity.class);
            startActivity(intent, activityOptionsCompat.toBundle());
        });

        binding.signOut.setOnClickListener(v -> showDialogSignOut());

        binding.backButton.setOnClickListener(v -> finish());

    }

    private void showDialogSignOut() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setMessage("Do you want to sign out ?");
        builder.setPositiveButton("Sign out", (dialog, which) -> {
            dialog.cancel();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProfileActivity.this, SplashScreenActivity.class));
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void showBottomSheetEditName() {

        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_name, null);

        ((View) view.findViewById(R.id.cancel)).setOnClickListener(v -> bottomSheetDialogEditName.dismiss());

        EditText editText = view.findViewById(R.id.editText);

        ((View) view.findViewById(R.id.save)).setOnClickListener(v -> {
            if(TextUtils.isEmpty(editText.getText().toString())) {
                Toast.makeText(ProfileActivity.this, "Name can't be empty", Toast.LENGTH_SHORT).show();
            } else {
                updateName(editText.getText().toString());
                bottomSheetDialogEditName.dismiss();
            }
        });

        bottomSheetDialogEditName = new BottomSheetDialog(this);
        bottomSheetDialogEditName.setContentView(view);

        Objects.requireNonNull(bottomSheetDialogEditName.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        bottomSheetDialogEditName.setOnDismissListener(dialog -> bottomSheetDialogEditName = null);

        bottomSheetDialogEditName.show();

    }
    private void showBottomSheetEditAbout() {

        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.bottom_sheet_edit_about, null);

        ((View) view.findViewById(R.id.cancel)).setOnClickListener(v -> bottomSheetDialogEditAbout.dismiss());

        EditText editText = view.findViewById(R.id.editText);

        ((View) view.findViewById(R.id.save)).setOnClickListener(v -> {
            if(TextUtils.isEmpty(editText.getText().toString())) {
                Toast.makeText(ProfileActivity.this, "About can't be empty", Toast.LENGTH_SHORT).show();
            } else {
                updateAbout(editText.getText().toString());
                bottomSheetDialogEditAbout.dismiss();
            }
        });

        bottomSheetDialogEditAbout = new BottomSheetDialog(this);
        bottomSheetDialogEditAbout.setContentView(view);

        Objects.requireNonNull(bottomSheetDialogEditAbout.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        bottomSheetDialogEditAbout.setOnDismissListener(dialog -> bottomSheetDialogEditAbout = null);

        bottomSheetDialogEditAbout.show();

    }

    private void updateName(String newUserName) {

        firestore.collection("Users").document(firebaseUser.getUid()).update("userName", newUserName).addOnSuccessListener(unused -> getInfo());

    }

    private void updateAbout(String newUserName) {

        firestore.collection("Users").document(firebaseUser.getUid()).update("bio", newUserName).addOnSuccessListener(unused -> getInfo());

    }

    private void showBottomSheetPickPhoto() {

        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);

        view.findViewById(R.id.gallery).setOnClickListener(v -> {
            openGalery();
            bottomSheetDialog.dismiss();
        });

        view.findViewById(R.id.camera).setOnClickListener(v -> {
            openCamera();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(view);

        Objects.requireNonNull(bottomSheetDialog.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        bottomSheetDialog.setOnDismissListener(dialog -> bottomSheetDialog = null);

        bottomSheetDialog.show();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void openCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";
        try {
            File file = File.createTempFile("IMG_" + timeStamp, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            imageUri = FileProvider.getUriForFile(this, "com.basheer.whatsappclone.provider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.putExtra("listPhotoName", imageFileName);
            startActivityForResult(intent, 440);
        } catch (Exception ignored) {

        }
    }

    private void openGalery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 45);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {

            if (data.getData() != null) {

                imageUri = data.getData();
                uploadToFirebaseStorage();

            }
        }

        if (requestCode == 440 && resultCode == RESULT_OK) {
            uploadToFirebaseStorage();
        }

    }

    private void uploadToFirebaseStorage() {

        if(imageUri!=null) {
            dialog.show();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ProfileImages/" + System.currentTimeMillis() + "." + getFileExtension(String.valueOf(imageUri)));
            storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful());
                Uri downloadUrl = urlTask.getResult();

                final String sdownload_url = String.valueOf(downloadUrl);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("imageProfile", sdownload_url);
                dialog.dismiss();

                firestore.collection("Users").document(firebaseUser.getUid()).update(hashMap).addOnSuccessListener(unused -> getInfo());

            });

        }

    }

    public void getInfo() {

        firestore.collection("Users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String userName = documentSnapshot.getString("userName");
                String userPhone = documentSnapshot.getString("userPhone");
                String profileImage = documentSnapshot.getString("imageProfile");
                String bio = documentSnapshot.getString("bio");

                binding.userName.setText(userName);
                binding.userPhone.setText(userPhone);
                assert bio != null;
                if (bio.isEmpty()) {

                    binding.userAbout.setText("No About");

                } else {

                    binding.userAbout.setText(bio);

                }

                Glide.with(ProfileActivity.this).load(profileImage).placeholder(R.drawable.avatar).into(binding.userProfileImage);

            }

        });

    }

}