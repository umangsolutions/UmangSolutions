package com.example.samplefirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.samplefirebase.adapters.FaceBitmapAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FaceDetectionActivity extends AppCompatActivity {

    Button btnSelectImage;

    private Uri filePath;

    List<Rect> rectangles;

    ImageView newImageView;

    InputImage image;

    List<Bitmap> facesBitmap;

    FaceBitmapAdapter faceBitmapAdapter;
    RecyclerView facesListRecylcer;

    Bitmap bitmapImage,scaledBitmap;

    TextView txtNumber,tsmileProb,trightEyeOpen;

    DrawView drawView;

    private final int PICK_IMAGE_REQUEST = 22;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);

        btnSelectImage = findViewById(R.id.selectImage);

        facesBitmap = new ArrayList<>();


        rectangles = new ArrayList<>();

        facesListRecylcer = findViewById(R.id.facesList);

        newImageView = findViewById(R.id.newImageView);

        drawView = findViewById(R.id.imageView);

        txtNumber = findViewById(R.id.noOfFaces);
        tsmileProb = findViewById(R.id.smileProb);
        trightEyeOpen = findViewById(R.id.rightEyeOpen);


        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

    }

    public void selectImage() {
        // Defining Implicit Intent to mobile gallery

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);

    }



    public void faceDetectorOptions() {
        // High-accuracy landmark detection and face classification
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();

// Real-time contour detection
        FaceDetectorOptions realTimeOpts =
                new FaceDetectorOptions.Builder()
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .build();
    }

    public void detectFaces(InputImage image) {



        // Setting the Face Detector Options
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .setMinFaceSize(0f)
                        .enableTracking()
                        .build();

        // Creating an instance
        FaceDetector detector = FaceDetection.getClient(options);

        // For drawing Rectangle



        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        // Task completed successfully
                                        // ...

                                        rectangles.clear();

                                        facesBitmap.clear();
                                        for (Face face : faces) {

                                            Rect bounds = face.getBoundingBox();

                                            // adding dimensions of Rectangles to Arraylist
                                            rectangles.add(bounds);

                                            Bitmap newBitmap = Bitmap.createBitmap(scaledBitmap,bounds.left,bounds.top,bounds.width(),bounds.height());

                                            // adding the Bitmap of faces to the facesBitmap list
                                            facesBitmap.add(newBitmap);

                                            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                            // nose available):
                                            FaceLandmark leftEar = face.getLandmark(FaceLandmark.LEFT_EAR);
                                            if (leftEar != null) {
                                                PointF leftEarPos = leftEar.getPosition();
                                            }

                                            // If classification was enabled:
                                            if (face.getSmilingProbability() != null) {
                                                float smileProb = face.getSmilingProbability();


                                                tsmileProb.setText("Smile Probability: " + Float.toString(smileProb));


                                            }
                                            if (face.getRightEyeOpenProbability() != null) {
                                                float rightEyeOpenProb = face.getRightEyeOpenProbability();

                                                trightEyeOpen.setText("Right Eye Open: " + Float.toString(rightEyeOpenProb));
                                            }



                                            // If face tracking was enabled:
                                            if (face.getTrackingId() != null) {
                                                int id = face.getTrackingId();
                                            }
                                        }


                                        drawView.createRectangles(rectangles);
                                        drawView.setImageBitmap(scaledBitmap);

                                        Toast.makeText(FaceDetectionActivity.this, "Completed !", Toast.LENGTH_SHORT).show();

                                        //Toast.makeText(FaceDetectionActivity.this, ""+ facesBitmap.size(), Toast.LENGTH_SHORT).show();

                                        // Initialising the Recycler Adapter
                                       // facesListRecylcer.setHasFixedSize(true);
                                        //facesListRecylcer.setLayoutManager(new LinearLayoutManager(FaceDetectionActivity.this, LinearLayoutManager.VERTICAL,false));

                                        faceBitmapAdapter = new FaceBitmapAdapter(FaceDetectionActivity.this,facesBitmap);
                                        facesListRecylcer.setAdapter(faceBitmapAdapter);

                                       // faceBitmapAdapter.notifyDataSetChanged();



/*
                                        drawView.buildDrawingCache();
                                        Bitmap newBitmap = drawView.getDrawingCache();
                                        newBitmap = Bitmap.createScaledBitmap(newBitmap,drawView.getWidth(),drawView.getHeight(),true);*/

                                        newImageView.setImageBitmap(facesBitmap.get(0));


                                        txtNumber.setText("No. of Faces Detected: " + Integer.toString(faces.size()));

                                       // Toast.makeText(FaceDetectionActivity.this, ""+faces.size(), Toast.LENGTH_SHORT).show();

                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...


                                        Toast.makeText(FaceDetectionActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

    }



    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {

               //image = InputImage.fromFilePath(getApplicationContext(), filePath);

                bitmapImage = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);

                int nh = (int) ( bitmapImage.getHeight() * (512.0 / bitmapImage.getWidth()) );
                scaledBitmap = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true);


                image = InputImage.fromBitmap(scaledBitmap,0);

                detectFaces(image);

                //Toast.makeText(this, "Image Selected Successfully !", Toast.LENGTH_SHORT).show();

                // Setting image on image view using Bitmap
               /* Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);


                //imageView.setImageBitmap(bitmap);
*/
            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }
}