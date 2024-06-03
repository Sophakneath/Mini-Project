package com.example.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
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

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.example.myapplication.utils.Product;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.myapplication.utils.Constant.PICK_FROM_GALLERY;
import static com.example.myapplication.utils.Constant.REQUEST_CODE_PERMISSIONS;
import static com.example.myapplication.utils.Constant.REQUIRED_PERMISSIONS;
import static com.example.myapplication.utils.Constant.RESULT_LOAD_IMG;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN;

public class CaptureActivity extends AppCompatActivity {

    ImageButton home, capture, gallery, back;
    ImageView imageResult;
    Interpreter interpreterApi;
    Executor mCameraExecutor = Executors.newSingleThreadExecutor();
    private static final String MODEL_PATH = "model_channel_pruning.tflite";
    ProcessCameraProvider cameraProvider;
    ImageAnalysis imageAnalysis;
    PreviewView mPreviewView;
    int inputWidth, inputHeight, channels, batchSize, numClass;
    float minThreshold = 96.0f, maxThreshold = 97.0f;
    List<String> classLabels;
    CoordinatorLayout bottomSheetContainer;
    FrameLayout bottomSheet;
    FragmentContainerView fragmentContainerView;
    Button scanAgain;
    ArrayList<Integer> allergenSelected;
    DatabaseReference myRef;
    RoundCornerProgressBar progressBar;
    ExecutorService executorService;
    boolean isCompleted = false;
    TextView detect;

    BottomSheetBehavior bottomSheetBehavior;
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
        executorService = Executors.newFixedThreadPool(2);

        back = findViewById(R.id.back);
        home = findViewById(R.id.home);
        capture = findViewById(R.id.capture);
        gallery = findViewById(R.id.gallery);
        mPreviewView = findViewById(R.id.previewCapture);
        imageResult = findViewById(R.id.imageResult);
        bottomSheetContainer = findViewById(R.id.bottomSheet);
        fragmentContainerView = findViewById(R.id.fragementContainer);
        scanAgain = findViewById(R.id.scanAgain);
        progressBar = findViewById(R.id.progressBar);
        detect = findViewById(R.id.detect);
        bottomSheet = findViewById(R.id.standard_bottom_sheet);

        classLabels = loadLabels();
        numClass = classLabels.size();
        int[] inputShape = interpreterApi.getInputTensor(0).shape();
        inputWidth = inputShape[2];
        inputHeight = inputShape[3];
        channels = inputShape[1];
        batchSize = inputShape[0];
        allergenSelected = getIntent().getIntegerArrayListExtra("allergenSelected");
        boolean isOpenGallery = getIntent().getBooleanExtra("gallery", false);

        Log.d("Allergen", String.valueOf(allergenSelected.size()));

//        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();

        back.setOnClickListener(view -> finish());

        home.setOnClickListener(view -> CaptureActivity.this.startActivity(new Intent(CaptureActivity.this, HomeActivity.class)));

        capture.setOnClickListener(view -> {
            mPreviewView.setVisibility(View.VISIBLE);
            imageResult.setVisibility(View.GONE);
            captureImage();
        });

        scanAgain.setOnClickListener(view -> {
            bottomSheetContainer.setVisibility(View.GONE);
            bottomSheetBehavior.setState(STATE_HIDDEN);
            fragmentContainerView.removeAllViews();
            scanAgain.setVisibility(View.GONE);
            detect.setText("Detecting...");
            imageResult.setVisibility(View.GONE);
            mPreviewView.setVisibility(View.VISIBLE);
            startCamera();
        });

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(STATE_HIDDEN);
        bottomSheet.setBackground(getDrawable(R.drawable.rounded_dialog));

        gallery.setOnClickListener(view -> {
            if (!checkGalleryPermissions()) requestGalleryPermission();
            else pickImage();
        });

        if (isOpenGallery) {
            if (!checkGalleryPermissions()) requestGalleryPermission();
            else pickImage();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!checkPermissions()) requestPermission();
        else startCamera();
    }

    private boolean checkGalleryPermissions() {
        for(String permission: REQUIRED_PERMISSIONS){
            int permissionState = ActivityCompat.checkSelfPermission(this, permission);
            if(permissionState != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }

    private void requestGalleryPermission() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PICK_FROM_GALLERY);
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        if(permissionState != PackageManager.PERMISSION_GRANTED) return false;
        return true;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length == 0) Log.i("TAG", "User interaction was cancelled");
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) startCamera();
            else Log.i("TAG", "Permission Denied");
        } else if(requestCode == PICK_FROM_GALLERY) {
            if (grantResults.length == 0) Log.i("TAG", "User interaction was cancelled");
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) pickImage();
            else Log.i("TAG", "Permission Denied");
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK && reqCode == RESULT_LOAD_IMG && data != null) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                imageResult.setVisibility(View.VISIBLE);
                mPreviewView.setVisibility(View.GONE);

                runInference(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("Image", "Something went wrong");
            }
        }else {
            Log.d("Image", "Image not pick");
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

    private void pickImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        CaptureActivity.this.startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
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
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

        cameraProvider.unbindAll();
        imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(128, 128)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
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

    private List<Product> processOutputTop3(float[][] outputScores, List<String> labels) {
        List<Product> top3 = new ArrayList<>();
        float sumExpScores = 0;

        // Iterate through the output scores
        for (int i = 0; i < outputScores[0].length; i++) {
            String label = labels.get(i);
            float score = outputScores[0][i];
            sumExpScores += Math.exp(score);

            // Create Recognition object for each class
            Product product = new Product(i, label, score);

            // Add to the list
            top3.add(product);
        }

        // Sort the list based on scores in descending order
        Collections.sort(top3, Comparator.comparingDouble(Product::getConfidence).reversed());
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
        imageAnalysis.setAnalyzer(mCameraExecutor, image -> {
            Bitmap bitmap = image.toBitmap();
            image.close();
            runInference(bitmap);
        });
    }

    private void runInference(Bitmap bitmap) {
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        CaptureActivity.this.getWindowManager()
//                .getDefaultDisplay()
//                .getMetrics(displayMetrics);
//        int height = displayMetrics.heightPixels;
//        int width = displayMetrics.widthPixels;
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotation = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        runOnUiThread(() -> {
            updateUI(rotation);
            cameraProvider.unbindAll();
        });

        // Run inference
        if (interpreterApi == null) {
            Log.e("TFLite", "Interpreter is not initialized.");
        }

        float[][][][] imageArray = preprocessImage(rotation);
        float[][] outputScores = new float[batchSize][numClass];

        new AsyncTask(imageArray, outputScores, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        runOnUiThread(() -> new AsyncTask(false, outputScores).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR));
    }

    private class AsyncTask extends android.os.AsyncTask<Void, Integer, Void> {
        float[][][][] imageArray;
        float[][] outputScores;
        Boolean inferenceTask;

        public AsyncTask(float[][][][] imageArray, float[][] outputScores, boolean inferenceTask) {
            this.imageArray = imageArray;
            this.outputScores = outputScores;
            this.inferenceTask = inferenceTask;
        }

        public AsyncTask(boolean inferenceTask, float[][] outputScores) {
            this.inferenceTask = inferenceTask;
            this.outputScores = outputScores;
        }

        @Override
        protected void onPreExecute() {
            // Initialize progress bar
            if (!inferenceTask) {
                progressBar.setMax(100);
                progressBar.setProgress(0);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (inferenceTask) {
                interpreterApi.run(imageArray, outputScores);
                Log.d("Inference result", Arrays.toString(outputScores));
            }
            else {
                // Simulate inference progress
                for (int progress = 0; progress <= 100; progress += 10) {
                    // Update progress
                    publishProgress(progress);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // Update progress bar on UI thread
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (inferenceTask) isCompleted = true;
            if((inferenceTask && progressBar.getProgress() == 100) || (isCompleted == true && progressBar.getProgress() == 100)) {
                // Inference completed
                // Post-process the output
                List<Product> topPredictions = processOutputTop3(outputScores, loadLabels());

                // Post-process to database for checking safe or not
                verifyDataWithDatabase(topPredictions);

                for (int i = 0; i < 3; i++) {
                    Log.d("Prediction", "Predicted Class: " + topPredictions.get(i).getEnglishName() + ", Confidence: " + topPredictions.get(i).getConfidence());
                }
                isCompleted = false;
            }
        }
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
        bottomSheetContainer.setVisibility(View.VISIBLE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        imageResult.setImageBitmap(bitmap);
    }

    private void verifyDataWithDatabase(List<Product> products) {
        Product product = products.get(0);
        Bundle bundle = new Bundle();
        ArrayList<String> allergenInfo = new ArrayList<>();

        if (product.getConfidence() > maxThreshold) {
            myRef.child(product.getEnglishName()).get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Product productResult = task.getResult().getValue(Product.class);
                    for (DataSnapshot snapshot: task.getResult().child("allergenInfo").getChildren()) {
                        Log.d("firebase", snapshot.toString());
                        allergenInfo.add(snapshot.getValue().toString());
                        if (allergenSelected.contains(Integer.parseInt(snapshot.getKey()))) productResult.setIsSafe(false);
                    }
                    productResult.setAllergenList(allergenInfo);
                    productResult.setConfidence(product.getConfidence());
                    Log.d("firebase", productResult.toString());

                    if (productResult.getSafe()) {
                        bundle.putParcelable("product", (Parcelable) productResult);
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .add(R.id.fragementContainer, SafeResult.class, bundle)
                                .commit();
                    }
                    else {
                        bundle.putParcelable("product", (Parcelable) productResult);
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .add(R.id.fragementContainer, UnsafeResult.class, bundle)
                                .commit();
                    }
                }
            });
        }else if (product.getConfidence() > minThreshold && product.getConfidence() < maxThreshold ) {
            ArrayList<Product> results = new ArrayList<>();
//            for (Product p: products) {
                myRef.child(products.get(0).getEnglishName()).get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    }
                    else {
                        Product productResult = task.getResult().getValue(Product.class);
                        for (DataSnapshot snapshot: task.getResult().child("allergenInfo").getChildren()) {
                            Log.d("firebase", snapshot.toString());
                            allergenInfo.add(snapshot.getValue().toString());
                            if (allergenSelected.contains(Integer.parseInt(snapshot.getKey()))) productResult.setIsSafe(false);
                        }
                        productResult.setAllergenList(allergenInfo);
                        productResult.setConfidence(products.get(0).getConfidence());
                        Log.d("firebase", productResult.toString());
                        results.add(productResult);

                    }
                });

            myRef.child(products.get(1).getEnglishName()).get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Product productResult = task.getResult().getValue(Product.class);
                    for (DataSnapshot snapshot: task.getResult().child("allergenInfo").getChildren()) {
                        Log.d("firebase", snapshot.toString());
                        allergenInfo.add(snapshot.getValue().toString());
                        if (allergenSelected.contains(Integer.parseInt(snapshot.getKey()))) productResult.setIsSafe(false);
                    }
                    productResult.setAllergenList(allergenInfo);
                    productResult.setConfidence(products.get(1).getConfidence());
                    Log.d("firebase", productResult.toString());
                    results.add(productResult);

                }
            });

            myRef.child(products.get(2).getEnglishName()).get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Product productResult = task.getResult().getValue(Product.class);
                    for (DataSnapshot snapshot: task.getResult().child("allergenInfo").getChildren()) {
                        Log.d("firebase", snapshot.toString());
                        allergenInfo.add(snapshot.getValue().toString());
                        if (allergenSelected.contains(Integer.parseInt(snapshot.getKey()))) productResult.setIsSafe(false);
                    }
                    productResult.setAllergenList(allergenInfo);
                    productResult.setConfidence(products.get(2).getConfidence());
                    Log.d("firebase", productResult.toString());
                    results.add(productResult);
                    bundle.putParcelableArrayList("products", results);
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fragementContainer, PossibleResult.class, bundle)
                            .commit();
                }
            });
//            }
        }else {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragementContainer, NotFoundResult.class, null)
                    .commit();
        }

        runOnUiThread(() -> {
            detect.setText("Completed");
            scanAgain.setVisibility(View.VISIBLE);
        });
    }
}