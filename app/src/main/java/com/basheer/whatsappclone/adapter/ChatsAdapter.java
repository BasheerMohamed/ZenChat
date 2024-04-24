package com.basheer.whatsappclone.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.basheer.whatsappclone.R;
import com.basheer.whatsappclone.activities.chat.ViewChatImageZoomActivity;
import com.basheer.whatsappclone.databinding.DeleteDialogBinding;
import com.basheer.whatsappclone.databinding.ItemReceiveBinding;
import com.basheer.whatsappclone.databinding.ItemSentBinding;
import com.basheer.whatsappclone.manage.AudioService;
import com.basheer.whatsappclone.model.Chats;
import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ChatsAdapter extends RecyclerView.Adapter {

    List<Chats> list;
    Context context;
    AudioService audioService;
    ImageView tmpBtnPlay;
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;
    String senderRoom;
    String receiverRoom;

    public ChatsAdapter(List<Chats> list, Context context, String senderRoom, String receiverRoom) {
        this.list = list;
        this.context = context;
        this.audioService = new AudioService(context);
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

//    public void setList(List<Chats> list) {
//        this.list = list;
//    }
//    @NonNull
//    @Override
//    public ChatsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//
////        View view;
////        if (viewType == MSG_TYPE_LEFT) {
////            view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
////        } else {
////            view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
////        }
////        return new ViewHolder(view);
//    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Chats chats = list.get(position);
        if (Objects.equals(FirebaseAuth.getInstance().getUid(), chats.getSender())) {
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Chats chats = list.get(position);

        int[] reactions = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {

            if(pos < 0) {
                return false;
            }

            if(holder.getClass() == SentViewHolder.class) {
                SentViewHolder viewHolder = (SentViewHolder) holder;
                viewHolder.binding.senderFeeling.setImageResource(reactions[pos]);
                viewHolder.binding.senderFeeling.setVisibility(View.VISIBLE);
            } else {
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                viewHolder.binding.receiverFeeling.setImageResource(reactions[pos]);
                viewHolder.binding.receiverFeeling.setVisibility(View.VISIBLE);
            }

            chats.setFeeling(pos);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(chats.getMessageId()).setValue(chats);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child("messages")
                    .child(chats.getMessageId()).setValue(chats);

            return true; // true is closing popup, false is requesting a new selection
        });

        if (holder.getClass() == SentViewHolder.class) {
            SentViewHolder viewHolder = (SentViewHolder)holder;

            switch (chats.getType()) {

                case "IMAGE" :

                    viewHolder.binding.imageMessage.setVisibility(View.VISIBLE);
                    viewHolder.binding.textMessage.setVisibility(View.GONE);
                    viewHolder.binding.linearLayoutAudio.setVisibility(View.GONE);
                    Glide.with(context)
                            .load(chats.getUrl())
                            .placeholder(R.drawable.placeholder)
                            .into(viewHolder.binding.imageMessage);

                    viewHolder.binding.imageMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String imageUrl = chats.getUrl();
                            Intent intent = new Intent(context, ViewChatImageZoomActivity.class);
                            intent.putExtra("imageUrl", imageUrl);
                            context.startActivity(intent);
                        }
                    });

                    break;

                case "VOICE" :

                    viewHolder.binding.linearLayoutMessage.setVisibility(View.GONE);
                    viewHolder.binding.linearLayoutAudio.setVisibility(View.VISIBLE);

                    viewHolder.binding.linearLayoutAudio.setOnClickListener(v -> {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @SuppressLint("UseCompatLoadingForDrawables")
                            @Override
                            public void run() {
                                if (tmpBtnPlay != null) {
                                    tmpBtnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon));
                                }
                                viewHolder.binding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_icon));
                            }
                        });

//                        audioService.playAudioFromUrl(chats.getUrl(), () -> {
//                            Activity activity = (Activity) context;
//                            if (!activity.isFinishing() && !activity.isDestroyed()) {
//                                activity.runOnUiThread(() -> viewHolder.binding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon)));
//                            }
//                        });

                        audioService.playAudioFromUrl(chats.getUrl(), () -> {
                            if (context instanceof Activity) {
                                Activity activity = (Activity) context;
                                if (!activity.isFinishing() && !activity.isDestroyed()) {
                                    activity.runOnUiThread(() -> {
                                        // Make sure the context is still valid
                                        if (context != null) {
                                            viewHolder.binding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon));
                                        }
                                    });
                                }
                            }
                        });

                        tmpBtnPlay = viewHolder.binding.playButton;
                    });

                    break;

                case "TEXT" :
                    viewHolder.binding.imageMessage.setVisibility(View.GONE);
                    viewHolder.binding.linearLayoutAudio.setVisibility(View.GONE);
                    viewHolder.binding.textMessage.setVisibility(View.VISIBLE);
                    viewHolder.binding.textMessage.setText(chats.getTextMessage());
            }

            if(chats.getFeeling() >= 0) {
                viewHolder.binding.senderFeeling.setImageResource(reactions[chats.getFeeling()]);
                viewHolder.binding.senderFeeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.senderFeeling.setVisibility(View.GONE);
            }

            viewHolder.binding.textMessage.setOnTouchListener((v, event) -> {
                popup.onTouch(v, event);
                return false;
            });

//            viewHolder.binding.imageMessage.setOnTouchListener((v, event) -> {
//                popup.onTouch(v, event);
//                return false;
//            });

            viewHolder.itemView.setOnLongClickListener(v -> {
                View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);

                DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Delete Message")
                        .setView(binding.getRoot())
                        .create();

                if(viewHolder.binding.textMessage.getText().toString().equals("This message is removed.")) {
                    binding.everyone.setVisibility(View.GONE);
                } else {

                    binding.everyone.setOnClickListener(v1 -> {

                        chats.setTextMessage("This message is removed.");
                        chats.setFeeling(-1);
                        chats.setUrl("");
                        chats.setType("TEXT");
                        Date date = new Date();

                        HashMap<String, Object> lastMsgObj = new HashMap<>();
                        lastMsgObj.put("lastMsg", chats.getTextMessage());
                        lastMsgObj.put("lastMsgTime", date.getTime());

                        FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                        FirebaseDatabase.getInstance().getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                        FirebaseDatabase.getInstance().getReference()
                                .child("chats")
                                .child(senderRoom)
                                .child("messages")
                                .child(chats.getMessageId()).setValue(chats);

                        FirebaseDatabase.getInstance().getReference()
                                .child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(chats.getMessageId()).setValue(chats);

                        dialog.dismiss();

                    });
                }

                binding.delete.setOnClickListener(v12 -> {
                    FirebaseDatabase.getInstance().getReference()
                            .child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(chats.getMessageId()).setValue(null);
                    dialog.dismiss();
                });

                binding.cancel.setOnClickListener(v13 -> {
                    dialog.dismiss();
                });

                dialog.show();

                return false;
            });

        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;

            switch (chats.getType()) {

                case "IMAGE" :

                    viewHolder.binding.imageMessage.setVisibility(View.VISIBLE);
                    viewHolder.binding.textMessage.setVisibility(View.GONE);
                    viewHolder.binding.linearLayoutAudio.setVisibility(View.GONE);
                    Glide.with(context)
                            .load(chats.getUrl())
                            .placeholder(R.drawable.placeholder)
                            .into(viewHolder.binding.imageMessage);

                    viewHolder.binding.imageMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String imageUrl = chats.getUrl();
                            Intent intent = new Intent(context, ViewChatImageZoomActivity.class);
                            intent.putExtra("imageUrl", imageUrl);
                            context.startActivity(intent);
                        }
                    });

                    break;

                case "VOICE" :

                    viewHolder.binding.linearLayoutMessage.setVisibility(View.GONE);
                    viewHolder.binding.linearLayoutAudio.setVisibility(View.VISIBLE);

                    viewHolder.binding.linearLayoutAudio.setOnClickListener(v -> {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @SuppressLint("UseCompatLoadingForDrawables")
                            @Override
                            public void run() {
                                if (tmpBtnPlay != null) {
                                    tmpBtnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon));
                                }
                                viewHolder.binding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_icon));
                            }
                        });

                        audioService.playAudioFromUrl(chats.getUrl(), () -> {
                            Activity activity = (Activity) context;
                            if (!activity.isFinishing() && !activity.isDestroyed()) {
                                activity.runOnUiThread(() -> viewHolder.binding.playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon)));
                            }
                        });

                        tmpBtnPlay = viewHolder.binding.playButton;
                    });

                    break;

                case "TEXT" :
                    viewHolder.binding.imageMessage.setVisibility(View.GONE);
                    viewHolder.binding.linearLayoutAudio.setVisibility(View.GONE);
                    viewHolder.binding.textMessage.setVisibility(View.VISIBLE);
                    viewHolder.binding.textMessage.setText(chats.getTextMessage());
                    break;

            }

            if(chats.getFeeling() >= 0) {
                viewHolder.binding.receiverFeeling.setImageResource(reactions[chats.getFeeling()]);
                viewHolder.binding.receiverFeeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.receiverFeeling.setVisibility(View.GONE);
            }

            viewHolder.binding.textMessage.setOnTouchListener((v, event) -> {
                popup.onTouch(v, event);
                return false;
            });

//            viewHolder.binding.imageMessage.setOnTouchListener((v, event) -> {
//                popup.onTouch(v, event);
//                return false;
//            });

            viewHolder.itemView.setOnLongClickListener(v -> {
                View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Delete Message")
                        .setView(binding.getRoot())
                        .create();

                binding.everyone.setVisibility(View.GONE);

                binding.delete.setOnClickListener(v15 -> {
                    FirebaseDatabase.getInstance().getReference()
                            .child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(chats.getMessageId()).setValue(null);
                    dialog.dismiss();
                });

                binding.cancel.setOnClickListener(v14 -> {
                    dialog.dismiss();
                });

                dialog.show();

                return false;
            });

        }
    }

//    @Override
//    public void onBindViewHolder(@NonNull ChatsAdapter.ViewHolder holder, int position) {
//        holder.bind(list.get(position));
//    }

    @Override
    public int getItemCount() {
        return list.size();
    }

//    public class ViewHolder extends RecyclerView.ViewHolder {
//        private final TextView textMessage;
//        private final ImageView imageMessage;
//        private final ImageView playButton;
//        private final LinearLayout audioLayout;
//        private final LinearLayout messageLayout;
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            textMessage = itemView.findViewById(R.id.textMessage);
//            imageMessage = itemView.findViewById(R.id.imageMessage);
//            audioLayout = itemView.findViewById(R.id.linearLayoutAudio);
//            messageLayout = itemView.findViewById(R.id.linearLayoutMessage);
//            playButton = itemView.findViewById(R.id.playButton);
//
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//
//                    Chats chats = list.get(getBindingAdapterPosition());
//
//                    View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
//                    DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
//                    AlertDialog dialog = new AlertDialog.Builder(context)
//                            .setTitle("Delete Message")
//                            .setView(binding.getRoot())
//                            .create();
//
//                    if (Objects.equals(chats.getSender(), FirebaseAuth.getInstance().getUid())) {
//
//                        if (textMessage.getText().toString().equals("This message is removed.")) {
//                            binding.everyone.setVisibility(View.GONE);
//                        } else {
//
//                            binding.everyone.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//
//                                    chats.setTextMessage("This message is removed.");
//
//                                    Date date = new Date();
//
//                                    HashMap<String, Object> lastMsgObj = new HashMap<>();
//                                    lastMsgObj.put("lastMsg", chats.getTextMessage());
//                                    lastMsgObj.put("lastMsgTime", date.getTime());
//
//                                    FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
//                                    FirebaseDatabase.getInstance().getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);
//
//                                    FirebaseDatabase.getInstance().getReference()
//                                            .child("chats")
//                                            .child(senderRoom)
//                                            .child("messages")
//                                            .child(chats.getMessageId()).setValue(chats);
//
//                                    FirebaseDatabase.getInstance().getReference()
//                                            .child("chats")
//                                            .child(receiverRoom)
//                                            .child("messages")
//                                            .child(chats.getMessageId()).setValue(chats);
//
//                                    dialog.dismiss();
//                                }
//                            });
//                        }
//
//                        binding.delete.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//
//                                Date date = new Date();
//
//                                HashMap<String, Object> lastMsgObj = new HashMap<>();
//                                lastMsgObj.put("lastMsg", chats.getTextMessage());
//                                lastMsgObj.put("lastMsgTime", date.getTime());
//
//                                FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
//                                FirebaseDatabase.getInstance().getReference()
//                                        .child("chats")
//                                        .child(senderRoom)
//                                        .child("messages")
//                                        .child(chats.getMessageId()).setValue(null);
//                                dialog.dismiss();
//                            }
//                        });
//
//                        binding.cancel.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                dialog.dismiss();
//                            }
//                        });
//
//                        dialog.show();
//
//                    }else {
//
//                        binding.everyone.setVisibility(View.GONE);
//
//                        binding.delete.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                FirebaseDatabase.getInstance().getReference()
//                                        .child("chats")
//                                        .child(senderRoom)
//                                        .child("messages")
//                                        .child(chats.getMessageId()).setValue(null);
//                                dialog.dismiss();
//                            }
//                        });
//
//                        binding.cancel.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                dialog.dismiss();
//                            }
//                        });
//                    }
//                    dialog.show();
//                    return true;
//                }
//            });
//
//        }
//
//        @SuppressLint("UseCompatLoadingForDrawables")
//        void bind(Chats chats) {
//
//            switch (chats.getType()) {
//
//                case "TEXT" :
//                    textMessage.setVisibility(View.VISIBLE);
//                    imageMessage.setVisibility(View.GONE);
//                    audioLayout.setVisibility(View.GONE);
//                    textMessage.setText(chats.getTextMessage());
//                    break;
//
//                case "IMAGE" :
//                    textMessage.setVisibility(View.GONE);
//                    imageMessage.setVisibility(View.VISIBLE);
//                    audioLayout.setVisibility(View.GONE);
//                    Glide.with(context).load(chats.getUrl()).into(imageMessage);
//
//                    imageMessage.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            String imageUrl = chats.getUrl();
//                            Intent intent = new Intent(context, ViewChatImageZoomActivity.class);
//                            intent.putExtra("imageUrl", imageUrl);
//                            context.startActivity(intent);
//                        }
//                    });
//                    break;
//
//                case "VOICE" :
//                    messageLayout.setVisibility(View.GONE);
//                    audioLayout.setVisibility(View.VISIBLE);
//
//                    audioLayout.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            // Ensure we are running on UI thread when updating UI elements
//                            ((Activity) context).runOnUiThread(new Runnable() {
//                                @SuppressLint("UseCompatLoadingForDrawables")
//                                @Override
//                                public void run() {
//                                    if (tmpBtnPlay != null) {
//                                        tmpBtnPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon));
//                                    }
//                                    playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pause_icon));
//                                }
//                            });
//
//                            audioService.playAudioFromUrl(chats.getUrl(), new AudioService.onPlayCallBack() {
//                                @Override
//                                public void onFinished() {
//                                    // Assuming 'context' is an Activity; otherwise, adjust accordingly.
//                                    Activity activity = (Activity) context;
//                                    if (!activity.isFinishing() && !activity.isDestroyed()) {
//                                        activity.runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                if (playButton != null) {
//                                                    playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_icon));
//                                                }
//                                            }
//                                        });
//                                    }
//                                }
//                            });
//
//                            tmpBtnPlay = playButton;
//                        }
//                    });
//
//
//                    break;
//            }
//        }
//    }

//    @Override
//    public int getItemViewType(int position) {
//        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (list.get(position).getSender().equals(firebaseUser.getUid())) {
//            return MSG_TYPE_RIGHT;
//        } else {
//            return MSG_TYPE_LEFT;
//        }
//    }

    public static class SentViewHolder extends RecyclerView.ViewHolder {

        ItemSentBinding binding;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);
        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {

        ItemReceiveBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }
}
