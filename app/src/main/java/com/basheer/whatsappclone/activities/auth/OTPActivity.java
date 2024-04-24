package com.basheer.whatsappclone.activities.auth;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.basheer.whatsappclone.activities.profile.SetupProfileActivity;
import com.basheer.whatsappclone.databinding.ActivityOtpactivityBinding;
import com.basheer.whatsappclone.model.Users;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    ActivityOtpactivityBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    String verificationId;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityOtpactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending OTP...");
        dialog.setCancelable(false);
        dialog.show();

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        String phoneNumber = getIntent().getStringExtra("phoneNumber");

        if (phoneNumber == null) {
            Toast.makeText(this, "Phone number is required.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity or handle accordingly
            return;
        }

        binding.mobileNumberText.setText(phoneNumber);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(30L, TimeUnit.SECONDS)
                .setActivity(OTPActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                        Toast.makeText(OTPActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        dialog.dismiss();

                    }

                    @Override
                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                        super.onCodeSent(verifyId, forceResendingToken);
                        dialog.dismiss();
                        verificationId = verifyId;

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        binding.otpview.requestFocus();

                    }

                }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);

        binding.resendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(30L, TimeUnit.SECONDS)
                        .setActivity(OTPActivity.this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {

                                Toast.makeText(OTPActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                dialog.dismiss();

                            }

                            @Override
                            public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                                super.onCodeSent(verifyId, forceResendingToken);
                                verificationId = verifyId;

                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                binding.otpview.requestFocus();

                            }

                        }).build();

                PhoneAuthProvider.verifyPhoneNumber(options);
            }

        });

        binding.otpview.setOtpCompletionListener(otp -> {

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

            auth.signInWithCredential(credential).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {

                    FirebaseUser firebaseUser = task.getResult().getUser();

                    assert firebaseUser != null;
                    Users users = new Users(
                            firebaseUser.getUid(),
                            "",
                            phoneNumber,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                    );

                    firestore.collection("Users").document(firebaseUser.getUid()).set(users).addOnSuccessListener(unused -> {

                        Intent intent = new Intent(OTPActivity.this, SetupProfileActivity.class);
                        startActivity(intent);
                        finishAffinity();

                    });

                }

            });

        });

    }

}