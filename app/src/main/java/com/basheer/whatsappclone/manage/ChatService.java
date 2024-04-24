package com.basheer.whatsappclone.manage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.basheer.whatsappclone.activities.chat.ChatActivity;
import com.basheer.whatsappclone.model.Chats;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatService {
    Context context;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    String receiverID;
    String senderRoom;
    String receiverRoom;
    private String userToken, name;
    ChatActivity chatActivity = new ChatActivity();

    public ChatService(Context context, String receiverID) {
        this.context = context;
        this.receiverID = receiverID;
    }

    public void sendTextMsg(String text) {

        senderRoom = firebaseUser.getUid() + receiverID;
        receiverRoom = receiverID + firebaseUser.getUid();

        String messageId = reference.push().getKey();

        Chats chats = new Chats(
                getCurrentDate(),
                getCurrentTime(),
                messageId,
                text,
                "",
                "TEXT",
                firebaseUser.getUid(),
                receiverID
        );

        Date date = new Date();

        String randomKey = reference.push().getKey();

        HashMap<String, Object> lastMsgObj = new HashMap<>();
        lastMsgObj.put("lastMsg", chats.getTextMessage());
        lastMsgObj.put("lastMsgTime", date.getTime());

        reference.child("chats").child(senderRoom).updateChildren(lastMsgObj);
        reference.child("chats").child(receiverRoom).updateChildren(lastMsgObj);

        assert randomKey != null;
        reference.child("chats")
                .child(senderRoom)
                .child("messages")
                .child(randomKey)
                .setValue(chats).addOnSuccessListener(aVoid ->
                        reference.child("chats")
                        .child(receiverRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(chats).addOnSuccessListener(aVoid1 ->
                                FirebaseFirestore.getInstance().collection("Users").document(receiverID).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    userToken = document.getString("token");
                                    FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.getUid()).get().addOnCompleteListener(task1 -> {
                                        if(task1.isSuccessful()) {
                                            DocumentSnapshot documentSnapshot = task1.getResult();
                                            name = documentSnapshot.getString("userName");
                                            Log.d("print", userToken);
                                            Log.d("print", name);
                                            Log.d("print", text);
                                            sendNotification(name, text, userToken);
                                        }
                                    });
                                }
                            }
                        })));

        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid()).child(receiverID);
        chatRef1.child("chatid").setValue(receiverID);

        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList").child(receiverID).child(firebaseUser.getUid());
        chatRef2.child("chatid").setValue(firebaseUser.getUid());

    }

    public void sendImage(String imageUrl) {

        senderRoom = firebaseUser.getUid() + receiverID;
        receiverRoom = receiverID + firebaseUser.getUid();

        String messageId = reference.push().getKey();

        Chats chats = new Chats(
                getCurrentDate(),
                getCurrentTime(),
                messageId,
                "",
                imageUrl,
                "IMAGE",
                firebaseUser.getUid(),
                receiverID
        );

        Date date = new Date();

        String randomKey = reference.push().getKey();

        HashMap<String, Object> lastMsgObj = new HashMap<>();
        lastMsgObj.put("lastMsg", "Photo");
        lastMsgObj.put("lastMsgTime", date.getTime());

        reference.child("chats").child(senderRoom).updateChildren(lastMsgObj);
        reference.child("chats").child(receiverRoom).updateChildren(lastMsgObj);

        assert randomKey != null;
        reference.child("chats")
                .child(senderRoom)
                .child("messages")
                .child(randomKey)
                .setValue(chats).addOnSuccessListener(aVoid -> reference.child("chats")
                        .child(receiverRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(chats).addOnSuccessListener(aVoid1 ->
                                FirebaseFirestore.getInstance().collection("Users").document(receiverID).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    userToken = document.getString("token");
                                    FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.getUid()).get().addOnCompleteListener(task1 -> {
                                        if(task1.isSuccessful()) {
                                            DocumentSnapshot documentSnapshot = task1.getResult();
                                            name = documentSnapshot.getString("userName");
                                            Log.d("print", userToken);
                                            Log.d("print", name);
                                            Log.d("print", chats.getType());
                                            sendNotification(name, chats.getType(), userToken);
                                        }
                                    });
                                }
                            }
                        })));

        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid()).child(receiverID);
        chatRef1.child("chatid").setValue(receiverID);

        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList").child(receiverID).child(firebaseUser.getUid());
        chatRef2.child("chatid").setValue(firebaseUser.getUid());

    }

    public String getCurrentDate() {

        Date date = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String today = formatter.format(date);

        Calendar currentDateTime = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
        String currentTime = df.format(currentDateTime.getTime());

        return today;

    }

    public String getCurrentTime() {

        Calendar currentDateTime = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
        String currentTime = df.format(currentDateTime.getTime());

        return currentTime;

    }

    public void sendVoice(String audioPath) {

        senderRoom = firebaseUser.getUid() + receiverID;
        receiverRoom = receiverID + firebaseUser.getUid();

        Uri uriAudio = Uri.fromFile(new File(audioPath));
        StorageReference audioRef = FirebaseStorage.getInstance().getReference().child("Chats/Voice/" + System.currentTimeMillis());

        String messageId = reference.push().getKey();

        audioRef.putFile(uriAudio).addOnSuccessListener(taskSnapshot -> {
            // Once the upload is successful, get the download URL
            taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUrl = task.getResult();
                    String voiceUrl = String.valueOf(downloadUrl);

                    // Assuming 'firebaseUser' and 'receiverID' are defined elsewhere in your class
                    Chats chats = new Chats(
                            getCurrentDate(),
                            getCurrentTime(),
                            messageId,
                            "",
                            voiceUrl,
                            "VOICE",
                            firebaseUser.getUid(),
                            receiverID
                    );

                    Date date = new Date();

                    String randomKey = reference.push().getKey();

                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                    lastMsgObj.put("lastMsg", "Audio");
                    lastMsgObj.put("lastMsgTime", date.getTime());

                    reference.child("chats").child(senderRoom).updateChildren(lastMsgObj);
                    reference.child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                    assert randomKey != null;
                    reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(randomKey)
                            .setValue(chats).addOnSuccessListener(aVoid -> reference.child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(randomKey)
                                    .setValue(chats).addOnSuccessListener(aVoid1 ->
                                            FirebaseFirestore.getInstance().collection("Users").document(receiverID).get().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot document = task1.getResult();
                                            if (document != null && document.exists()) {
                                                userToken = document.getString("token");
                                                FirebaseFirestore.getInstance().collection("Users").document(firebaseUser.getUid()).get().addOnCompleteListener(task2 -> {
                                                    if(task2.isSuccessful()) {
                                                        DocumentSnapshot documentSnapshot = task2.getResult();
                                                        name = documentSnapshot.getString("userName");
                                                        Log.d("print", userToken);
                                                        Log.d("print", name);
                                                        Log.d("print", chats.getType());
                                                        sendNotification(name, chats.getType(), userToken);
                                                    }
                                                });
                                            }
                                        }
                                    })));

                    // Update ChatList for both users
                    DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid()).child(receiverID);
                    chatRef1.child("chatid").setValue(receiverID);

                    DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList").child(receiverID).child(firebaseUser.getUid());
                    chatRef2.child("chatid").setValue(firebaseUser.getUid());

                } else {
                    Log.e("SendVoice", "Error getting download URL", task.getException());
                }
            });
        }).addOnFailureListener(e -> Log.d("Send", "Upload failure: " + e.getMessage()));
    }

    public void sendNotification(String name, String message, String token) {

        try {

            RequestQueue queue = Volley.newRequestQueue(context);

            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", message);
            JSONObject notificationData = new JSONObject();
            notificationData.put("notification", data);
            notificationData.put("to", token);
            JsonObjectRequest request = new JsonObjectRequest(url, notificationData
                    , jsonObject -> Toast.makeText(context, "success", Toast.LENGTH_SHORT).show(), volleyError -> Toast.makeText(context, volleyError.getLocalizedMessage(), Toast.LENGTH_SHORT).show()) {
                @Override
                public Map<String, String> getHeaders() {

                    HashMap<String, String> map = new HashMap<>();
                    String Key = "Key=AAAADymcVLA:APA91bF3XAg92K_B8z4Mf5_PTAy-PerXAWzzt4upFBdYb86GgzL9pBEMWiht4Xsxeq8PQ4IBzI4useWuPsG0LVYwDB5upY593SB254cCqRZzyABGuBM4D0ilC4Isn3eCapNa0WkiA_1R";
                    map.put("Authorization", Key);
                    map.put("Content-Type", "application/json");
                    return map;

                }

            };

            queue.add(request);
            Log.d("NotificationRequest", "Notification request added to the queue");

        } catch (Exception e) {

            Log.e("NotificationError", "Error sending notification", e);

        }

    }
}
