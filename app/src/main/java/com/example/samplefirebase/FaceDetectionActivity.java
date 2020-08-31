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
import android.graphics.Point;
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
import com.example.samplefirebase.modals.FaceLandmarkData;
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

    List<FaceLandmarkData> facesLandmarkDataList;

    FaceBitmapAdapter faceBitmapAdapter;
    RecyclerView facesListRecylcer;

    Bitmap bitmapImage,scaledBitmap, borderedBitmap;

    TextView txtNumber,tsmileProb,trightEyeOpen, txtLandMarksData;

    DrawView drawView;

    private final int PICK_IMAGE_REQUEST = 22;

    PointF leftEarPos,rightEarPos, leftEyePos, rightEyePos, leftMouthPos, rightMouthPos, noseBasePos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);

        btnSelectImage = findViewById(R.id.selectImage);

        facesLandmarkDataList = new ArrayList<>();


        rectangles = new ArrayList<>();

        txtLandMarksData = findViewById(R.id.landmarksData);

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

                                        facesLandmarkDataList.clear();
                                        for (Face face : faces) {

                                            Rect bounds = face.getBoundingBox();

                                            // adding dimensions of Rectangles to Array List
                                            rectangles.add(bounds);

                                            // cutting the Faces using the Co-ordinates of Rect from Master Image
                                           Bitmap faceDetectedBitmap = Bitmap.createBitmap(scaledBitmap,face.getBoundingBox().left,face.getBoundingBox().top,face.getBoundingBox().width(),face.getBoundingBox().height());

                                            // adding the Bitmap of faces to the facesBitmap list


                                            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                            // nose available):

                                            FaceLandmark leftEar = face.getLandmark(FaceLandmark.LEFT_EAR);
                                            if (leftEar != null) {
                                                leftEarPos = leftEar.getPosition();
                                            }

                                            FaceLandmark rightEar = face.getLandmark(FaceLandmark.RIGHT_EAR);
                                            if(rightEar !=null) {
                                                rightEarPos =rightEar.getPosition();
                                            }

                                            FaceLandmark leftEyeCorner = face.getLandmark(FaceLandmark.LEFT_EYE);
                                            if(leftEyeCorner !=null) {
                                                leftEyePos = leftEyeCorner.getPosition();
                                            }

                                            FaceLandmark rightEyeCorner = face.getLandmark(FaceLandmark.RIGHT_EYE);
                                            if(rightEyeCorner!=null) {
                                                rightEyePos = rightEyeCorner.getPosition();
                                            }

                                            FaceLandmark leftMouth = face.getLandmark(FaceLandmark.MOUTH_LEFT);
                                            if(leftMouth !=null) {
                                                leftMouthPos = leftMouth.getPosition();
                                            }

                                            FaceLandmark rightMouth = face.getLandmark(FaceLandmark.MOUTH_RIGHT);
                                            if(rightMouth !=null) {
                                                rightMouthPos = rightMouth.getPosition();
                                            }

                                            FaceLandmark noseBase = face.getLandmark(FaceLandmark.NOSE_BASE);
                                            if(noseBase !=null) {
                                                noseBasePos = noseBase.getPosition();
                                            }

                                            //adding all the Faces Landmark Data to the FacesLandmarkData Array list
                                            facesLandmarkDataList.add(new FaceLandmarkData(faceDetectedBitmap, leftEarPos, rightEarPos, leftEyePos, rightEyePos,leftMouthPos,rightMouthPos,noseBasePos));


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



                                        //Toast.makeText(FaceDetectionActivity.this, "Completed !", Toast.LENGTH_SHORT).show();

                                        //Toast.makeText(FaceDetectionActivity.this, ""+ facesBitmap.size(), Toast.LENGTH_SHORT).show();


                                        Toast.makeText(FaceDetectionActivity.this, ""+ facesLandmarkDataList.size(), Toast.LENGTH_SHORT).show();



/*
                                        drawView.buildDrawingCache();
                                        Bitmap newBitmap = drawView.getDrawingCache();
                                        newBitmap = Bitmap.createScaledBitmap(newBitmap,drawView.getWidth(),drawView.getHeight(),true);*/

                                       if(facesLandmarkDataList != null) {
                                           newImageView.setImageBitmap(facesLandmarkDataList.get(0).getImageBitmap());
                                           txtLandMarksData.setText("LeftEarPos" + facesLandmarkDataList.get(0).getLeftEar().toString() + "\n" + "RightEarPos" + facesLandmarkDataList.get(0).getRightEar().toString() + "\n" +  "LeftEyePos" + facesLandmarkDataList.get(0).getLeftEye().toString() + "\n" +  "RightEyePos" + facesLandmarkDataList.get(0).getRightEye().toString() + "\n" +  "LeftMouthPos" + facesLandmarkDataList.get(0).getLeftMouth().toString() + "\n" +  "RightMouthPos" + facesLandmarkDataList.get(0).getRightMouth().toString() + "\n" +  "NosePos" + facesLandmarkDataList.get(0).getNoseBase());
                                       }


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

                // adding Border of 2 units to the Image received from the User's Gallery
                borderedBitmap = addWhiteBorder(bitmapImage,5);


                int nh = (int) ( borderedBitmap.getHeight() * (512.0 / borderedBitmap.getWidth()) );
                scaledBitmap = Bitmap.createScaledBitmap(borderedBitmap, 512, nh, true);

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

    private Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

}