package com.basheer.whatsappclone.dialog;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import com.basheer.whatsappclone.R;
import com.basheer.whatsappclone.activities.chat.ChatActivity;
import com.basheer.whatsappclone.activities.profile.UserProfileActivity;
import com.basheer.whatsappclone.activities.profile.ViewProfileZoomActivity;
import com.basheer.whatsappclone.common.Common;
import com.basheer.whatsappclone.model.ChatList;
import com.bumptech.glide.Glide;
import java.util.Objects;

public class DialogViewUser {

    Context context;
    ImageButton chatButton, callButton, videoCallButton, infoButton;
    ImageView profile;
    TextView userName;
    static int PERMISSION_CODE = 100;

    public DialogViewUser(Context context, ChatList chatList) {
        this.context = context;
        initialize(chatList);
    }

    public void initialize(ChatList chatList) {

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR);
        dialog.setContentView(R.layout.dialog_view_user);

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(Objects.requireNonNull(dialog.getWindow().getAttributes()));
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        chatButton = dialog.findViewById(R.id.chat);
        callButton = dialog.findViewById(R.id.call);
        videoCallButton = dialog.findViewById(R.id.vedioCall);
        infoButton = dialog.findViewById(R.id.profile);
        profile = dialog.findViewById(R.id.userProfileImage);
        userName = dialog.findViewById(R.id.userName);

        userName.setText(chatList.getUserName());
        Glide.with(context).load(chatList.getUrlProfile()).into(profile);

        chatButton.setOnClickListener(v -> {
            context.startActivity(new Intent(context, ChatActivity.class)
                    .putExtra("userID", chatList.getUserID())
                    .putExtra("userName", chatList.getUserName())
                    .putExtra("userProfile", chatList.getUrlProfile())
                    .putExtra("mobileNumber", chatList.getUserPhone()));

            dialog.dismiss();
        });

        infoButton.setOnClickListener(v -> {
            context.startActivity(new Intent(context, UserProfileActivity.class)
                    .putExtra("userID", chatList.getUserID())
                    .putExtra("imageProfile", chatList.getUrlProfile())
                    .putExtra("mobileNumber", chatList.getUserPhone())
                    .putExtra("userName", chatList.getUserName()));

            dialog.dismiss();
        });

        profile.setOnClickListener(v -> {
            profile.invalidate();
            Drawable dr = profile.getDrawable();
            Common.IMAGE_BITMAP = ((BitmapDrawable)dr.getCurrent()).getBitmap();
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, profile, "image");
            Intent intent = new Intent(context, ViewProfileZoomActivity.class);
            context.startActivity(intent, activityOptionsCompat.toBundle());

            dialog.dismiss();
        });

        callButton.setOnClickListener(v -> {
            // Check if permission is granted
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);
            } else {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + chatList.getUserPhone()));
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    context.startActivity(intent);
                }
            }
            dialog.dismiss();
        });

        dialog.show();

    }

}
