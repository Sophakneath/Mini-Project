package com.example.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentContainerView;

import com.example.myapplication.utils.Recognition;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.example.myapplication.utils.Constant.REQUEST_CODE_PERMISSIONS;
import static com.example.myapplication.utils.Constant.REQUIRED_PERMISSIONS;

public class CaptureActivity extends AppCompatActivity {

    FrameLayout home, capture, gallery;
    ImageView back, imageResult;
    Interpreter interpreterApi;
    Executor mCameraExecutor = Executors.newSingleThreadExecutor();
    private static final String MODEL_PATH = "model_tflite.tflite";
    ProcessCameraProvider cameraProvider;
    ImageAnalysis imageAnalysis;
    PreviewView mPreviewView;
    int inputWidth, inputHeight, channels, batchSize, numClass;
    float minThreshold = 10.0f, maxThreshold = 20.0f;
    List<String> classLabels;
    CoordinatorLayout bottomSheet;
    FragmentContainerView fragmentContainerView;
    Button scanAgain;
    ArrayList<Integer> allergenSelected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_capture);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initInterpreter();

        back = findViewById(R.id.back);
        home = findViewById(R.id.home);
        capture = findViewById(R.id.capture);
        gallery = findViewById(R.id.gallery);
        mPreviewView = findViewById(R.id.previewCapture);
        imageResult = findViewById(R.id.imageResult);
        bottomSheet = findViewById(R.id.bottomSheet);
        fragmentContainerView = findViewById(R.id.fragementContainer);
        scanAgain = findViewById(R.id.scanAgain);

        classLabels = loadLabels();
        numClass = classLabels.size();
        int[] inputShape = interpreterApi.getInputTensor(0).shape();
        inputWidth = inputShape[2];
        inputHeight = inputShape[3];
        channels = inputShape[1];
        batchSize = inputShape[0];
        allergenSelected = getIntent().getIntegerArrayListExtra("allergenSelected");

        Log.d("Allergen", String.valueOf(allergenSelected.size()));

        // TODO: 5/2/24 compare and get data form database
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CaptureActivity.this.startActivity(new Intent(CaptureActivity.this, HomeActivity.class));
            }
        });

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPreviewView.setVisibility(View.VISIBLE);
                imageResult.setVisibility(View.GONE);
                captureImage();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!checkPermissions()) requestPermission();
        else startCamera();
    }

    private boolean checkPermissions() {
        for(String permission: REQUIRED_PERMISSIONS){
            int permissionState = ActivityCompat.checkSelfPermission(this, permission);
            if(permissionState != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length == 0) Log.i("TAG", "User interaction was cancelled");
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) startCamera();
            else Log.i("TAG", "Permission Denied");
        }
    }

    private void initInterpreter() {
        // Load the TFLite model
        AssetManager assetManager = this.getAssets();
        try{
            Interpreter.Options options = new Interpreter.Options();
            options.setUseNNAPI(true);
            // Finish interpreter initialization
            this.interpreterApi = new Interpreter(loadModelFile(assetManager), options);
            Log.d("TAG", "Initialized TFLite interpreter.");
        }catch (Exception e){
            Log.d("","xee:"+e.getMessage());
        }
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    bindUseCases();
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    void bindUseCases() {
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        ///////////////////////////////////// Preview Use Case ////////////////////////////////////
        //build preview to use case
        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

        cameraProvider.unbindAll();

        imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(128, 128)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

        ///////////////////////////////////////////////////////////////////////////////////////////
    }

    private List<String> loadLabels() {
        List<String> labels = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("product_label.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                labels.add(line);
            }
            reader.close();
        } catch (IOException e) {
            Log.e("TFLite", "Error reading labels", e);
        }
        return labels;
    }

    private Map<Integer, String> loadAllergenTypes() {
        Map<Integer, String> allergenTypes = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("allergen_type.txt")));
            String line;
            int index = 1;
            while ((line = reader.readLine()) != null) {
                allergenTypes.put(index, line);
                index++;
            }
            reader.close();
        } catch (IOException e) {
            Log.e("TFLite", "Error reading allergenTypes", e);
        }
        return allergenTypes;
    }

    private Recognition processOutput(float[][] outputScores, List<String> labels) {
        int topClassIndex = -1;
        float topScore = -Float.MAX_VALUE;

        for (int i = 0; i < outputScores[0].length; i++) {
            if (outputScores[0][i] > topScore) {
                topScore = outputScores[0][i];
                topClassIndex = i;
            }
        }

        if (topClassIndex != -1) {
            String topClassLabel = labels.get(topClassIndex);
            return new Recognition(String.valueOf(topClassIndex), topClassLabel, topScore);
        } else {
            return null; // No class found
        }
    }

    private List<Recognition> processOutputTop3(float[][] outputScores, List<String> labels) {
        List<Recognition> top3 = new ArrayList<>();
        float sumExpScores = 0;

        // Iterate through the output scores
        for (int i = 0; i < outputScores[0].length; i++) {
            String label = labels.get(i);
            float score = outputScores[0][i];
            sumExpScores += Math.exp(score);

            // Create Recognition object for each class
            Recognition recognition = new Recognition(String.valueOf(i), label, score);

            // Add to the list
            top3.add(recognition);
        }

        // Sort the list based on scores in descending order
        Collections.sort(top3, Comparator.comparingDouble(Recognition::getConfidence).reversed());
        top3 = top3.subList(0, 3);

        for (int i = 0; i < 3; i++) {
            Log.d("Prediction", "Predicted Class: " + top3.get(i).getEnglishName() + ", Confidence: " + top3.get(i).getConfidence());
            float pro = (float) (Math.exp(top3.get(i).getConfidence()) / sumExpScores) * 100;
            top3.get(i).setConfidence(pro);
        }

        return top3;
    }


    private ByteBuffer loadModelFile(AssetManager assetManager) throws IOException {
        FileInputStream inputStream = null;
        FileChannel fileChannel = null;
        ByteBuffer buffer;
        try {
            inputStream = new FileInputStream(assetManager.openFd(MODEL_PATH).getFileDescriptor());
            fileChannel = inputStream.getChannel();
            long startOffset = assetManager.openFd(MODEL_PATH).getStartOffset();
            long declaredLength = assetManager.openFd(MODEL_PATH).getDeclaredLength();
            buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (fileChannel != null) {
                fileChannel.close();
            }
        }
        return buffer;
    }

    private void captureImage() {
        imageAnalysis.setAnalyzer(mCameraExecutor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                Bitmap bitmap = image.toBitmap();

                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotation = Bitmap.createBitmap(bitmap, 0, 0, image.getWidth(), image.getHeight(), matrix, true);

                runOnUiThread(() -> {
                    cameraProvider.unbindAll();
                    updateUI(rotation);
                });
//                InputStream inputStream;
//                try {
//                    inputStream = MainActivity.this.getAssets().open("f9cc9d84-8c85-4020-a751-d44432a6055e.jpg");
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                bitmap = BitmapFactory.decodeStream(inputStream);

                // Run inference
                if (interpreterApi == null) {
                    Log.e("TFLite", "Interpreter is not initialized.");
                }

                image.close();

                float[][][][] imageArray = preprocessImage(rotation);

                float[][] outputScores = new float[batchSize][numClass];
                interpreterApi.run(imageArray, outputScores);

                // Post-process the output
                List<Recognition> topPredictions = processOutputTop3(outputScores, loadLabels());

                // Post-process to database for checking safe or not
                // TODO: 5/2/24

                showFragement(topPredictions);

                for (int i = 0; i < 3; i++) {
                    Log.d("Prediction", "Predicted Class: " + topPredictions.get(i).getEnglishName() + ", Confidence: " + topPredictions.get(i).getConfidence());
                }
            }
        });
    }

    private float[][][][] preprocessImage(Bitmap bitmap) {
        float IMAGE_MEAN = 0f;
        float IMAGE_STD = 255.0f;

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true);

        int[] intValues = new int[inputWidth * inputHeight];
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());
        int pixel = 0;

        float[][][][] imageArray = new float[batchSize][channels][inputWidth][inputHeight];

        for (int i = 0; i < batchSize; ++i) {
            for (int j = 0; j < inputWidth; ++j) {
                for (int k = 0; k < inputHeight; ++k) {
                    int val = intValues[pixel++];
                    imageArray[i][0][j][k] = (((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
                    imageArray[i][1][j][k] = (((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
                    imageArray[i][2][j][k] = ((val & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
                }
            }
            pixel = 0;
        }
        return imageArray;
    }

    private String formatFloat(float d) {
        DecimalFormat df = new DecimalFormat("#.###");
        return df.format(d);
    }

    private void updateUI(Bitmap bitmap) {
        bottomSheet.setVisibility(View.VISIBLE);
        imageResult.setImageBitmap(bitmap);
    }

    private void showFragement(List<Recognition> recognitions) {
        float firstConfidence = recognitions.get(0).getConfidence();
        Bundle bundle = new Bundle();
        if (firstConfidence > maxThreshold) {
            Recognition recognition = recognitions.get(0);
            if (recognition.getSafe()) {
                bundle.putParcelable("product", (Parcelable) recognition);
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .add(R.id.fragementContainer, SafeResult.class, bundle)
                        .commit();
            }
            else {
                bundle.putParcelable("product", (Parcelable) recognition);
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .add(R.id.fragementContainer, UnsafeResult.class, bundle)
                        .commit();
            }
        }else if (firstConfidence > minThreshold && firstConfidence < maxThreshold ) {
            bundle.putParcelableArrayList("products", new ArrayList<>(recognitions));
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragementContainer, PossibleResult.class, bundle)
                    .commit();
        }else {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragementContainer, NotFoundResult.class, null)
                    .commit();
        }
    }
}