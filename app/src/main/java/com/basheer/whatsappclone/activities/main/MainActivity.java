package com.basheer.whatsappclone.activities.main;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.basheer.whatsappclone.R;
import com.basheer.whatsappclone.activities.chat.ContactsActivity;
import com.basheer.whatsappclone.activities.chatbot.ChatBotActivity;
import com.basheer.whatsappclone.databinding.ActivityMainBinding;
import com.basheer.whatsappclone.menu.CallsFragment;
import com.basheer.whatsappclone.menu.ChatsFragment;
import com.basheer.whatsappclone.menu.StatusFragment;
import com.basheer.whatsappclone.activities.settings.SettingsActivity;
import com.basheer.whatsappclone.model.Status;
import com.basheer.whatsappclone.model.UserStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private int currentFabAction = -1;
    DatabaseReference reference;
    FirebaseUser firebaseUser;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String senderID, name, imageUrl;
    private GestureDetector gestureDetector;
    private float xCoOrdinate, yCoOrdinate;
    FirebaseFirestore firestore;
    ProgressDialog dialog;
    private boolean isMoving = false;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reference = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image ...");
        dialog.setCancelable(false);

        setUpWithViewPager(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);
        binding.viewPager.setCurrentItem(0);
        changeFabIcon(0);

        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                changeFabIcon(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

        });

        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(token -> {

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("token", token);
                    firestore.collection("Users").document(firebaseUser.getUid()).update(map);

                });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageButton floatingImageButton = findViewById(R.id.chatbot);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(@NonNull MotionEvent e) {

                super.onLongPress(e);
                isMoving = true;

            }

            @Override
            public boolean onDown(@NonNull MotionEvent e) { return true; }

        });

        floatingImageButton.setOnClickListener(view -> {
            if (!isMoving) {

                Intent intent = new Intent(MainActivity.this, ChatBotActivity.class);
                startActivity(intent);

            }

        });

        floatingImageButton.setOnTouchListener((view, event) -> {

            gestureDetector.onTouchEvent(event);
            if (!isMoving) {

                return false;

            }

            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:
                    xCoOrdinate = view.getX() - event.getRawX();
                    yCoOrdinate = view.getY() - event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    view.animate()
                            .x(event.getRawX() + xCoOrdinate)
                            .y(event.getRawY() + yCoOrdinate)
                            .setDuration(0)
                            .start();
                    break;

                case MotionEvent.ACTION_UP:

                case MotionEvent.ACTION_CANCEL:
                    isMoving = false;
                    break;
            }

            return true;

        });

    }

    public void setUpWithViewPager(ViewPager viewPager) {

        MainActivity.SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ChatsFragment(), "chats");
        adapter.addFragment(new StatusFragment(), "status");
        adapter.addFragment(new CallsFragment(), "calls");
        viewPager.setAdapter(adapter);

    }

    public static class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mfragmentList = new ArrayList<>();
        private final List<String> mfragmentTitleList = new ArrayList<>();
        public SectionsPagerAdapter(FragmentManager manager) {
            super(manager);
        }
        @NonNull
        public Fragment getItem(int position) {return mfragmentList.get(position);}
        @Override
        public int getCount() {return mfragmentList.size();}
        public void addFragment(Fragment fragment, String title) {

            mfragmentList.add(fragment);
            mfragmentTitleList.add(title);

        }

        public CharSequence getPageTitle(int position) { return mfragmentTitleList.get(position); }

    }

    private void changeFabIcon(final int index) {

        binding.fabTab.setVisibility(View.GONE);
        binding.editTab.setVisibility(View.GONE);
        currentFabAction = index;
        new Handler().postDelayed(new Runnable() {

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void run() {

                switch (index) {

                    case 0:
                        binding.fabTab.setVisibility(View.VISIBLE);
                        binding.editTab.setVisibility(View.GONE);
                        binding.fabTab.setImageDrawable(getDrawable(R.drawable.chat_new_img));
                        break;

                    case 1:
                        binding.fabTab.setVisibility(View.VISIBLE);
                        binding.editTab.setVisibility(View.VISIBLE);
                        binding.fabTab.setImageDrawable(getDrawable(R.drawable.camera_icon));
                        break;

                    case 2:
                        binding.fabTab.setVisibility(View.GONE);
                        break;

                }

            }

        }, 400);
        onPerformClick();

    }

    private void onPerformClick() {
        binding.fabTab.setOnClickListener(v -> {

            switch (currentFabAction) {

                case 0:
                    startActivity(new Intent(MainActivity.this, ContactsActivity.class));
                    break;

                case 1:
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, 75);
                    break;

                case 2:
                    Toast.makeText(MainActivity.this, "Call page", Toast.LENGTH_SHORT).show();

            }

        });

        binding.editTab.setOnClickListener( v -> Toast.makeText(MainActivity.this, "Edit Status", Toast.LENGTH_SHORT).show() );

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();

        if (itemId == R.id.action_settings) {

            startActivity(new Intent(MainActivity.this, SettingsActivity.class));

        }

        if (itemId == R.id.search) {

            Toast.makeText(this, "search", Toast.LENGTH_SHORT).show();

        }

        if (itemId == R.id.action_linked_devices) {

            Toast.makeText(this, "action_linked_devices", Toast.LENGTH_SHORT).show();

        }

        if (itemId == R.id.action_new_broadcast) {

            Toast.makeText(this, "action_new_broadcast", Toast.LENGTH_SHORT).show();

        }

        if (itemId == R.id.action_starred_messages) {

            Toast.makeText(this, "action_starred_messages", Toast.LENGTH_SHORT).show();

        }

        if (itemId == R.id.action_payments) {

            Toast.makeText(this, "action_payments", Toast.LENGTH_SHORT).show();

        }

        if (itemId == R.id.action_new_group) {

            Toast.makeText(this, "action_new_group", Toast.LENGTH_SHORT).show();

        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onResume() {

        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        assert currentId != null;
        reference.child("presence").child(currentId).setValue("Online");

    }

    @Override
    protected void onPause() {

        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        assert currentId != null;
        reference.child("presence").child(currentId).setValue("Offline");

    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {

            if(data.getData() != null) {

                dialog.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                Date date = new Date();
                StorageReference reference = storage.getReference().child("status").child(date.getTime() + "");

                reference.putFile(data.getData()).addOnCompleteListener(task -> {

                    if(task.isSuccessful()) {

                        reference.getDownloadUrl().addOnSuccessListener(uri -> {

                            senderID = FirebaseAuth.getInstance().getUid();
                            FirebaseFirestore.getInstance().collection("Users").document(senderID).get().addOnCompleteListener(task1 -> {

                                if (task1.isSuccessful()) {

                                    DocumentSnapshot document = task1.getResult();
                                    if (document != null && document.exists()) {

                                        name = document.getString("userName");
                                        imageUrl = document.getString("imageProfile");

                                        UserStatus userStatus = new UserStatus();
                                        userStatus.setName(name);
                                        userStatus.setProfileImage(imageUrl);
                                        userStatus.setLastUpdated(date.getTime());

                                        HashMap<String, Object> obj = new HashMap<>();
                                        obj.put("name", userStatus.getName());
                                        obj.put("profileImage", userStatus.getProfileImage());
                                        obj.put("lastUpdated", userStatus.getLastUpdated());

                                        String imageUrl = uri.toString();
                                        Status status = new Status(imageUrl, userStatus.getLastUpdated());

                                        database.getReference().child("stories")
                                                .child(senderID)
                                                .updateChildren(obj);

                                        database.getReference().child("stories")
                                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                                .child("statuses")
                                                .push()
                                                .setValue(status);
                                        dialog.dismiss();

                                    }

                                }

                            });

                        });

                    }

                });

            }

        }

    }

}