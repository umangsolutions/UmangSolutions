package com.example.samplefirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
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
import com.example.samplefirebase.tflite.SimilarityClassifier;
import com.example.samplefirebase.tflite.TFLiteObjectDetectionAPIModel;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static android.media.MediaCodec.MetricsConstants.MODE;

public class RegisterFaceActivity extends AppCompatActivity {

    ImageView imgFaceView, back_btn;

    EditText edtName,edtJNTU, edtSection, edtDepartment;
    Button btnUpload, btnRegister;

    String name,JNTU, Department,Section;

    private final int PICK_IMAGE_REQUEST = 22;

    private Uri filePath;

    private List<SimilarityClassifier.Recognition> mappedRecognitions;

    private Bitmap borderedBitmap,scaledBitmap,bitmapImage, faceDetectedBitmap, foundBitmap;

    InputImage image, filteredImage;

    PointF leftEarPos, rightEarPos,leftEyePos,rightEyePos,leftMouthPos,rightMouthPos,noseBasePos;

    float D1_VAL, D2_VAL, D3_VAL, D4_VAL, D5_VAL;

    StorageReference storageReference;

    SimilarityClassifier detector;
    FaceDetector faceDetector;

    // MobileFaceNet
    private static final int TF_OD_API_INPUT_SIZE = 112;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "mobile_face_net.tflite";

    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";

    private Bitmap faceBmp = null;

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

        mappedRecognitions = new LinkedList<>();

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

                if(mappedRecognitions!=null) {
                    SimilarityClassifier.Recognition rec = mappedRecognitions.get(0);
                    detector.register(name,JNTU,Department,Section,rec);
                   //uploadData(rec.getCrop(),name, JNTU, Section, Department);
                }


            }
        });

        // Setting the Face Detector Options
        final FaceDetectorOptions options =
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


        // For drawing Rectangle
        Task<List<Face>> result =
                faceDetector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        // Task completed successfully

                                        if(faces.size() == 0) {
                                            Toast.makeText(RegisterFaceActivity.this, "No Faces Detected in the Given Image !", Toast.LENGTH_SHORT).show();
                                        } else {
                                            onFacesDetected(faces, scaledBitmap);
                                        }
                                        // returns one Face Data and its Detected Features
                                      /*  for (Face face : faces) {
                                            // cutting the Faces using the Co-ordinates of Rect from Master Image
                                            faceDetectedBitmap = Bitmap.createBitmap(scaledBitmap,face.getBoundingBox().left,face.getBoundingBox().top,face.getBoundingBox().width(),face.getBoundingBox().height());
                                        }
*/
                                        // setting the Detected Face from User Uploaded Image from his Gallery

                                        /*foundBitmap = Bitmap.createScaledBitmap(faceDetectedBitmap,470,470,true);
                                        imgFaceView.setImageBitmap(foundBitmap);
*/
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
//          Object extra = result.getExtra();
//          if (extra != null) {
//            LOGGER.i("embeeding retrieved " + extra.toString());
//          }

                    float conf = result.getDistance();

                    Toast.makeText(this, ""+conf, Toast.LENGTH_SHORT).show();
                    if (conf < 1.0f) {

                        confidence = conf;
                        label = result.getTitle();

                        Toast.makeText(RegisterFaceActivity.this, "Found: " + label, Toast.LENGTH_SHORT).show();
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
            }

        }

       // updateResults(mappedRecognitions);

    }


    /*private void updateResults(final List<SimilarityClassifier.Recognition> mappedRecognitions) {

        if (mappedRecognitions.size() > 0) {
            SimilarityClassifier.Recognition rec = mappedRecognitions.get(0);
            if (rec.getExtra() != null) {
                showAddFaceDialog(rec);
            }

        }

    }
*/




    private void uploadData(final Bitmap croppedBitmap,final String name, final String JNTU, final String section, final String department) {

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
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
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
                                    RegisterFaceData registerFaceData = new RegisterFaceData(name, JNTU, section, department, downloadUrl.toString());
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