package com.example.samplefirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.samplefirebase.modals.UserData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
/*
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;

import org.tensorflow.lite.Interpreter;
*/

import java.io.File;
import java.util.Objects;

public class WelcomeActivity extends AppCompatActivity {


    // Model Name: PostureDetectionModel


    TextView txtName,txtEmail,txtRoll,txtPhone;

    DatabaseReference myRef;

    String  userName,rollNo,phone;

    Button btnFaceDetection,btnFaceRegistration;

    // Tensorflow Interpreter
    //Interpreter interpreter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        final String email = getIntent().getStringExtra("email");

        txtEmail = findViewById(R.id.email);
        txtName = findViewById(R.id.name);
        txtRoll = findViewById(R.id.roll);
        txtPhone = findViewById(R.id.phone);

        btnFaceDetection = findViewById(R.id.faceDetection);
        btnFaceRegistration = findViewById(R.id.btnFaceRegistration);

        btnFaceRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this, RegisterFaceActivity.class);
                startActivity(intent);
            }
        });


        myRef = FirebaseDatabase.getInstance().getReference("UserDetails");

        final Query query = myRef.orderByChild("emailID").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {

                    for(DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                        userName = Objects.requireNonNull(dataSnapshot1.getValue(UserData.class)).getName();
                        rollNo = Objects.requireNonNull(dataSnapshot1.getValue(UserData.class)).getRollNumber();
                        phone = Objects.requireNonNull(dataSnapshot1.getValue(UserData.class)).getPhone();
                    }


                    txtEmail.setText(email);
                    txtName.setText(userName);
                    txtRoll.setText(rollNo);
                    txtPhone.setText(phone);

                } else {
                  //  Toast.makeText(WelcomeActivity.this, "No Data Found !", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WelcomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        btnFaceDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this,FaceDetectionActivity.class);
                startActivity(intent);
            }
        });


        /*FirebaseCustomRemoteModel remoteModel =
                new FirebaseCustomRemoteModel.Builder("PostureDetectionModel").build();
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        // Download complete. Depending on your app, you could enable
                        // the ML feature, or switch from the local model to the remote
                        // model, etc.

                        Toast.makeText(WelcomeActivity.this, "Model Downloaded Successfully !", Toast.LENGTH_SHORT).show();

                        initializeModel();

                    }
                });
   */ }




    /*public void initializeModel() {
        // Model downloaded Successfully, Now we need to Initialize it.

        FirebaseCustomRemoteModel remoteModel = new FirebaseCustomRemoteModel.Builder("PostureDetectionModel").build();
        FirebaseModelManager.getInstance().getLatestModelFile(remoteModel)
                .addOnCompleteListener(new OnCompleteListener<File>() {
                    @Override
                    public void onComplete(@NonNull Task<File> task) {
                        File modelFile = task.getResult();
                        if (modelFile != null) {
                            interpreter = new Interpreter(modelFile);
                        }
                    }
                });

    }*/
}