package com.example.samplefirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
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
import com.example.samplefirebase.modals.IndividualFaceData;
import com.example.samplefirebase.modals.RegisterFaceData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class FaceDetectionActivity extends AppCompatActivity {

    Button btnSelectImage;

    private Uri filePath;

    List<Rect> rectangles;

    List<Float> fetchedDistances;

    ImageView newImageView;


    InputImage image, filteredImage;

    Button btnUploadFaces, btnRecognizeFace;

    float d1_val, d2_val, d3_val, d4_val, d5_val;

    List<FaceLandmarkData> facesLandmarkDataList, derivedFaceLandmarksList;

    DatabaseReference myRef;

    RecyclerView facesDisplayList;

    FaceBitmapAdapter faceBitmapAdapter;
    RecyclerView facesListRecylcer;

    Bitmap bitmapImage,scaledBitmap, borderedBitmap;

    TextView txtNumber,tsmileProb,trightEyeOpen, txtLandMarksData;

    DrawView drawView;

    private final int PICK_IMAGE_REQUEST = 22;

    List<RegisterFaceData> registerFaceDataList;

    StorageReference storageReference;

    FaceBitmapAdapter mFacesAdapter;

    PointF leftEarPosition,rightEarPosition, leftEyePosition, rightEyePosition, leftMouthPosition, rightMouthPosition, noseBasePosition,leftEarPos, rightEarPos,leftEyePos,rightEyePos,leftMouthPos,rightMouthPos,noseBasePos;

    float recognizedAvg, calculatedAvg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);

        btnSelectImage = findViewById(R.id.selectImage);

        // Recycler View
        facesDisplayList = findViewById(R.id.facesDisplayList);

        facesLandmarkDataList = new ArrayList<>();
        registerFaceDataList = new ArrayList<>();
        fetchedDistances = new ArrayList<>();

        //btnUploadFaces = findViewById(R.id.uploadFaces);
        btnRecognizeFace = findViewById(R.id.recognizeFaces);

        // creating Instances for Storage and Reference
        storageReference = FirebaseStorage.getInstance().getReference();

        myRef = FirebaseDatabase.getInstance().getReference().child("Faces_Data");


        rectangles = new ArrayList<>();

        txtLandMarksData = findViewById(R.id.landmarksData);

        newImageView = findViewById(R.id.newImageView);

        drawView = findViewById(R.id.imageView);

        derivedFaceLandmarksList = new ArrayList<>();


        txtNumber = findViewById(R.id.noOfFaces);


        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

      /*  btnUploadFaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImages();
            }
        });
*/
        btnRecognizeFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recognizeFaces();
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
        final FaceDetectorOptions options =
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

                                           Bitmap convertedBitmap = Bitmap.createScaledBitmap(faceDetectedBitmap,112,112,true);
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
                                           facesLandmarkDataList.add(new FaceLandmarkData(convertedBitmap, leftEarPos, rightEarPos, leftEyePos, rightEyePos,leftMouthPos,rightMouthPos,noseBasePos, ""));


                                            // If classification was enabled:
                                            if (face.getSmilingProbability() != null) {
                                                float smileProb = face.getSmilingProbability();


                                               // tsmileProb.setText("Smile Probability: " + Float.toString(smileProb));


                                            }
                                            if (face.getRightEyeOpenProbability() != null) {
                                                float rightEyeOpenProb = face.getRightEyeOpenProbability();

                                                // trightEyeOpen.setText("Right Eye Open: " + Float.toString(rightEyeOpenProb));
                                            }


                                            // If face tracking was enabled:
                                            if (face.getTrackingId() != null) {
                                                int id = face.getTrackingId();
                                            }

                                        }


                                        //drawView.createRectangles(rectangles);
                                        drawView.setImageBitmap(scaledBitmap);



                                        //Toast.makeText(FaceDetectionActivity.this, "Completed !", Toast.LENGTH_SHORT).show();

                                        //Toast.makeText(FaceDetectionActivity.this, ""+ facesBitmap.size(), Toast.LENGTH_SHORT).show();


                                        //Toast.makeText(FaceDetectionActivity.this, ""+ facesLandmarkDataList.size(), Toast.LENGTH_SHORT).show();



/*
                                        drawView.buildDrawingCache();
                                        Bitmap newBitmap = drawView.getDrawingCache();
                                        newBitmap = Bitmap.createScaledBitmap(newBitmap,drawView.getWidth(),drawView.getHeight(),true);*/

                                        if(facesLandmarkDataList !=null) {

                                            mFacesAdapter = new FaceBitmapAdapter(FaceDetectionActivity.this, facesLandmarkDataList);
                                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FaceDetectionActivity.this, LinearLayoutManager.HORIZONTAL, false);
                                            facesDisplayList.setLayoutManager(linearLayoutManager);
                                            facesDisplayList.setAdapter(mFacesAdapter);

                                        }


/*
                                       if(facesLandmarkDataList != null) {
                                           final Bitmap foundBitmap = Bitmap.createScaledBitmap(facesLandmarkDataList.get(0).getImageBitmap(),112,112,true);
                                           //newImageView.setImageBitmap(foundBitmap);

                                           filteredImage = InputImage.fromBitmap(foundBitmap,0);

                                           FaceDetector faceDetector = FaceDetection.getClient(options);

                                           Task<List<Face>> result = faceDetector.process(filteredImage).addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                                               @Override
                                               public void onSuccess(List<Face> faces) {

                                                   derivedFaceLandmarksList.clear();
                                                   for (Face face : faces) {

                                                       FaceLandmark leftEar = face.getLandmark(FaceLandmark.LEFT_EAR);
                                                       if (leftEar != null) {
                                                           leftEarPosition = leftEar.getPosition();
                                                       }

                                                       FaceLandmark rightEar = face.getLandmark(FaceLandmark.RIGHT_EAR);
                                                       if(rightEar !=null) {
                                                           rightEarPosition =rightEar.getPosition();
                                                       }

                                                       FaceLandmark leftEyeCorner = face.getLandmark(FaceLandmark.LEFT_EYE);
                                                       if(leftEyeCorner !=null) {
                                                           leftEyePosition = leftEyeCorner.getPosition();
                                                       }

                                                       FaceLandmark rightEyeCorner = face.getLandmark(FaceLandmark.RIGHT_EYE);
                                                       if(rightEyeCorner!=null) {
                                                           rightEyePosition = rightEyeCorner.getPosition();
                                                       }

                                                       FaceLandmark leftMouth = face.getLandmark(FaceLandmark.MOUTH_LEFT);
                                                       if(leftMouth !=null) {
                                                           leftMouthPosition = leftMouth.getPosition();
                                                       }

                                                       FaceLandmark rightMouth = face.getLandmark(FaceLandmark.MOUTH_RIGHT);
                                                       if(rightMouth !=null) {
                                                           rightMouthPosition = rightMouth.getPosition();
                                                       }

                                                       FaceLandmark noseBase = face.getLandmark(FaceLandmark.NOSE_BASE);
                                                       if(noseBase !=null) {
                                                           noseBasePosition = noseBase.getPosition();
                                                       }


                                                       derivedFaceLandmarksList.add(new FaceLandmarkData(foundBitmap, leftEarPosition, rightEarPosition, leftEyePosition, rightEyePosition,leftMouthPosition,rightMouthPosition,noseBasePosition, ""));
                                                   }


                                               }
                                           }).addOnFailureListener(new OnFailureListener() {
                                               @Override
                                               public void onFailure(@NonNull Exception e) {
                                                   Toast.makeText(FaceDetectionActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                               }
                                           });


                                        //   txtLandMarksData.setText("LeftEarPos" + facesLandmarkDataList.get(0).getLeftEar().toString() + "\n" + "RightEarPos" + facesLandmarkDataList.get(0).getRightEar().toString() + "\n" +  "LeftEyePos" + facesLandmarkDataList.get(0).getLeftEye().toString() + "\n" +  "RightEyePos" + facesLandmarkDataList.get(0).getRightEye().toString() + "\n" +  "LeftMouthPos" + facesLandmarkDataList.get(0).getLeftMouth().toString() + "\n" +  "RightMouthPos" + facesLandmarkDataList.get(0).getRightMouth().toString() + "\n" +  "NosePos" + facesLandmarkDataList.get(0).getNoseBase());
                                       }
*/

                                       String X_Coordinate =  getX_Coordinate(facesLandmarkDataList.get(0).getLeftEye().toString().replaceAll("^(PointF)",""));

                                        String Y_Coordinate =  getY_Coordinate(facesLandmarkDataList.get(0).getLeftEye().toString().replaceAll("^(PointF)",""));


                                        txtNumber.setText("Number of Faces Detected: " + Integer.toString(faces.size()));

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


    private void recognizeFaces() {


            // Calculating Distances for the Faces Detected in the Image

            String leftEye = derivedFaceLandmarksList.get(0).getLeftEye().toString().replaceAll("^(PointF)", "");
            float leftEyePosX = Float.parseFloat(getX_Coordinate(leftEye));
            float leftEyePosY = Float.parseFloat(getY_Coordinate(leftEye));


            String rightEye = derivedFaceLandmarksList.get(0).getRightEye().toString().replaceAll("^(PointF)", "");
            float rightEyePosX = Float.parseFloat(getX_Coordinate(rightEye));
            float rightEyePosY = Float.parseFloat(getY_Coordinate(rightEye));


            String noseBase = derivedFaceLandmarksList.get(0).getNoseBase().toString().replaceAll("^(PointF)", "");
            float noseBasePosX = Float.parseFloat(getX_Coordinate(noseBase));
            float noseBasePosY = Float.parseFloat(getY_Coordinate(noseBase));


            String leftMouth = derivedFaceLandmarksList.get(0).getLeftMouth().toString().replaceAll("^(PointF)", "");
            float leftMouthPosX = Float.parseFloat(getX_Coordinate(leftMouth));
            float leftMouthPosY = Float.parseFloat(getY_Coordinate(leftMouth));

            String rightMouth = derivedFaceLandmarksList.get(0).getRightMouth().toString().replaceAll("^(PointF)", "");
            float rightMouthPosX = Float.parseFloat(getX_Coordinate(rightMouth));
            float rightMouthPosY = Float.parseFloat(getY_Coordinate(rightMouth));

            // d1 means Distance between Left Eye and Right Eye
            d1_val = calculateEuclideanDistance(leftEyePosX, leftEyePosY, rightEyePosX, rightEyePosY);

            // d2 means Distance between NoseBase and LeftEye
            d2_val = calculateEuclideanDistance(noseBasePosX, noseBasePosY, leftEyePosX, leftEyePosY);

            // d3 means Distance between NoseBase and RightEye
            d3_val = calculateEuclideanDistance(noseBasePosX, noseBasePosY, rightEyePosX, rightEyePosY);

            // d4 represents Distance between NoseBase and Left Mouth
            d4_val = calculateEuclideanDistance(noseBasePosX, noseBasePosY, leftMouthPosX, leftMouthPosY);

            // d5 represents Distance between NoseBase and Right Mouth
            d5_val = calculateEuclideanDistance(noseBasePosX,noseBasePosY,rightMouthPosX, rightMouthPosY);



            txtLandMarksData.setText("d1: " + d1_val + "\nd2: " + d2_val + "\nd3: " + d3_val + "\nd4: " + d4_val + "\nd5: " + d5_val );


            // Retrieving Data of Faces available in Firebase
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                        RegisterFaceData registerFaceData = dataSnapshot1.getValue(RegisterFaceData.class);
                        registerFaceDataList.add(registerFaceData);
                    }

                    int count = 0;

                    for(int i=0; i<registerFaceDataList.size(); i++) {

                        count = count + 1;
                        // d1 means Distance between Left Eye and Right Eye
                        float D1_VAL = registerFaceDataList.get(i).getD1_val();

                        // d2 means Distance between NoseBase and LeftEye
                        float D2_VAL = registerFaceDataList.get(i).getD2_val();

                        // d3 means Distance between NoseBase and RightEye
                        float D3_VAL = registerFaceDataList.get(i).getD3_val();

                        // d4 represents Distance between NoseBase and Left Mouth
                        float D4_VAL = registerFaceDataList.get(i).getD4_val();

                        // d5 represents Distance between NoseBase and Right Mouth
                        float D5_VAL = registerFaceDataList.get(i).getD5_val();

                       // Toast.makeText(FaceDetectionActivity.this, ""+registerFaceDataList.get(i).getName(), Toast.LENGTH_SHORT).show();

                      if( Math.abs(D1_VAL - d1_val)<=11.0 && Math.abs(D2_VAL-d2_val)<=11.0 && Math.abs(D3_VAL-d3_val)<=11.0 && Math.abs(D4_VAL-d4_val)<=11.0 && Math.abs(D5_VAL-d5_val)<=11.0) {
                            // Face matched with Some person
                            Toast.makeText(FaceDetectionActivity.this, "Hello, "+ registerFaceDataList.get(i).getName(), Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }

                    if(count == registerFaceDataList.size()) {
                        Toast.makeText(FaceDetectionActivity.this, "No matched Faces Found !!!", Toast.LENGTH_SHORT).show();
                    }



                   //Toast.makeText(FaceDetectionActivity.this, "Calculated Avg: " + calculatedAvg + "\n Recognized Avg:" + recognizedAvg, Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                    Toast.makeText(FaceDetectionActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private float calculateEuclideanDistance(float x1, float y1, float x2, float y2) {
        double d = Math.sqrt(Math.abs((double) Math.pow(Math.abs(x2-x1),2) + (double) Math.pow(Math.abs(y2-y1),2)));
        return (float)d;
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

    private void uploadImages() {
        if(facesLandmarkDataList!=null) {

            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading Data...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();




            // adding listeners on upload
            // or failure of image

            for(int i=0; i<facesLandmarkDataList.size(); i++) {

                Bitmap individualFaceBitmap = facesLandmarkDataList.get(i).getImageBitmap();

                Date date = new Date();
                String datetime = date.toString();

                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Faces_Detected").child(datetime);


                // Defining the child of storageReference
                final StorageReference ref = storageReference.child("Images/" + datetime + "/"+ UUID.randomUUID().toString());

                // converting the obtained Bitmap to Bytes to send to Firebase

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                individualFaceBitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
                final byte[] data = byteArrayOutputStream.toByteArray();

                final int finalI = i;
                ref.putBytes(data)
                        .addOnSuccessListener(
                                new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        // Image uploaded successfully
                                        // Dismiss dialog
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful()) ;

                                        // getting the URL of image uploaded to
                                        Uri downloadUrl = uriTask.getResult();

                                        // sending Data to Realtime Database
                                        String key = reference.push().getKey();
                                        assert downloadUrl != null;
                                        IndividualFaceData faceLandmarkData = new IndividualFaceData(facesLandmarkDataList.get(finalI).getLeftEar().toString(),facesLandmarkDataList.get(finalI).getRightEar().toString(),facesLandmarkDataList.get(finalI).getLeftEye().toString(),facesLandmarkDataList.get(finalI).getRightEye().toString(), facesLandmarkDataList.get(finalI).getLeftMouth().toString(), facesLandmarkDataList.get(finalI).getRightMouth().toString(),facesLandmarkDataList.get(finalI).getNoseBase().toString(), downloadUrl.toString(),"Saikiran Kopparthi");
                                        reference.child(key).setValue(faceLandmarkData);

                                        progressDialog.dismiss();
                                        Toast.makeText(FaceDetectionActivity.this, "Faces Uploaded Successfully !", Toast.LENGTH_SHORT).show();

                                    }
                                })

                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                // Error, Image not uploaded
                                progressDialog.dismiss();
                                Toast.makeText(FaceDetectionActivity.this,
                                        "Failed " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(
                                new OnProgressListener<UploadTask.TaskSnapshot>() {

                                    // Progress Listener for loading
                                    // percentage on the dialog box
                                    @Override
                                    public void onProgress(
                                            UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress
                                                = (100.0
                                                * taskSnapshot.getBytesTransferred()
                                                / taskSnapshot.getTotalByteCount());
                                        progressDialog.setMessage(
                                                "Uploaded "
                                                        + (int) progress + "%");
                                    }
                                });
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

    private String getX_Coordinate(String point) {
        String[] parts = point.split(",");
        return parts[0].trim().substring(1).trim();
    }

    private String getY_Coordinate(String point) {
        String[] parts = point.split(",");
        return parts[1].trim().substring(0, parts[1].trim().length() - 1).trim();
    }




}