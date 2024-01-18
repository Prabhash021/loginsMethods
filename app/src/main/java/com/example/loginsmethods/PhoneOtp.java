package com.example.loginsmethods;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class PhoneOtp extends AppCompatActivity {

    EditText phNo, gtOtp;
    Button sndOtpBtn, loginBtn;
    TextView reSndOtp;
    ProgressBar loading;
    FirebaseAuth mAuth;
    String phoneNumber;
    String verificationCode;
    PhoneAuthProvider.ForceResendingToken resendingToken;
    long timeOutSec = 60L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_otp);

        phNo = findViewById(R.id.PhNo);
        gtOtp = findViewById(R.id.Otp);
        sndOtpBtn = findViewById(R.id.sendOtp);
        loginBtn = findViewById(R.id.login);
        reSndOtp = findViewById(R.id.resendOtp);
        loading = findViewById(R.id.progresLod);

        mAuth = FirebaseAuth.getInstance();

        sndOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = phNo.getText().toString().trim();
                if(TextUtils.isEmpty(phoneNumber)){
                    toast("Enter the Phone Number");
                    return;
                }
                sendOtp(phoneNumber, false);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredOtp = gtOtp.getText().toString().trim();
                if(TextUtils.isEmpty(enteredOtp)){
                    toast("Enter the OTP");
                    return;
                }
                PhoneAuthCredential credential= PhoneAuthProvider.getCredential(verificationCode, enteredOtp);
                login(credential);
                setInProgress(true);
            }
        });

        reSndOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = phNo.getText().toString();
                sendOtp(phoneNumber, true);
            }
        });


    }

    void sendOtp(String phoneNumber, boolean isResend){
        startResendTimer();
        setInProgress(true);
        PhoneAuthOptions.Builder options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(timeOutSec, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                toast("Verification Completed");
                                setInProgress(false);
                                login(phoneAuthCredential);
                            }
                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                setInProgress(false);
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);
                                verificationCode = s;
                                resendingToken = forceResendingToken;

                                toast("OTP sent successfully");
                                setInProgress(false);
                            }
                        });         // OnVerificationStateChangedCallbacks

        if(isResend){
            PhoneAuthProvider.verifyPhoneNumber(options.setForceResendingToken(resendingToken).build());
        }else {
            PhoneAuthProvider.verifyPhoneNumber(options.build());
        }
    }

    private void login(PhoneAuthCredential phoneAuthCredential) {
        setInProgress(true);
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(PhoneOtp.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    toast("OTP verification failed");
                    setInProgress(false);
                }
            }
        });

    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    void setInProgress(boolean inProgress){
        if(inProgress){
            loading.setVisibility(View.VISIBLE);
            loginBtn.setVisibility(View.GONE);
        }else {
            loading.setVisibility(View.GONE);
            loginBtn.setVisibility(View.VISIBLE);
        }
    }

    private void startResendTimer() {
        reSndOtp.setEnabled(false);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeOutSec--;
                reSndOtp.setText("Resend otp in "+timeOutSec+" seconds");
                if(timeOutSec<=0){
                    timeOutSec =60L;
                    timer.cancel();
                    runOnUiThread(() -> {
                        reSndOtp.setEnabled(true);
                    });
                }
            }
        },0,1000);
    }
}