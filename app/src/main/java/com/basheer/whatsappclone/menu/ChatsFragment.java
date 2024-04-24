package com.basheer.whatsappclone.menu;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.basheer.whatsappclone.adapter.ChatListAdapter;
import com.basheer.whatsappclone.databinding.FragmentChatBinding;
import com.basheer.whatsappclone.model.ChatList;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatsFragment extends Fragment {

    FragmentChatBinding binding;
    private static final String TAG = "ChatsFragment";
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    FirebaseFirestore firestore;
    List<ChatList> list;
    private ArrayList<String> allUserID;
    private final Handler handler = new Handler();
    private ChatListAdapter adapter;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentChatBinding.inflate(getLayoutInflater());

        list = new ArrayList<>();
        allUserID = new ArrayList<>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatListAdapter(list, getContext());
        binding.recyclerView.setAdapter(adapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        if (firebaseUser != null) {
            getChatList();
        }

        return binding.getRoot();
    }

    private void getChatList() {
        binding.progressBar.setVisibility(View.VISIBLE);
        list.clear();
        allUserID.clear();
        reference.child("ChatList").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userID = Objects.requireNonNull(snapshot.child("chatid").getValue()).toString();
                    Log.d(TAG, "onDataChange : userid " + userID);
                    binding.inviteLayout.setVisibility(View.GONE);
                    binding.progressBar.setVisibility(View.GONE);
                    allUserID.add(userID);
                }
                getUserInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserInfo() {

        handler.post(() -> {
            for (String userID : allUserID) {
                firestore.collection("Users").document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d(TAG, "onSuccess : ddd" + documentSnapshot.getString("userName"));
                        try {
                            ChatList chat = new ChatList(
                                    documentSnapshot.getString("userID"),
                                    documentSnapshot.getString("userName"),
                                    "This is description",
                                    "",
                                    documentSnapshot.getString("imageProfile"),
                                    documentSnapshot.getString("userPhone")
                            );
                            list.add(chat);
                        } catch (Exception e) {
                            Log.d(TAG, "onSuccess: "+e.getMessage());
                        }
                        if (adapter != null) {
                            adapter.notifyItemInserted(0);
                            adapter.notifyDataSetChanged();

                            Log.d(TAG, "onSuccess: adapter"+adapter.getItemCount());
                        }
                    }
                }).addOnFailureListener(e -> Log.d(TAG, "onFailure : Error " + e.getMessage()));
            }
        });
    }
}