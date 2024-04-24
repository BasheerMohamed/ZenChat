package com.basheer.whatsappclone.activities.chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.basheer.whatsappclone.R;
import com.basheer.whatsappclone.activities.profile.UserProfileActivity;
import com.basheer.whatsappclone.adapter.ChatsAdapter;
import com.basheer.whatsappclone.databinding.ActivityChatBinding;
import com.basheer.whatsappclone.manage.ChatService;
import com.basheer.whatsappclone.manage.FirebaseService;
import com.basheer.whatsappclone.model.Chats;
import com.basheer.whatsappclone.dialog.DialogReviewSendImage;
import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnRecordListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    ValueEventListener valueEventListener;
    private String receiverID;
    ChatsAdapter adapter;
    DatabaseReference reference;
    private DatabaseReference chatMessagesReference;
    List<Chats> list = new ArrayList<>();
    String userProfile, userName, mobileNumber;
    private boolean isActionShown = false;
    private ChatService chatService;
    private MediaRecorder mediaRecorder;
    String senderRoom, receiverRoom;
    Uri imageUri;
    String audio_path;
    String sTime;
    private static final int REQUEST_AUDIO_PERMISSION_CODE = 332;
    static int PERMISSION_CODE = 100;
    String senderUid;
    String receiverUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        initButtonClick();

        Intent intent = getIntent();
        senderUid = FirebaseAuth.getInstance().getUid();
        receiverUid = intent.getStringExtra("userID");

        senderUid = FirebaseAuth.getInstance().getUid();
        senderRoom = senderUid + receiverID;
        receiverRoom = receiverUid + senderUid;

        adapter = new ChatsAdapter(list, this, senderRoom, receiverRoom);
        binding.messageContent.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.messageContent.setLayoutManager(layoutManager);

        chatMessagesReference = FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(senderRoom)
                .child("messages");

        valueEventListener = new ValueEventListener() {

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<Chats> updatedList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Chats chat = snapshot.getValue(Chats.class);
                    if (chat != null) {

                        chat.setMessageId(snapshot.getKey());
                        updatedList.add(chat);

                    }

                }

                runOnUiThread(() -> {

                    list.clear();
                    list.addAll(updatedList);
                    adapter.notifyDataSetChanged();
                    binding.messageContent.smoothScrollToPosition(adapter.getItemCount());

                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.e("ChatActivity", "Listener was cancelled");

            }

        };

        reference.child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {

                    String status = snapshot.getValue(String.class);
                    assert status != null;
                    if(!status.isEmpty()) {

                        if(status.equals("Offline")) {

                            binding.status.setVisibility(View.GONE);

                        } else {

                            binding.status.setText(status);
                            binding.status.setVisibility(View.VISIBLE);

                        }

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        });

        final Handler handler = new Handler();
        binding.messageBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                reference.child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTying, 1000);

            }

            final Runnable userStoppedTying = new Runnable() {
                @Override
                public void run() {

                    reference.child("presence").child(senderUid).setValue("Online");
                }

            };

        });

    }

    public void initialize() {

        reference = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        receiverID = intent.getStringExtra("userID");
        userProfile = intent.getStringExtra("userProfile");
        mobileNumber = intent.getStringExtra("mobileNumber");

        chatService = new ChatService(this, receiverID);

        if (receiverID != null) {
            binding.userName.setText(userName);
            assert userProfile != null;
            if (userProfile.isEmpty()) {
                binding.userProfile.setImageResource(R.drawable.avatar);
            } else {
                Glide.with(this).load(userProfile).into(binding.userProfile);
            }
        }

        binding.messageBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (TextUtils.isEmpty(binding.messageBox.getText().toString())) {

                    binding.recordButton.setVisibility(View.VISIBLE);

                } else {

                    binding.recordButton.setVisibility(View.INVISIBLE);
                    binding.sendButton.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

        binding.call.setOnClickListener(v -> {

            if(ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);

            }

            Intent intent1 = new Intent();
            intent1.setAction(Intent.ACTION_CALL);
            intent1.setData(Uri.parse("tel:"+mobileNumber));
            startActivity(intent1);

        });

        binding.recordButton.setRecordView(binding.recordView);

        binding.recordView.setLockEnabled(true);
        binding.recordView.setRecordLockImageView(findViewById(R.id.record_lock));
        binding.recordView.setCancelBounds(8);
        binding.recordView.setSmallMicColor(Color.parseColor("#FA1100"));
        binding.recordView.setSlideToCancelText("Slide to cancel");
        binding.recordView.setLessThanSecondAllowed(false);
        binding.recordView.setCounterTimeColor(Color.parseColor("#777373"));
        binding.recordView.setShimmerEffectEnabled(true);
        binding.recordView.setTimeLimit(30000);
        binding.recordView.setTrashIconColor(Color.parseColor("#000000"));
        binding.recordView.setRecordButtonGrowingAnimationEnabled(true);
        binding.recordButton.setScaleUpTo(1.5f);
        binding.recordLock.setDefaultCircleColor(Color.parseColor("#2C977D"));
        binding.recordLock.setCircleLockedColor(Color.parseColor("#FFFFFFFF"));
        binding.recordLock.setLockColor(Color.WHITE);
        binding.recordButton.setSendIconResource(R.drawable.send_icon);

        binding.recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {

                if (!checkPermissionFromDevice()) {

                    requestPermission();

                } else {

                    binding.cardlayout.setVisibility(View.INVISIBLE);
                    startRecord();
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {

                        vibrator.vibrate(100);

                    }

                }

            }

            @Override
            public void onCancel() {
                binding.cardlayout.setVisibility(View.VISIBLE);
                if (mediaRecorder != null) {

                    try {

                        mediaRecorder.reset();

                    } catch (Exception ignored) { }

                }

            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {

                binding.cardlayout.setVisibility(View.VISIBLE);
                try {

                    sTime = getHumanTimeText(recordTime);
                    stopRecord();

                } catch (Exception ignored) { }

            }

            @Override
            public void onLessThanSecond() {

                binding.cardlayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void onLock() {

                Log.d("RecordView", "onLock");

            }

        });

        binding.recordView.setOnBasketAnimationEndListener(() -> binding.cardlayout.setVisibility(View.VISIBLE));

    }

    private void stopRecord() {

        try {

            if (mediaRecorder != null) {

                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();

                mediaRecorder = null;

                 chatService.sendVoice(audio_path);

            }

        } catch (Exception ignored) { }

    }

    @SuppressLint("DefaultLocale")
    private String getHumanTimeText(Long milliseconds) {

        return String.format("%02d",
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));

    }

    private void initButtonClick() {

        binding.sendButton.setOnClickListener(v -> {

            if (!TextUtils.isEmpty(binding.messageBox.getText().toString())) {

                chatService.sendTextMsg(binding.messageBox.getText().toString());
                binding.messageBox.setText("");

            }

        });

        binding.backButton.setOnClickListener(v -> finish());

        binding.userProfile.setOnClickListener(v -> startActivity(new Intent(ChatActivity.this, UserProfileActivity.class)
                .putExtra("userID", receiverID)
                .putExtra("imageProfile", userProfile)
                .putExtra("mobileNumber", mobileNumber)
                .putExtra("userName", userName)));

        binding.attachment.setOnClickListener(v -> {

            if(isActionShown) {

                binding.layoutShow.setVisibility(View.GONE);
                isActionShown = false;

            } else {

                binding.layoutShow.setVisibility(View.VISIBLE);
                isActionShown = true;

            }

        });

        binding.gallery.setOnClickListener(v -> openGalery());

        binding.camera.setOnClickListener(v -> openCamera());

        binding.cameraIcon.setOnClickListener(v -> openCamera());

    }

    private boolean checkPermissionFromDevice() {

        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_DENIED || record_audio_result == PackageManager.PERMISSION_DENIED;

    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
        }, REQUEST_AUDIO_PERMISSION_CODE);

    }

    private void startRecord() {

        setUpMediaRecorder();

        try {

            mediaRecorder.prepare();
            mediaRecorder.start();

        } catch (IOException ignored) { }

    }

    private void setUpMediaRecorder() {

        File audioDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        assert audioDir != null;
        if (!audioDir.exists() && !audioDir.mkdirs()) {

            Log.e("setUpMediaRecorder", "Failed to create directory for audio recordings.");
            return;

        }

        String fileName = UUID.randomUUID().toString() + "_audio_record.m4a";
        File audioFile = new File(audioDir, fileName);

        audio_path = audioFile.getAbsolutePath();
        mediaRecorder = new MediaRecorder();

        try {

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(audio_path);

        } catch (Exception e) {

            Log.e("setUpMediaRecorder", "Exception setting up media recorder", e);

        }

    }

    private void openGalery() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 45);

    }

    @SuppressLint("QueryPermissionsNeeded")
    private void openCamera() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            startActivityForResult(takePictureIntent, 440);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {

            imageUri = data.getData();

            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                reviewImage(bitmap);

            } catch (IOException ignored) { }

        }

        if (requestCode == 440 && resultCode == RESULT_OK) {

            if (data != null && data.getExtras() != null) {

                ProgressDialog progressDialog = new ProgressDialog(ChatActivity.this);
                progressDialog.setMessage("Sending image...");
                progressDialog.show();
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                if (imageBitmap != null) {

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] dataImage = baos.toByteArray();

                    UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("ImagesChats/").putBytes(dataImage);
                    uploadTask.addOnFailureListener(exception -> progressDialog.dismiss()).addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        chatService.sendImage(imageUrl);
                        progressDialog.dismiss();

                    }));

                }

            }

        }

    }

    private void reviewImage(Bitmap bitmap) {
        new DialogReviewSendImage(ChatActivity.this, bitmap).show(() -> {

            if (imageUri != null) {

                ProgressDialog progressDialog = new ProgressDialog(ChatActivity.this);
                progressDialog.setMessage("Sending image...");
                progressDialog.show();

                binding.layoutShow.setVisibility(View.GONE);
                isActionShown = false;

                new FirebaseService(ChatActivity.this).uploadImageToFirebaseStorage(imageUri, new FirebaseService.OnCallBack() {

                    @Override
                    public void onUploadSuccess(String imageUrl) {

                        chatService.sendImage(imageUrl);
                        progressDialog.dismiss();

                    }

                    @Override
                    public void onUploadFailed(Exception e) { progressDialog.dismiss(); }

                });

            }

        });

    }

    @Override
    protected void onResume() {

        super.onResume();
        chatMessagesReference.addValueEventListener(valueEventListener);
        String currentId = FirebaseAuth.getInstance().getUid();
        assert currentId != null;
        reference.child("presence").child(currentId).setValue("Online");

    }

    @Override
    protected void onPause() {

        super.onPause();
        if (valueEventListener != null) {

            chatMessagesReference.removeEventListener(valueEventListener);

        }

        String currentId = FirebaseAuth.getInstance().getUid();
        assert currentId != null;
        reference.child("presence").child(currentId).setValue("Online");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.chat_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return super.onOptionsItemSelected(item);

    }

//    public void sendNotification(String name, String message, String token) {
//
//        try {
//
//            RequestQueue queue = Volley.newRequestQueue(this);
//
//            String url = "https://fcm.googleapis.com/fcm/send";
//
//            JSONObject data = new JSONObject();
//            data.put("title", name);
//            data.put("body", message);
//            JSONObject notificationData = new JSONObject();
//            notificationData.put("notification", data);
//            notificationData.put("to", token);
//            JsonObjectRequest request = new JsonObjectRequest(url, notificationData
//                    , jsonObject -> Toast.makeText(ChatActivity.this, "success", Toast.LENGTH_SHORT).show(), volleyError -> Toast.makeText(ChatActivity.this, volleyError.getLocalizedMessage(), Toast.LENGTH_SHORT).show()) {
//                @Override
//                public Map<String, String> getHeaders() {
//
//                    HashMap<String, String> map = new HashMap<>();
//                    String Key = "Key=AAAADymcVLA:APA91bF3XAg92K_B8z4Mf5_PTAy-PerXAWzzt4upFBdYb86GgzL9pBEMWiht4Xsxeq8PQ4IBzI4useWuPsG0LVYwDB5upY593SB254cCqRZzyABGuBM4D0ilC4Isn3eCapNa0WkiA_1R";
//                    map.put("Authorization", Key);
//                    map.put("Content-Type", "application/json");
//                    return map;
//
//                }
//
//            };
//
//            queue.add(request);
//            Log.d("NotificationRequest", "Notification request added to the queue");
//
//        } catch (Exception e) {
//
//            Log.e("NotificationError", "Error sending notification", e);
//
//        }
//
//    }

}