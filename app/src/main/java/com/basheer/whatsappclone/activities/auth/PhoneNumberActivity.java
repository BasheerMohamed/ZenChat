package com.basheer.whatsappclone.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.basheer.whatsappclone.databinding.ActivityPhoneNumberBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.hbb20.CountryCodePicker;

public class PhoneNumberActivity extends AppCompatActivity {

    ActivityPhoneNumberBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get an instance of FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Ensure the CountryCodePicker (ccp) is correctly bound
        CountryCodePicker ccp =  binding.cpp;

        // Request focus for the mobile number input
        binding.mobileNumber.requestFocus();

        // Set a click listener on the generate OTP button
        binding.generateOTP.setOnClickListener(v -> {

            // Get the phone number from the input field
            String phoneNumber = binding.mobileNumber.getText().toString().trim();

            // Check if the phone number is not empty
            if (!phoneNumber.isEmpty()) {

                // Get the selected country code from ccp
                String code = ccp.getSelectedCountryCode();
                String number = "+" + code + phoneNumber;

                // Create an intent for the OTPActivity
                Intent intent = new Intent(PhoneNumberActivity.this, OTPActivity.class);
                intent.putExtra("phoneNumber", number);
                startActivity(intent);

            } else {

                // Show an error message if the phone number input is empty
                binding.mobileNumber.setError("Please enter your phone number");
                binding.mobileNumber.requestFocus();

            }

        });

    }

}