package com.example.sudarshanseshadri.bork;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sudarshanseshadri.bork.motiondetection.MotionDetector;
import com.example.sudarshanseshadri.bork.motiondetection.MotionDetectorCallback;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import clarifai2.dto.prediction.Prediction;
import clarifai2.dto.workflow.WorkflowPredictResult;


public class CameraFragment extends Fragment {

    private static final int CAMERA_REQUEST_CODE = 1;
    MotionDetector motionDetector;

    SeekBar seekBar;

    SurfaceView surfaceView;
    ImageView exclamation;

    Button startScanning;
    boolean isScanningToggle = false;

    MediaPlayer barkPlayer;


    AtomicBoolean canScanAgain = new AtomicBoolean(true);
    View rootview;

    MainTabbedActivity mainTabbedActivity;

    boolean barkAtSquirrel, barkAtGrackle;
    int barkSound;


    public CameraFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        final SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        barkSound = sharedPref.getInt("barkSound", R.raw.bruno_british_lab);
        barkAtSquirrel = sharedPref.getBoolean("barkAtSquirrel", false);
        barkAtGrackle = sharedPref.getBoolean("barkAtGrackle", false);

        mainTabbedActivity = (MainTabbedActivity) getActivity();

        // Inflate the layout for this fragment
        rootview = inflater.inflate(R.layout.fragment_camera, container, false);
        startScanning = rootview.findViewById(R.id.button_startDetection);
        isScanningToggle=false;

        barkPlayer = MediaPlayer.create(getContext(), barkSound);


        startScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScanningToggle==false)
                {
                    isScanningToggle=true;
                    startScanning.setText("Stop Detection");
                }
                else
                {
                    isScanningToggle=false;
                    startScanning.setText("Start Detection");
                }
            }
        });

        getPermissions();
        Log.d("HOME", "in here");

        surfaceView = (SurfaceView) rootview.findViewById(R.id.surfaceView);

        motionDetector = new MotionDetector(getContext(), surfaceView);

        exclamation = rootview.findViewById(R.id.imageView_exclamation);


        seekBar = rootview.findViewById(R.id.seekBar);


        ScaleGestureDetector scaleGestureDetector;

        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCanScanAgain(true);
                surfaceView.setFocusableInTouchMode(true);
                surfaceView.setFocusable(true);
                surfaceView.requestFocus();
                motionDetector.focus();
            }
        });





        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double zoom = progress;
                zoom /= 100.0;

                zoom *= motionDetector.getMaxZoom();
                motionDetector.zoom(zoom);

                Log.d("HOME", "" + zoom);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        motionDetector.setMotionDetectorCallback(new MotionDetectorCallback() {
            @Override
            public void onMotionDetected() {
                if (isScanningToggle == true) {

                    Log.d("HOME", "Motion Detected");
                    if (canScanAgain.get() == true) {
                        scanImage();
                        exclamation.setImageResource(R.drawable.exclamation);
                        fadeOutAndHideImage(exclamation);
                    }

                }
            }

            @Override
            public void onTooDark() {
                Log.d("HOME","Too dark");
            }
        });



        return rootview;
    }


    private void getPermissions() {
        // Here, thisActivity is the current activity
        Log.d("HOME", "" + ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA));
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            Log.d("HOME", "not granted");
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        motionDetector.onResume();

        if (motionDetector.checkCameraHardware()) {
            Log.d("HOME","Camera found");
        } else {
            Log.d("HOME","No camera available");
        }

        double zoom = seekBar.getProgress();
        zoom /= 100.0;

        zoom *= motionDetector.getMaxZoom();
        motionDetector.zoom(zoom);
    }

    @Override
    public void onPause() {
        super.onPause();
        motionDetector.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void fadeOutAndHideImage(final ImageView img)
    {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(1000);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                img.setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        img.startAnimation(fadeOut);
    }


    public void scanImage()
    {
        try {


            byte[] data = motionDetector.getNextData().get();
            AtomicInteger width = motionDetector.getNextWidth();
            AtomicInteger height = motionDetector.getNextHeight();
            Camera.Parameters parameters = motionDetector.getmCamera().getParameters();

            int format = parameters.getPreviewFormat();
            //YUV formats require more conversion
            if (format == ImageFormat.NV21 || format == ImageFormat.YUY2 || format == ImageFormat.NV16) {
                int w = parameters.getPreviewSize().width;
                int h = parameters.getPreviewSize().height;
                // Get the YuV image
                YuvImage yuv_image = new YuvImage(data, format, w, h, null);
                // Convert YuV to Jpeg
                Rect rect = new Rect(0, 0, w, h);

                ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
                yuv_image.compressToJpeg(rect, 100, output_stream);
                byte[] byt = output_stream.toByteArray();


                Bitmap bitmap = BitmapFactory.decodeByteArray(byt, 0, byt.length);

                bitmap = rotateBitmap(bitmap, motionDetector.getRotationAmount());

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byt = stream.toByteArray();

                if (canScanAgain.get() == true) {
                    new ImageRecognitionThread().execute(byt);
                    canScanAgain.set(false);
                } else {
                    Log.d("HOME", "Wait until done!");
                }




            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }



    }



    public Bitmap rotateBitmap(Bitmap bitmap, int degrees)
    {
        Matrix matrix = new Matrix();

        matrix.postRotate(degrees);


        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }




    public void setCanScanAgain(boolean canScanAgain) {

        this.canScanAgain.set(canScanAgain);

    }



    public class ImageRecognitionThread extends AsyncTask<byte[], Void, Void>
    {

        @Override
        protected Void doInBackground(byte[]... bytes) {


            Log.d("PREDICT", "doing");
            byte[] byteArray = bytes[0];


            ClarifaiClient client = new ClarifaiBuilder("06bc654950094c0aa39287ab4e8eeeea")
                    .buildSync();

//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//            byte[] byteArray = stream.toByteArray();

            try {
                final WorkflowPredictResult response =
                        // You can also do client.getModelByID("id") to get your custom model
                        client.workflowPredict("identify-at-feeder")
                                .withInputs(ClarifaiInput.forImage(byteArray))
                                .executeSync()
                                .get();
                boolean isSquirrel = false;
                boolean isGrackle = false;
                boolean isBird = false;
                for (ClarifaiOutput<Prediction> modelOutput : response.workflowResults().get(0).predictions())
                {
                    for (Prediction p : modelOutput.data())
                    {
                        Concept c = (Concept) p;
                        if (c.value()>0.1)

                        {
                            Log.d("PRED", c.name() + ", " + c.value());
                        }

                        if(c.name().equals("Squirrel") && c.value()>0.7)
                        {
                            if (barkAtSquirrel == true) {
                                barkPlayer.start();
                            }
                            saveImageToFileSystem(byteArray, "Squirrel");
                            Log.d("PREDN", "Squirrel!");

                            Thread.sleep(30000);
                            Log.d("PREDICT", "wait over!");
                            break;

                        }
                        else if(c.name().equals("Grackle") && c.value()>0.5)
                        {
                            if (barkAtGrackle == true) {
                                barkPlayer.start();
                            }

                            saveImageToFileSystem(byteArray, "Grackle");
                            Log.d("PREDN", "Grackle!");

                            Thread.sleep(10000);
                            Log.d("PREDICT", "wait over!");
                            break;

                        }
                        else if(c.name().equals("bird") && c.value()>0.7)
                        {
                            saveImageToFileSystem(byteArray, "Bird");
                            Log.d("PREDN", "B!");
                            Thread.sleep(10000);
                            Log.d("PREDICT", "wait over!");
                            break;

                        }



                    }
                }
                Thread.sleep(5000);
                setCanScanAgain(true);




            }
            catch (Exception e){
                e.printStackTrace();
                //Toast.makeText(getContext(), "Please check your internet connection.", Toast.LENGTH_LONG);

            }





//            client.workflowPredict("{workflow-id}")
//                    .withInputs(ClarifaiInput.forImage("https://samples.clarifai.com/metro-north.jpg"))
//                    .executeSync();

            return null;
        }



        public void saveImageToFileSystem(byte[] byt, String s)
        {


            try {
                ContextWrapper cw = new ContextWrapper(getContext());
                // path to /data/data/yourapp/app_data/imageDir
                File directory = cw.getDir("images", Context.MODE_PRIVATE);
                // Create imageDir
                File file = new File(directory, "I_" + System.currentTimeMillis() + ".jpg");

                //Uri uriSavedImage = Uri.fromFile(file);

                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bos.write(byt);
                bos.flush();
                bos.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy, h:mm a");
            String currentDateandTime = sdf.format(new Date());

            String file = "log.txt";
            saveToFile(s + " at " + currentDateandTime);


        }

        public  boolean saveToFile(String data){



            try {

                ContextWrapper cw = new ContextWrapper(getContext());
                // path to /data/data/yourapp/app_data/imageDir
                File directory = cw.getDir("images", Context.MODE_PRIVATE);
                // Create imageDir
                File file = new File(directory, "log.txt");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(file,true);
                fileOutputStream.write((data + System.getProperty("line.separator")).getBytes());

                return true;
            }  catch(FileNotFoundException e) {
                e.printStackTrace();
                Log.d("PRED", "fnfe");
            }  catch(IOException e) {
                e.printStackTrace();
                Log.d("PRED", "ioEX");
            }
            return false;


        }


    }









}
