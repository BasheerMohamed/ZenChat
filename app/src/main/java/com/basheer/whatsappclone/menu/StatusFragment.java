package com.basheer.whatsappclone.menu;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.basheer.whatsappclone.adapter.TopStatusAdapter;
import com.basheer.whatsappclone.databinding.FragmentStatusBinding;
import com.basheer.whatsappclone.model.Status;
import com.basheer.whatsappclone.model.UserStatus;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class StatusFragment extends Fragment {

    FragmentStatusBinding binding;
    TopStatusAdapter statusAdapter;
    ArrayList<UserStatus> userStatuses;
    FirebaseDatabase database;

    public StatusFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentStatusBinding.inflate(getLayoutInflater());

        database = FirebaseDatabase.getInstance();

        userStatuses = new ArrayList<>();

        statusAdapter = new TopStatusAdapter(getContext(), userStatuses);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.statusList.setLayoutManager(layoutManager);
        binding.statusList.setAdapter(statusAdapter);

        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    userStatuses.clear();
                    for(DataSnapshot storySnapshot : snapshot.getChildren()) {
                        UserStatus status = new UserStatus();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));

                        ArrayList<Status> statuses = new ArrayList<>();

                        for(DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()) {
                            Status sampleStatus = statusSnapshot.getValue(Status.class);
                            statuses.add(sampleStatus);
                        }

                        status.setStatuses(statuses);
                        userStatuses.add(status);
                    }
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return binding.getRoot();
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(data != null) {
//            if(data.getData() != null) {
//                FirebaseStorage storage = FirebaseStorage.getInstance();
//                Date date = new Date();
//                StorageReference reference = storage.getReference().child("status").child(date.getTime() + "");
//
//                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                        if(task.isSuccessful()) {
//                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                @Override
//                                public void onSuccess(Uri uri) {
//                                    senderID = FirebaseAuth.getInstance().getUid();
//                                    FirebaseFirestore.getInstance().collection("Users").document(senderID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                            if (task.isSuccessful()) {
//                                                DocumentSnapshot document = task.getResult();
//                                                if (document != null && document.exists()) {
//                                                    name = document.getString("userName");
//                                                    imageUrl = document.getString("imageProfile");
//
//                                                    UserStatus userStatus = new UserStatus();
//                                                    userStatus.setName(name);
//                                                    userStatus.setProfileImage(imageUrl);
//                                                    userStatus.setLastUpdated(date.getTime());
//
//                                                    HashMap<String, Object> obj = new HashMap<>();
//                                                    obj.put("name", userStatus.getName());
//                                                    obj.put("profileImage", userStatus.getProfileImage());
//                                                    obj.put("lastUpdated", userStatus.getLastUpdated());
//
//                                                    String imageUrl = uri.toString();
//                                                    Status status = new Status(imageUrl, userStatus.getLastUpdated());
//
//                                                    database.getReference().child("stories")
//                                                            .child(senderID)
//                                                            .updateChildren(obj);
//
//                                                    database.getReference().child("stories")
//                                                            .child(FirebaseAuth.getInstance().getUid())
//                                                            .child("statuses")
//                                                            .push()
//                                                            .setValue(status);
//
//                                                }
//                                            }
//                                        }
//                                    });
//                                }
//                            });
//                        }
//                    }
//                });
//            }
//        }
//    }
}