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
import android.graphics.RectF;
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
import com.example.samplefirebase.tflite.SimilarityClassifier;
import com.example.samplefirebase.tflite.TFLiteObjectDetectionAPIModel;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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


    SimilarityClassifier detector;
    FaceDetector faceDetector;

    FaceBitmapAdapter mFacesAdapter;

    // MobileFaceNet
    private static final int TF_OD_API_INPUT_SIZE = 112;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "mobile_face_net.tflite";

    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";

    private Bitmap faceBmp = null;

    private HashMap<String, SimilarityClassifier.Recognition> registered;

    //private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final boolean MAINTAIN_ASPECT = false;

    private List<SimilarityClassifier.Recognition> mappedRecognitions;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);

        btnSelectImage = findViewById(R.id.selectImage);

        // Recycler View
        facesDisplayList = findViewById(R.id.facesDisplayList);

        mappedRecognitions = new LinkedList<>();


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

        // Setting the Face Detector Options
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .setMinFaceSize(0f)
                        .enableTracking()
                        .build();


        // Creating an instance
        FaceDetector Fdetector = FaceDetection.getClient(options);

        faceDetector = Fdetector;

        // Initialising the Tensorflow Model
        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);


            //cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Bitmap.Config.ARGB_8888);



        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        detector.loadData();


       /* btnRecognizeFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recognizeFaces();
            }
        });
*/

        registered = new HashMap<>();



    }

    public void loadData() {

        registered = new HashMap<>();

        myRef = FirebaseDatabase.getInstance().getReference("Faces_Data");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {

                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        //Fetching Data
                        final String name = Objects.requireNonNull(snapshot1.getValue(RegisterFaceData.class)).getName();

                        final Object extra = (Object) Objects.requireNonNull(snapshot1.getValue(RegisterFaceData.class)).getImgUrl();

                        SimilarityClassifier.Recognition  recognition = new SimilarityClassifier.Recognition("0","",-1.0f,new RectF());
                        recognition.setExtra(extra);
                        //String id = Objects.requireNonNull(snapshot1.child("Recognition").);
                        // String title = Objects.requireNonNull(snapshot1.child("Recognition").getValue(Recognition.class)).getTitle();
                        //Float distance = Objects.requireNonNull(snapshot1.child("Recognition").getValue(Recognition.class)).getDistance();
                        //Object extra = Objects.requireNonNull(snapshot1.child("Recognition").getValue(Recognition.class)).getExtra();
                        // RectF location = Objects.requireNonNull(snapshot1.child("Recognition").getValue(Recognition.class)).getLocation();
                        //Integer color = Objects.requireNonNull(snapshot1.child("Recognition").getValue(Recognition.class)).getColor();
                        Bitmap crop = null;


                        // SimilarityClassifier.Recognition recognition = snapshot1.child("Recognition").getValue(SimilarityClassifier.Recognition.class);

                        // adding to Hash Map
                        registered.put(name,recognition);


                    }

                    Toast.makeText(FaceDetectionActivity.this, "Registered Size"+registered.size(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Error Occurred. Toast Message not working
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

        // For drawing Rectangle
        Task<List<Face>> result =
                faceDetector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        // Task completed successfully
                                        // ...

                                        if(faces.size() == 0) {
                                            Toast.makeText(FaceDetectionActivity.this, "No Faces Detected in the Given Image !", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // loads Faces_Data from Firebase

                                            onFacesDetected(faces, scaledBitmap);
                                        }

                                        //drawView.createRectangles(rectangles);
                                       // drawView.setImageBitmap(scaledBitmap);
                                        txtNumber.setText("Number of Faces Detected: " + Integer.toString(faces.size()));

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

    private void onFacesDetected(List<Face> faces, Bitmap scaledBitmap) {

        final Canvas cvFace = new Canvas(faceBmp);

        mappedRecognitions =
                new LinkedList<SimilarityClassifier.Recognition>();

        for (Face face: faces) {

            final RectF boundingBox = new RectF(face.getBoundingBox());

            final boolean goodConfidence = true; //face.get;

            if(boundingBox!=null && goodConfidence) {

                RectF faceBB = new RectF(boundingBox);

                float sx = ((float) TF_OD_API_INPUT_SIZE) / faceBB.width();
                float sy = ((float) TF_OD_API_INPUT_SIZE) / faceBB.height();
                Matrix matrix = new Matrix();
                matrix.postTranslate(-faceBB.left, -faceBB.top);
                matrix.postScale(sx, sy);

                cvFace.drawBitmap(scaledBitmap,matrix,null);

                String label = "";
                float confidence = -1f;
                Integer color = Color.BLUE;
                Object extra = null;
                Bitmap crop = null;

                crop = Bitmap.createBitmap(scaledBitmap,
                        (int) faceBB.left,
                        (int) faceBB.top,
                        (int) faceBB.width(),
                        (int) faceBB.height());

                final List<SimilarityClassifier.Recognition> resultsAux = detector.recognizeImage(faceBmp,true);


                if (resultsAux.size() > 0) {

                    SimilarityClassifier.Recognition result = resultsAux.get(0);

                    extra = result.getExtra();

                    float conf = result.getDistance();
                    Toast.makeText(this, ""+conf, Toast.LENGTH_SHORT).show();
                    if (conf < 1.0f) {

                        confidence = conf;
                        label = result.getTitle();

                        Toast.makeText(FaceDetectionActivity.this, "Found: " + label, Toast.LENGTH_SHORT).show();

                     //   facesLandmarkDataList.add(result.getCrop(), result.getTitle(),result.);

                        if (result.getId().equals("0")) {
                            color = Color.GREEN;
                        }
                        else {
                            color = Color.RED;
                        }
                    }
                }

                // setting Features of the Face
                final SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
                        "0", label, confidence, boundingBox);

                result.setColor(color);
                result.setLocation(boundingBox);
                result.setExtra(extra);
                result.setCrop(crop);

                // adding to List
                mappedRecognitions.add(result);

                if(facesLandmarkDataList !=null) {

                    mFacesAdapter = new FaceBitmapAdapter(FaceDetectionActivity.this, facesLandmarkDataList);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FaceDetectionActivity.this, LinearLayoutManager.HORIZONTAL, false);
                    facesDisplayList.setLayoutManager(linearLayoutManager);
                    facesDisplayList.setAdapter(mFacesAdapter);

                } else {
                    Toast.makeText(FaceDetectionActivity.this, "No Data Found !", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }



/*
    private void recognizeFaces() {

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


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                    Toast.makeText(FaceDetectionActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
*/

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

    private String getX_Coordinate(String point) {
        String[] parts = point.split(",");
        return parts[0].trim().substring(1).trim();
    }

    private String getY_Coordinate(String point) {
        String[] parts = point.split(",");
        return parts[1].trim().substring(0, parts[1].trim().length() - 1).trim();
    }

}