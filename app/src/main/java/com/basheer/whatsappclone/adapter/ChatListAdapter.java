package com.basheer.whatsappclone.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.basheer.whatsappclone.R;
import com.basheer.whatsappclone.dialog.DialogViewUser;
import com.basheer.whatsappclone.model.ChatList;
import com.basheer.whatsappclone.activities.chat.ChatActivity;
import com.basheer.whatsappclone.model.Users;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.Holder> {

    private final List<ChatList> list;
    private final Context context;

    public ChatListAdapter(List<ChatList> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         View view = LayoutInflater.from(context).inflate(R.layout.layout_chat_list, parent, false);
         return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListAdapter.Holder holder, int position) {

        ChatList chatList = list.get(position);

        holder.userName.setText(chatList.getUserName());

        String senderId = FirebaseAuth.getInstance().getUid();

        String senderRoom = senderId + chatList.getUserID();

        FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                            long time = snapshot.child("lastMsgTime").getValue(Long.class);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                            holder.date.setText(dateFormat.format(new Date(time)));
                            holder.description.setText(lastMsg);
                        } else {
                            holder.description.setText("Tap to chat");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if (chatList.getUrlProfile().isEmpty()) {
            holder.userProfileImage.setImageResource(R.drawable.avatar);
        } else {
            Glide.with(context).load(chatList.getUrlProfile()).into(holder.userProfileImage);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ChatActivity.class)
                        .putExtra("userID", chatList.getUserID())
                        .putExtra("userName", chatList.getUserName())
                        .putExtra("userProfile", chatList.getUrlProfile())
                        .putExtra("mobileNumber", chatList.getUserPhone()));
            }
        });

        holder.userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogViewUser(context, chatList);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private final TextView userName;
        private final TextView description;
        private final TextView date;
        private final CircleImageView userProfileImage;
        public Holder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.userName);
            description = itemView.findViewById(R.id.description);
            date = itemView.findViewById(R.id.date);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
        }
    }
}
