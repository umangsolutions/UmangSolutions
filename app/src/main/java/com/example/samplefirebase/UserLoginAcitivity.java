package com.example.samplefirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class UserLoginAcitivity extends AppCompatActivity {

    EditText edtMail,edtPassword;
    Button btnSignIn;
    ImageView btnBack;

    String email,password;

    // Firebase Code
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        edtMail = findViewById(R.id.editMail);
        edtPassword = findViewById(R.id.editPassword);

        btnBack = findViewById(R.id.back_btn);
        btnSignIn = findViewById(R.id.btnSignIn);

        mAuth = FirebaseAuth.getInstance();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserLoginAcitivity.this,MainActivity.class));
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = edtMail.getText().toString();
                password = edtPassword.getText().toString();

                if(email.isEmpty()) {
                    Toast.makeText(UserLoginAcitivity.this, "Please enter Email ID !", Toast.LENGTH_SHORT).show();
                } else if(password.isEmpty()) {
                    Toast.makeText(UserLoginAcitivity.this, "Please enter Password !", Toast.LENGTH_SHORT).show();
                } else {
                    next();
                }
            }
        });

    }

    private void next() {

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(UserLoginAcitivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(UserLoginAcitivity.this, "Successfully Logged In!", Toast.LENGTH_SHORT).show();


                } else {
                    Toast.makeText(UserLoginAcitivity.this, "Invalid Credentials !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}