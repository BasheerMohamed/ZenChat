package com.basheer.whatsappclone.activities.profile;

import static com.google.common.io.Files.getFileExtension;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.basheer.whatsappclone.R;
import com.basheer.whatsappclone.databinding.ActivitySetupProfileBinding;
import com.basheer.whatsappclone.activities.main.MainActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class SetupProfileActivity extends AppCompatActivity {

    ActivitySetupProfileBinding binding;
    ProgressDialog progressDialog;
    String TAG = "SetupProfileActivity";
    private static final int CAMERA_REQUEST_CODE = 100;
    FirebaseUser firebaseUser;
    FirebaseFirestore firestore;
    BottomSheetDialog bottomSheetDialog;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        firestore.collection("Users").document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        binding.name.setText(task.getResult().getString("userName"));
                        Glide.with(SetupProfileActivity.this).load(task.getResult().getString("imageProfile")).placeholder(R.drawable.avatar).into(binding.profileImage);

                    } else {

                        Log.w(TAG, "Error getting documents.", task.getException());
                    }

                });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Profile..");
        progressDialog.setCancelable(false);
        initButtonClick();

    }

    private void initButtonClick() {

        binding.setupProfile.setOnClickListener(v -> {

            if(TextUtils.isEmpty(binding.name.getText().toString())) {

                Toast.makeText(SetupProfileActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();

            } else {

                uploadToFirebaseStorage();

            }

        });

        binding.floating.setOnClickListener(v -> showBottomSheetPickPhoto());
    }

    private void showBottomSheetPickPhoto() {

        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);

        view.findViewById(R.id.gallery).setOnClickListener(v -> {

            openGalery();
            bottomSheetDialog.dismiss();

        });

        view.findViewById(R.id.camera).setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(SetupProfileActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(SetupProfileActivity.this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);

            } else {

                openCamera();
                bottomSheetDialog.dismiss();

            }

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
                Glide.with(SetupProfileActivity.this).load(imageUri).placeholder(R.drawable.avatar).into(binding.profileImage);

            }
        }

        if (requestCode == 440 && resultCode == RESULT_OK) {

            Glide.with(SetupProfileActivity.this).load(imageUri).placeholder(R.drawable.avatar).into(binding.profileImage);

        }

    }

    private void uploadToFirebaseStorage() {

        if(imageUri != null) {

            progressDialog.show();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ProfileImages/" + System.currentTimeMillis() + "." + getFileExtension(String.valueOf(imageUri)));
            storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {

                final String downloadUrl = uri.toString();

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("imageProfile", downloadUrl);
                hashMap.put("userName", binding.name.getText().toString());

                firestore.collection("Users").document(firebaseUser.getUid()).update(hashMap).addOnSuccessListener(aVoid -> {

                    progressDialog.dismiss();
                    Toast.makeText(SetupProfileActivity.this, "Upload successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SetupProfileActivity.this, MainActivity.class));
                    finish();

                }).addOnFailureListener(e -> {

                    progressDialog.dismiss();
                    Toast.makeText(SetupProfileActivity.this, "Profile update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("FirestoreUpdateError", "Error updating document", e);

                });

            }).addOnFailureListener(e -> {

                progressDialog.dismiss();
                Toast.makeText(SetupProfileActivity.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_LONG).show();

            })).addOnFailureListener(e -> {

                progressDialog.dismiss();
                Toast.makeText(SetupProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();

            });

        } else {

            updateProfile();

        }

    }

    private void updateProfile() {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userName", binding.name.getText().toString());

        firestore.collection("Users").document(firebaseUser.getUid()).update(hashMap).addOnSuccessListener(aVoid -> {
            Toast.makeText(SetupProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        }).addOnCompleteListener(task -> progressDialog.dismiss()).addOnFailureListener(e -> {

            progressDialog.dismiss();
            Toast.makeText(SetupProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();

        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                openCamera();

            } else {

                Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_LONG).show();

            }

        }

    }

}