package com.basheer.whatsappclone.adapter;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.basheer.whatsappclone.R;
import com.basheer.whatsappclone.model.CallList;
import com.basheer.whatsappclone.model.ChatList;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallListAdapter extends RecyclerView.Adapter<CallListAdapter.Holder> {

    private List<CallList> list;
    private Context context;

    public CallListAdapter(List<CallList> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         View view = LayoutInflater.from(context).inflate(R.layout.layout_call_list, parent, false);
         return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallListAdapter.Holder holder, int position) {

        CallList callList = list.get(position);

        holder.tvName.setText(callList.getUserName());
        holder.tvDate.setText(callList.getDate());

        if(callList.getCallType().equals("missed")) {
            holder.arrow.setImageDrawable(context.getDrawable(R.drawable.arrow_down_icon));
            holder.arrow.getDrawable().setTint(context.getResources().getColor(R.color.red));
        } else if(callList.getCallType().equals("income")){
            holder.arrow.setImageDrawable(context.getDrawable(R.drawable.arrow_down_icon));
            holder.arrow.getDrawable().setTint(context.getResources().getColor(R.color.green));
        } else {
            holder.arrow.setImageDrawable(context.getDrawable(R.drawable.arrow_up_icon));
            holder.arrow.getDrawable().setTint(context.getResources().getColor(R.color.green));
        }

        Glide.with(context).load(callList.getUrlProfile()).into(holder.profile);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private TextView tvName, tvDate;
        private CircleImageView profile;
        private ImageView arrow;

        public Holder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            profile = itemView.findViewById(R.id.image_profile);
            arrow = itemView.findViewById(R.id.image_arrow);
        }
    }
}
