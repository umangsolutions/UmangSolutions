package com.example.samplefirebase.tflite;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Trace;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.samplefirebase.RegisterFaceActivity;
import com.example.samplefirebase.modals.RegisterFaceData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.logging.Logger;

public class TFLiteObjectDetectionAPIModel implements SimilarityClassifier {
   // private static final Logger LOGGER = new Logger();

    //private static final int OUTPUT_SIZE = 512;
    private static final int OUTPUT_SIZE = 192;

    // Only return this many results.
    private static final int NUM_DETECTIONS = 1;

    // Float model
    private static final float IMAGE_MEAN = 128.0f;
    private static final float IMAGE_STD = 128.0f;

    // Number of threads in the java app
    private static final int NUM_THREADS = 4;
    private boolean isModelQuantized;
    // Config values.
    private int inputSize;
    // Pre-allocated buffers.
    private Vector<String> labels = new Vector<String>();
    private int[] intValues;
    // outputLocations: array of shape [Batchsize, NUM_DETECTIONS,4]
    // contains the location of detected boxes
    private float[][][] outputLocations;
    // outputClasses: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the classes of detected boxes
    private float[][] outputClasses;
    // outputScores: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the scores of detected boxes
    private float[][] outputScores;
    // numDetections: array of shape [Batchsize]
    // contains the number of detected boxes
    private float[] numDetections;

    private float[][] embeedings;

    private ByteBuffer imgData;

    private Interpreter tfLite;

    // Face Mask Detector Output
    private float[][] output;

    private List faceFeaturesList;

    private HashMap<String, Recognition> registered = new HashMap<>();

    private DatabaseReference databaseReference,myRef;


/*
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

                       final List extra = (List) snapshot1.getValue(RegisterFaceData.class).getImgUrl();

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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Error Occurred. Toast Message not working
            }
        });
    }
*/

    public void register(String name, String JNTU, String Department, String Section,Recognition rec) {

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Faces_Data");
        //databaseReference.updateChildren(registered);


        String key = databaseReference.push().getKey();



        RegisterFaceData registerFaceData = new RegisterFaceData(name, JNTU, Section, Department,rec.getExtra().toString(),rec.getLocation().bottom,rec.getLocation().isEmpty(),rec.getLocation().left,rec.getLocation().right,rec.getLocation().bottom);
        //Recognition recognition = new Recognition(rec.getId(),rec.getTitle(),rec.getDistance(),null,rec.getLocation(),rec.getColor(),null);

        assert key != null;
       databaseReference.child(key).setValue(registerFaceData);
       // databaseReference.child(key).child("Recognition").setValue(recognition);*/

        registered.put(name,rec);

    }

    private TFLiteObjectDetectionAPIModel() {}

    /** Memory-map the model file in Assets. */
    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param assetManager The asset manager to be used to load assets.
     * @param modelFilename The filepath of the model GraphDef protocol buffer.
     * @param labelFilename The filepath of label file for classes.
     * @param inputSize The size of image input
     * @param isQuantized Boolean representing model is quantized or not
     */
    public static SimilarityClassifier create(
            final AssetManager assetManager,
            final String modelFilename,
            final String labelFilename,
            final int inputSize,
            final boolean isQuantized)
            throws IOException {

        final TFLiteObjectDetectionAPIModel d = new TFLiteObjectDetectionAPIModel();

        String actualFilename = labelFilename.split("file:///android_asset/")[1];
        InputStream labelsInput = assetManager.open(actualFilename);
        BufferedReader br = new BufferedReader(new InputStreamReader(labelsInput));
        String line;
        while ((line = br.readLine()) != null) {
            //LOGGER.w(line);
            d.labels.add(line);
        }
        br.close();

        d.inputSize = inputSize;

        try {
            d.tfLite = new Interpreter(loadModelFile(assetManager, modelFilename));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        d.isModelQuantized = isQuantized;
        // Pre-allocate buffers.
        int numBytesPerChannel;
        if (isQuantized) {
            numBytesPerChannel = 1; // Quantized
        } else {
            numBytesPerChannel = 4; // Floating point
        }
        d.imgData = ByteBuffer.allocateDirect(1 * d.inputSize * d.inputSize * 3 * numBytesPerChannel);
        d.imgData.order(ByteOrder.nativeOrder());
        d.intValues = new int[d.inputSize * d.inputSize];

        d.tfLite.setNumThreads(NUM_THREADS);
        d.outputLocations = new float[1][NUM_DETECTIONS][4];
        d.outputClasses = new float[1][NUM_DETECTIONS];
        d.outputScores = new float[1][NUM_DETECTIONS];
        d.numDetections = new float[1];
        return d;
    }

    // looks for the nearest embeeding in the dataset (using L2 norm)
    // and retrurns the pair <id, distance>

    private Pair<String, Float> findNearest(float[] emb) {

        Pair<String, Float> ret = null;

        for (Map.Entry<String, Recognition> entry : registered.entrySet()) {
            final String name = entry.getKey();



            final float[] knownEmb = ((float[][]) entry.getValue().getExtra())[0];


            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = emb[i] - knownEmb[i];
                distance += diff*diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                ret = new Pair<>(name, distance);
            }
        }

        return ret;

    }



    @Override
    public List<Recognition> recognizeImage(final Bitmap bitmap, boolean storeExtra) {

        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        Trace.beginSection("PreProcessBitmap");
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                }
            }
        }
        Trace.endSection(); // preprocessBitmap

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed");


        Object[] inputArray = {imgData};

        Trace.endSection();

// Here outputMap is changed to fit the Face Mask detector
        Map<Integer, Object> outputMap = new HashMap<>();

        embeedings = new float[1][OUTPUT_SIZE];
        outputMap.put(0, embeedings);


        // Run the inference call.
        Trace.beginSection("run");
        //tfLite.runForMultipleInputsOutputs(inputArray, outputMapBack);
        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);
        Trace.endSection();

//    String res = "[";
//    for (int i = 0; i < embeedings[0].length; i++) {
//      res += embeedings[0][i];
//      if (i < embeedings[0].length - 1) res += ", ";
//    }
//    res += "]";


        // Change to MAX_VALUE
        float distance = Float.MAX_VALUE;
        String id = "0";
        String label = "?";


    if (registered.size() > 0) {
            //LOGGER.i("dataset SIZE: " + registered.size());
            final Pair<String, Float> nearest = findNearest(embeedings[0]);
            if (nearest != null) {

                final String name = nearest.first;
                label = name;
                distance = nearest.second;

               // LOGGER.i("nearest: " + name + " - distance: " + distance);


            }
        }



        final int numDetectionsOutput = 1;

        final ArrayList<Recognition> recognitions = new ArrayList<>(numDetectionsOutput);

        Recognition rec = new Recognition(id, label, distance, new RectF());
        recognitions.add( rec );

        if (storeExtra) {
            rec.setExtra(embeedings);
        }

        Trace.endSection();
        return recognitions;

    }

    @Override
    public void enableStatLogging(final boolean logStats) {}

    @Override
    public String getStatString() {
        return "";
    }

    @Override
    public void close() {}

    public void setNumThreads(int num_threads) {
        if (tfLite != null) tfLite.setNumThreads(num_threads);
    }

    @Override
    public void setUseNNAPI(boolean isChecked) {
        if (tfLite != null) tfLite.setUseNNAPI(isChecked);
    }

}
