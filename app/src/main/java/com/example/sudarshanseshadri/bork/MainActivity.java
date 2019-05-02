package com.example.sudarshanseshadri.bork;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.sudarshanseshadri.bork.motiondetection.MotionDetector;
import com.example.sudarshanseshadri.bork.motiondetection.MotionDetectorCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import android.net.Uri;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import clarifai2.dto.prediction.Prediction;
import clarifai2.dto.workflow.WorkflowPredictResult;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 1;
    MotionDetector motionDetector;

    SeekBar seekBar, checkIntervalBar;

    SurfaceView surfaceView;

    LinearLayout box;

    TextView checkTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermissions();
        Log.d("HOME", "in here");

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        motionDetector = new MotionDetector(this, surfaceView);


        seekBar = findViewById(R.id.seekBar);

        checkIntervalBar = findViewById(R.id.seekBar2);

        box = findViewById(R.id.box);


        checkTime = findViewById(R.id.textView);
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                motionDetector.zoom();
            }
        });

        final int maxZoom = motionDetector.getMaxZoom();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("HOME", "" + progress);
                double zoom = (double) progress/100 * (double) maxZoom;
                motionDetector.zoom(zoom);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        checkIntervalBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getPhotoDescription();

            }
        });

        box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                box.setBackgroundColor(Color.WHITE);
                motionDetector.unzoom();
            }
        });

        motionDetector.setMotionDetectorCallback(new MotionDetectorCallback() {
            @Override
            public void onMotionDetected() {
                Log.d("HOME", "Motion Detected");
                box.setBackgroundColor(Color.RED);
                saveImageToFileSystem();
            }

            @Override
            public void onTooDark() {
                Log.d("HOME","Too dark");
            }
        });



    }

    private void getPermissions() {
        // Here, thisActivity is the current activity
        Log.d("HOME", "" + ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            Log.d("HOME", "not granted");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        motionDetector.onResume();

        if (motionDetector.checkCameraHardware()) {
            Log.d("HOME","Camera found");
        } else {
            Log.d("HOME","No camera available");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        motionDetector.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }



    public void saveImageToFileSystem()
    {

        byte[] data = motionDetector.getNextData().get();

        String s = null;
        try {
            s = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Uri uri = Uri.parse(s);

        BitmapFactory.Options options = new BitmapFactory.Options();

        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            bitmap = null;
            e.printStackTrace();
        }

        String path = Environment.getExternalStorageDirectory().toString();

        String filename = "myfile.jpg";

        File file = new File(path, filename);

        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);

            bitmap.compress(Bitmap.CompressFormat.JPEG,100, outputStream);

            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Uri savedImageURI = Uri.parse(file.getAbsolutePath());


    }


    public void getPhotoDescription()
    {


        new AsyncThread().execute();


    }


    public class AsyncThread extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {
            ClarifaiClient client = new ClarifaiBuilder("06bc654950094c0aa39287ab4e8eeeea")
                    .buildSync();

            final WorkflowPredictResult response =
                    // You can also do client.getModelByID("id") to get your custom model
                    client.workflowPredict("identify-at-feeder")
                            .withInputs(ClarifaiInput.forImage("https://i.ibb.co/ZYZnCdT/sq.png"))
                            .executeSync()
                            .get();

            for (ClarifaiOutput<Prediction> modelOutput : response.workflowResults().get(0).predictions())
            {
                for (Prediction p : modelOutput.data())
                {
                    Concept c = (Concept) p;
                    Log.d("PRED", c.name() + " " + c.value());


                }
            }



//            client.workflowPredict("{workflow-id}")
//                    .withInputs(ClarifaiInput.forImage("https://samples.clarifai.com/metro-north.jpg"))
//                    .executeSync();


            return null;
        }
    }


}
