package com.example.loginsmethods;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    TextView logout, dEmail, dPhNo, dId, dName;
    ImageView dImg;
    GoogleSignInOptions options;
    GoogleSignInClient googleSignInClient;
    FirebaseAuth auth;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logout = findViewById(R.id.logout);
        dEmail = findViewById(R.id.userMail);
        dName = findViewById(R.id.userName);
        dPhNo = findViewById(R.id.userPhNo);
        dImg = findViewById(R.id.userLogo);
        dId = findViewById(R.id.UserId);

        options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, options);

        auth = FirebaseAuth.getInstance();

        Glide.with(MainActivity.this)
                .load(Objects.requireNonNull(auth.getCurrentUser()).getPhotoUrl())
                .into(dImg);
        dName.setText(auth.getCurrentUser().getDisplayName());
        dId.setText("User Id: "+ auth.getCurrentUser().getUid());
        dEmail.setText("Email: "+ auth.getCurrentUser().getEmail());
        dPhNo.setText("Phone Number"+auth.getCurrentUser().getPhoneNumber());

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignInClient.signOut();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}