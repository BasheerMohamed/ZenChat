package com.basheer.whatsappclone.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.basheer.whatsappclone.R;
import com.basheer.whatsappclone.model.Users;
import com.basheer.whatsappclone.activities.chat.ChatActivity;
import com.bumptech.glide.Glide;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    List<Users> list;
    Context context;

    public ContactsAdapter(List<Users> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_contact_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsAdapter.ViewHolder holder, int position) {

        Users user = list.get(position);

        holder.userName.setText(user.getUserName());

        if(user.getBio().isEmpty()) {
            holder.desc.setText(user.getUserPhone());
        } else {
            holder.desc.setText(user.getBio());
        }

        Glide.with(context).load(user.getImageProfile()).placeholder(R.drawable.avatar).into(holder.userProfile);

        holder.itemView.setOnClickListener(v ->
                context.startActivity(new Intent(context, ChatActivity.class)
                .putExtra("userID", user.getUserID())
                .putExtra("userName", user.getUserName())
                .putExtra("userProfile", user.getImageProfile())
                .putExtra("token", user.getToken())));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView userProfile;
        private final TextView userName;
        private final TextView desc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userProfile = itemView.findViewById(R.id.image_profile);
            userName = itemView.findViewById(R.id.tv_name);
            desc = itemView.findViewById(R.id.tv_desc);

        }
    }
}
