package com.example.samplefirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.samplefirebase.modals.FaceLandmarkData;
import com.example.samplefirebase.modals.IndividualFaceData;
import com.example.samplefirebase.modals.RegisterFaceData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RegisterFaceActivity extends AppCompatActivity {

    ImageView imgFaceView, back_btn;

    EditText edtName,edtJNTU, edtSection, edtDepartment;
    Button btnUpload, btnRegister;

    String name,JNTU, Department,Section;

    private final int PICK_IMAGE_REQUEST = 22;

    private Uri filePath;

    private Bitmap borderedBitmap,scaledBitmap,bitmapImage, faceDetectedBitmap, foundBitmap;

    InputImage image, filteredImage;

    PointF leftEarPos, rightEarPos,leftEyePos,rightEyePos,leftMouthPos,rightMouthPos,noseBasePos;

    float D1_VAL, D2_VAL, D3_VAL, D4_VAL, D5_VAL;

    StorageReference storageReference;

    // MobileFaceNet
    private static final int TF_OD_API_INPUT_SIZE = 112;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "mobile_face_net.tflite";

    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";

    //private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final boolean MAINTAIN_ASPECT = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_face);

        imgFaceView = findViewById(R.id.imgFace);

        edtName = findViewById(R.id.editName);
        edtJNTU = findViewById(R.id.editJNTU);
        edtDepartment = findViewById(R.id.editDepartment);
        edtSection = findViewById(R.id.editSection);

        btnRegister= findViewById(R.id.btnRegister);
        btnUpload = findViewById(R.id.btnUploadPhoto);

        back_btn = findViewById(R.id.back_btn);

        storageReference = FirebaseStorage.getInstance().getReference();

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterFaceActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = edtName.getText().toString().trim();
                JNTU = edtJNTU.getText().toString().trim();
                Department = edtDepartment.getText().toString().trim();
                Section = edtSection.getText().toString().trim();

                uploadData(name, JNTU, Section, Department);
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

                bitmapImage = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                // adding Border of 2 units to the Image received from the User's Gallery
                borderedBitmap = addWhiteBorder(bitmapImage,5);

                // Decreasing the Resolution of Image using the Below Formula
                int nh = (int) ( borderedBitmap.getHeight() * (512.0 / borderedBitmap.getWidth()) );
                scaledBitmap = Bitmap.createScaledBitmap(borderedBitmap, 512, nh, true);

                // passing the modified Resolution image to Firebase ML Kit
                image = InputImage.fromBitmap(scaledBitmap,0);

                // calling the method to detect Faces
                detectFaces(image);

            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
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

                                        // returns one Face Data and its Detected Features
                                        for (Face face : faces) {
                                            // cutting the Faces using the Co-ordinates of Rect from Master Image
                                            faceDetectedBitmap = Bitmap.createBitmap(scaledBitmap,face.getBoundingBox().left,face.getBoundingBox().top,face.getBoundingBox().width(),face.getBoundingBox().height());
                                        }

                                        // setting the Detected Face from User Uploaded Image from his Gallery
                                        foundBitmap = Bitmap.createScaledBitmap(faceDetectedBitmap,470,470,true);
                                        imgFaceView.setImageBitmap(foundBitmap);

                                        // applying Face Detector once again on filteredBitmap for more Precised Co-ordinates of Features
                                        filteredImage = InputImage.fromBitmap(foundBitmap,0);
                                        FaceDetector faceDetector = FaceDetection.getClient(options);
                                        Task<List<Face>> result = faceDetector.process(filteredImage).addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                                            @Override
                                            public void onSuccess(List<Face> faces) {
                                                for (Face face : faces) {

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
                                                }

                                                // passing all the Points Detected to calculate Euclidean Distance
                                                calculate_Face_Feature_Distances(leftEyePos, rightEyePos, leftMouthPos, rightMouthPos, noseBasePos);


                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(RegisterFaceActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });



                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        Toast.makeText(RegisterFaceActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

    }

    private void calculate_Face_Feature_Distances(PointF leftEyePos, PointF rightEyePos, PointF leftMouthPos, PointF rightMouthPos, PointF noseBasePos) {

        float LEFT_EYE_X = leftEyePos.x;
        float LEFT_EYE_Y = leftEyePos.y;

        float RIGHT_EYE_X = rightEyePos.x;
        float RIGHT_EYE_Y = rightEyePos.y;

        float LEFT_MOUTH_X = leftMouthPos.x;
        float LEFT_MOUTH_Y = leftMouthPos.y;

        float RIGHT_MOUTH_X = rightMouthPos.x;
        float RIGHT_MOUTH_Y = rightMouthPos.y;

        float NOSE_BASE_X = noseBasePos.x;
        float NOSE_BASE_Y = noseBasePos.y;

        // d1 means Distance between Left Eye and Right Eye
        D1_VAL = calculateEuclideanDistance(LEFT_EYE_X, LEFT_EYE_Y, RIGHT_EYE_X, RIGHT_EYE_Y);

        // d2 means Distance between NoseBase and LeftEye
        D2_VAL = calculateEuclideanDistance(NOSE_BASE_X, NOSE_BASE_Y, LEFT_EYE_X, LEFT_EYE_Y);

        // d3 means Distance between NoseBase and RightEye
        D3_VAL = calculateEuclideanDistance(NOSE_BASE_X, NOSE_BASE_Y, RIGHT_EYE_X, RIGHT_EYE_Y);

        // d4 represents Distance between NoseBase and Left Mouth
        D4_VAL = calculateEuclideanDistance(NOSE_BASE_X, NOSE_BASE_Y, LEFT_MOUTH_X, LEFT_MOUTH_Y);

        // d5 represents Distance between NoseBase and Right Mouth
        D5_VAL = calculateEuclideanDistance(NOSE_BASE_X,NOSE_BASE_Y,RIGHT_MOUTH_X, RIGHT_MOUTH_Y);
    }

    private float calculateEuclideanDistance(float x1, float y1, float x2, float y2) {
        double d = Math.sqrt(Math.abs((double) Math.pow(Math.abs(x2-x1),2) + (double) Math.pow(Math.abs(y2-y1),2)));
        return (float)d;
    }



    private void uploadData(final String name, final String JNTU, final String section, final String department) {

            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Registering Student...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            // adding listeners on upload
            // or failure of image
            Date date = new Date();
            String datetime = date.toString();

            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Faces_Data");

            // Defining the child of storageReference
            final StorageReference ref = storageReference.child("Images/" + name + "/"+ UUID.randomUUID().toString());

            // converting the obtained Bitmap to Bytes to send to Firebase
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            foundBitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            final byte[] data = byteArrayOutputStream.toByteArray();

            // uploading Image to Firebase Storage
            ref.putBytes(data).addOnSuccessListener(
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
                                    RegisterFaceData registerFaceData = new RegisterFaceData(name, JNTU, section, department, D1_VAL, D2_VAL, D3_VAL, D4_VAL, D5_VAL);
                                    reference.child(key).setValue(registerFaceData);

                                    progressDialog.dismiss();
                                    Toast.makeText(RegisterFaceActivity.this, "Student Registered Successfully !", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                        // Error, Image not uploaded
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterFaceActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                           }).addOnProgressListener(
                                new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    // Progress Listener for loading
                                    // percentage on the dialog box
                                    @Override
                                    public void onProgress(
                                            UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                        progressDialog.setMessage("Uploaded " + (int) progress + "%");
                                    }
                                });
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