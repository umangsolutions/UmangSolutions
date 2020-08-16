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

public class UserRegistrationActivity extends AppCompatActivity {

    ImageView imgBackBtn;
    EditText edtName,edtRollNo,edtMail,edtPass;
    Button btnSignUp;
    String name,rollNo,mailID,password;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        imgBackBtn = findViewById(R.id.back_btn);

        mAuth = FirebaseAuth.getInstance();

        edtName = findViewById(R.id.editName);
        edtMail = findViewById(R.id.editMail);
        edtRollNo = findViewById(R.id.editRollNumber);
        edtPass = findViewById(R.id.editPassword);

        btnSignUp = findViewById(R.id.btnCreateAccount);

        imgBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserRegistrationActivity.this,MainActivity.class));
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = edtName.getText().toString();
                mailID = edtMail.getText().toString();
                rollNo = edtRollNo.getText().toString();
                password = edtPass.getText().toString();

                if(name.isEmpty()) {
                    Toast.makeText(UserRegistrationActivity.this, "Please enter Name !", Toast.LENGTH_SHORT).show();
                } else if(rollNo.isEmpty()) {
                    Toast.makeText(UserRegistrationActivity.this, "Please enter Roll Number !", Toast.LENGTH_SHORT).show();
                } else if(mailID.isEmpty()) {
                    Toast.makeText(UserRegistrationActivity.this, "Please enter Mail ID !", Toast.LENGTH_SHORT).show();
                } else if(password.isEmpty()) {
                    Toast.makeText(UserRegistrationActivity.this, "Please enter Password !", Toast.LENGTH_SHORT).show();
                } else {
                    next();
                }
            }
        });

    }

    private void next() {

        mAuth.createUserWithEmailAndPassword(mailID,password).addOnCompleteListener(UserRegistrationActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(UserRegistrationActivity.this, "User Registered Successfully !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UserRegistrationActivity.this,MainActivity.class));
                } else {
                    Toast.makeText(UserRegistrationActivity.this, "Something Wrong Occurred !", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}