package com.basheer.whatsappclone.activities.chat;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.basheer.whatsappclone.adapter.ContactsAdapter;
import com.basheer.whatsappclone.databinding.ActivityContactsBinding;
import com.basheer.whatsappclone.model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    ActivityContactsBinding binding;
    List<Users> list = new ArrayList<>();
    ContactsAdapter adapter;
    FirebaseUser firebaseUser;
    FirebaseFirestore firestore;
    public static final int REQUEST_READ_CONTACTS = 79;
    ArrayList mobileArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if(firebaseUser != null) { getContactsFromPhone(); }

        if (mobileArray != null) {

            getContactList();

//            for (int i=0; i<mobileArray.size();i++) {
//                Log.d(TAG, "contacts : " + mobileArray.get(i).toString());
//            }

        }

        binding.backButton.setOnClickListener(v -> finish());

    }

    private void getContactsFromPhone() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            mobileArray = getAllPhoneContacts();
        } else {
            requestPermission();
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) { }
        else {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);

        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) { }
        else {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { mobileArray = getAllPhoneContacts(); } else { finish(); }

        }

    }

    @SuppressLint("Range")
    private ArrayList getAllPhoneContacts() {

        ArrayList<String> phoneList = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0) {

            while (cur.moveToNext()) {

                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                if (cur.getInt(cur.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {

                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);

                    while (true) {

                        assert pCur != null;
                        if (!pCur.moveToNext()) break;
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneList.add(removeWhiteSpace(phoneNo));

                    }

                    pCur.close();
                }

            }

        }

        if (cur != null) { cur.close(); }

        return phoneList;
    }


    private void getContactList() {

        firestore.collection("Users").get().addOnSuccessListener(queryDocumentSnapshots -> {

            for (QueryDocumentSnapshot snapshots : queryDocumentSnapshots) {

                String userID = snapshots.getString("userID");
                String userName = snapshots.getString("userName");
                String imageUrl = snapshots.getString("imageProfile");
                String desc = snapshots.getString("bio");
                String phone = snapshots.getString("userPhone");

                Users user = new Users();

                user.setUserID(userID);
                user.setBio(desc);
                user.setUserName(userName);
                user.setImageProfile(imageUrl);
                user.setUserPhone(phone);

                assert userID != null;
                if(!userID.equals(firebaseUser.getUid())) {

                    if (mobileArray.contains(removeFirstThreeChars(user.getUserPhone()))) {

                        list.add(user);

                    }

                }

            }

            adapter = new ContactsAdapter(list, ContactsActivity.this);
            binding.recyclerView.setAdapter(adapter);

        });

    }

    public String removeWhiteSpace(String number){
        return number.replaceAll("\\s", "");
    }

    public static String removeFirstThreeChars(String originalString) { return originalString.substring(3); }

}