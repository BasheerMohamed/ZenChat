package com.basheer.whatsappclone.manage;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseService {

    private final Context context;

    public FirebaseService(Context context) {
        this.context = context;
    }

//    public void uploadImageToFirebaseStorage(Uri uri, OnCallBack onCallBack) {
//
//        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ImagesChats/" + System.currentTimeMillis() + "." + getFileExtension(uri));
//        storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
//            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
//            while (!urlTask.isSuccessful());
//            Uri downloadUrl = urlTask.getResult();
//
//            final String sdownload_url = String.valueOf(downloadUrl);
//
//            onCallBack.onUploadSuccess(sdownload_url);
//
//        }).addOnFailureListener(onCallBack::onUploadFailed);
//
//    }

    public void uploadImageToFirebaseStorage(Uri uri, OnCallBack onCallBack) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ImagesChats/" + System.currentTimeMillis() + "." + getFileExtension(context, uri));

        storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            // Once the image is uploaded, get the download URL
            taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Successfully got the download URL
                    Uri downloadUrl = task.getResult();
                    String sDownloadUrl = String.valueOf(downloadUrl);
                    onCallBack.onUploadSuccess(sDownloadUrl);
                } else {
                    // Handle failure
                    onCallBack.onUploadFailed(task.getException());
                }
            });
        }).addOnFailureListener(onCallBack::onUploadFailed);
    }

    private String getFileExtension(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null; // Handle null context or uri
        }

        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            return null; // Handle null content resolver
        }

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public interface OnCallBack {
        void onUploadSuccess(String imageUrl);
        void onUploadFailed(Exception e);
    }

}
