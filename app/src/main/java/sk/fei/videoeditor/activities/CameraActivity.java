package sk.fei.videoeditor.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Audio;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Mode;
import com.otaliastudios.cameraview.controls.VideoCodec;
import com.otaliastudios.cameraview.size.Size;
import com.otaliastudios.cameraview.size.SizeSelector;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import sk.fei.videoeditor.R;

public class CameraActivity extends AppCompatActivity {

    private String TAG = "cameraActivity";
    private String mode = "maxDurationReached";
    private CameraView camera;
    private Chronometer mChronometer;

    private SimpleExoPlayer player;

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
    private static final int REQUEST_AUDIO = 100;

    private boolean mIsRecording = false;

    private File mVideoFolder;
    private String mVideoFileName;
    private Uri audioUri;
    private LinearLayout selectAudioView;
    private TextView selectAudioText;
    private ImageView flipCam, flashOn, flashOff, selectIcon;
    private ImageView startRecordBtn, recordingBtn;
    private ImageButton closeCam;
    private boolean hasFlash = false;
    private ConstraintLayout bottomBar;

    private boolean isAudioReady = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.overridePendingTransition(R.anim.fade_in_custom, R.anim.fade_out_custom);
        setContentView(R.layout.activity_camera);


        Intent i = getIntent();
        if (i != null) {
                //audioUri = Uri.parse(i.getStringExtra("audioUri"));
        }

        setCameraView();
    }

    private String getBackFacingCameraId(CameraManager cManager) throws CameraAccessException {
        for(final String cameraId : cManager.getCameraIdList()){
            CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
            int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);

            if(cOrientation == CameraCharacteristics.LENS_FACING_BACK)
            {
                Log.d("facing back", cameraId);
                return cameraId;
            }
        }
        return null;
    }

    private void toggleFlashLight(boolean isFlashlightOn) {
        if(camera.getFacing() == Facing.BACK){

        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

            String cameraId = null; // Usually front camera is at 0 position.
            try {
                cameraId = getBackFacingCameraId(Objects.requireNonNull(camManager));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraId != null) {
                    //camManager.setTorchMode(cameraId, isFlashlightOn);
                    if(isFlashlightOn){
                        camera.setFlash(Flash.TORCH);
                    }else{
                        camera.setFlash(Flash.OFF);
                    }
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
             }
            }
        }
    }

    private void setRecordButtonMaxSize(ImageView imageView, int newSize){
        /*
                    requestLayout()
                        Call this when something has changed which has
                        invalidated the layout of this view.
                */
        imageView.requestLayout();
        // Apply the new height for ImageView programmatically
        imageView.getLayoutParams().height = newSize;
        imageView.getLayoutParams().width = newSize;
        imageView.requestLayout();

        //imageView.setScaleType(ImageView.ScaleType.FIT_XY);

    }

    public static Size getOptimalPreviewSize(
            List<Size> sizes, int w, int h) {
        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Size optimalSize = null;/*  w  w  w  .ja va  2 s .c  om*/

        // Start with max value and refine as we iterate over available preview sizes. This is the
        // minimum difference between view and camera height.
        double minDiff = Double.MAX_VALUE;

        // Target view height
        int targetHeight = h;

        // Try to find a preview size that matches aspect ratio and the target view size.
        // Iterate over all available sizes and pick the largest size that can fit in the view and
        // still maintain the aspect ratio.
        for (Size size : sizes) {
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - targetHeight);
            }
        }

        // Cannot find preview size that matches the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }

        return optimalSize;
    }

    public void setCameraView(){
        camera = findViewById(R.id.camera);
        camera.setLifecycleOwner(this);
        camera.setMode(Mode.VIDEO);
        camera.setAudio(Audio.OFF);
        camera.setFacing(Facing.BACK);
        camera.setVideoCodec(VideoCodec.H_264);
        camera.setFlash(Flash.OFF);

        // This will be the size of videos taken with takeVideo().
        camera.setVideoSize(new SizeSelector() {
            @Override
            public List<Size> select(List<Size> source) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;

                List<Size> optimalSizes = new ArrayList<>();
//                optimalSizes.add(getOptimalPreviewSize(source, width, height));
                Log.d("size", "velkost: " + width + ", " + height);
                Log.d("size", "velkost: " +getOptimalPreviewSize(source, width, height).toString());
                for(Size s : source){
                    Log.d("size", "velkost: "+ s.toString());
                    String[] separated = s.toString().split("x");
                    double w = Double.parseDouble(separated[0]);
                    double h = Double.parseDouble(separated[1]);
                    double ratio = h/w;
                    if(ratio >= 1.75 && ratio <= 1.85){
                        optimalSizes.add(s);

                    }
                }
                return optimalSizes;
            }
        });

//        closeCam = findViewById(R.id.close_cam);
        selectAudioText = findViewById(R.id.selectAudioText);
        selectIcon = findViewById(R.id.selectIcon);
        bottomBar = findViewById(R.id.bottomBar);
        selectAudioView = findViewById(R.id.selectAudio);
        mChronometer = findViewById(R.id.chronometer);
        flipCam = findViewById(R.id.flip_facing_camera);
        flashOn = findViewById(R.id.flash_on);
        flashOff = findViewById(R.id.flash_off);

        createVideoFolder();

//        closeCam.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });


        camera.addCameraListener(new CameraListener() {
            @Override
            public void onVideoTaken(@NonNull VideoResult result) {
                super.onVideoTaken(result);
                Log.w("daAD","onVideoTaken called! Launching activity.");
                sendIntent(result);
            }

            @Override
            public void onVideoRecordingStart() {
                    toggleFlashLight(hasFlash);

                // Notifies that the actual video recording has started.
                // Can be used to show some UI indicator for video recording or counting time.
            }

            @Override
            public void onVideoRecordingEnd() {
                    toggleFlashLight(false);
                // Notifies that the actual video recording has ended.
                // Can be used to remove UI indicators added in onVideoRecordingStart.
                flipCam.setVisibility(View.VISIBLE);
                flashOn.setVisibility(View.VISIBLE);
                bottomBar.setBackgroundColor(getResources().getColor(R.color.transparent));
                selectAudioView.setVisibility(View.VISIBLE);
                selectIcon.setVisibility(View.VISIBLE);
                //closeCam.setVisibility(View.VISIBLE);
                onRecordingStopped();
            }
        });

        selectAudioView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAudio(v);
                selectAudioView.setClickable(false);
            }
        });

        flashOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                   flashOn.setVisibility(View.GONE);
                   flashOff.setVisibility(View.VISIBLE);
                   if(mIsRecording){
                       toggleFlashLight(false);
                   }
                   hasFlash = false;
                } else {
                    Toast.makeText(CameraActivity.this, "Your device doesn't have a flash camera..", Toast.LENGTH_SHORT).show();
                }

            }
        });

        flashOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    flashOn.setVisibility(View.VISIBLE);
                    flashOff.setVisibility(View.GONE);
                    if(mIsRecording){
                        toggleFlashLight(true);
                    }
                    hasFlash = true;
                } else {
                    Toast.makeText(CameraActivity.this, "Your device doesn't have a flash camera..", Toast.LENGTH_SHORT).show();
                }

            }
        });

        startRecordBtn = findViewById(R.id.videoOnlineImageButton);
        recordingBtn = findViewById(R.id.videoRecording);

        recordingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    onRecordingStopped();
                    animateViewFadeOut(recordingBtn);
                    animateViewFadeIn(startRecordBtn);
                    mode = "earlyStop";
            }
        });

        startRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if(audioUri != null){
                        camera.setVideoMaxDuration(getAudioDuration());
                        mIsRecording = true;
                        checkWriteStoragePermission();
                        }else{
                        Toast.makeText(CameraActivity.this,"Select audio before recording.",Toast.LENGTH_LONG).show();
                        }

            }
        });


        flipCam.setVisibility(View.VISIBLE);
        //CHANGE CAMERA - FRONT/BACK
        flipCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
                    if (camera.getFacing() == Facing.FRONT) {
                        camera.setFacing(Facing.BACK);
                        if(hasFlash){
                            flashOn.setVisibility(View.VISIBLE);
                            flashOff.setVisibility(View.GONE);
                        }else{
                            flashOn.setVisibility(View.GONE);
                            flashOff.setVisibility(View.VISIBLE);
                        }

                    } else {
                        camera.setFacing(Facing.FRONT);
                        flashOn.setVisibility(View.GONE);
                        flashOff.setVisibility(View.GONE);
                    }
                    rotateFlipCameraIcon();
                } else {
                    Toast.makeText(CameraActivity.this, "Your device doesn't have a front camera..", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void animateViewFadeOut(View view){
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(fadeOut);
    }

    private void animateViewFadeIn(View view){
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(fadeIn);
    }

    private void rotateFlipCameraIcon(){
        flipCam.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.rotate) );
    }

    private void sendIntent(VideoResult result){
        VideoPreview.setVideoResult(result);
        Log.d("cameraSize", result.getSize().toString());

        Intent intent = new Intent(CameraActivity.this, VideoPreview.class);
        intent.putExtra("mode",mode);
        intent.putExtra("audioUri",audioUri.toString());
        if(Build.VERSION.SDK_INT>20){
            ActivityOptions options =
                    ActivityOptions.makeSceneTransitionAnimation(this);
            startActivity(intent,options.toBundle());
        }else {
            startActivity(intent);
        }
    }

    private void animateTextView(){
        selectAudioText.setText(R.string.audio_selected);
    }

    private void openAudio(View v){
        Intent i = new Intent(this, AudioFileRecycleView.class);
        //i.putExtra("mode","audio");
        startActivityForResult(i,REQUEST_AUDIO);
    }

    @Override
    public void onBackPressed() {
    // Not calling **super**, disables back button in current screen.
        if(!mIsRecording){
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        selectAudioView.setClickable(true);
        if(audioUri != null) {
            initializePlayer();
            animateTextView();
        }
    }

    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkPermission()){
        camera.open();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(player != null){
            player.stop();
        }
        camera.close();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.destroy();
        if(player != null){
            player.release();

        }
    }

    public int getAudioDuration(){
        MediaPlayer mp  = new MediaPlayer();
        try {
            mp.setDataSource(this, audioUri);
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mp.getDuration();
    }

    private void captureVideo() {
        //camera.takeVideo(new File(mVideoFileName));
        if (!camera.isTakingVideo() && isAudioReady) {
            camera.takeVideo(new File(mVideoFileName)); //zacne zaznamenavat video
            player.setPlayWhenReady(true); //zacne prehravat audio cez exoplayer
        }else{
            Toast.makeText(this, "Audio is not prepared yet.", Toast.LENGTH_SHORT).show();

        }
    }

    private void onRecordingStopped(){
        mChronometer.getHandler().post(new Runnable() {
            public void run() {
                mChronometer.setVisibility(View.INVISIBLE);
            }
        });
        mIsRecording = false;
        camera.stopVideo();
        player.stop();
        player.release();
    }

        private void checkWriteStoragePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(mIsRecording) {
                    captureVideo();
                    flipCam.setVisibility(View.GONE);
                    //closeCam.setVisibility(View.GONE);
                    animateViewFadeOut(startRecordBtn); //GONE
                    animateViewFadeIn(recordingBtn); //VISIBLE
                    bottomBar.setBackgroundColor(getResources().getColor(R.color.invisible));
                    if(camera.getFacing() != Facing.BACK){
                        flashOn.setVisibility(View.GONE);
                        flashOff.setVisibility(View.GONE);
                    }
                    selectAudioView.setVisibility(View.GONE);
                    selectIcon.setVisibility(View.GONE);
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.setVisibility(View.VISIBLE);
                    mChronometer.start();
                }
            } else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "app needs to be able to save videos", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
        } else {
            try {
                createVideoFileName();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(mIsRecording) {
                captureVideo();
                flipCam.setVisibility(View.GONE);
                //closeCam.setVisibility(View.GONE);
                animateViewFadeOut(startRecordBtn); //GONE
                animateViewFadeIn(recordingBtn); //VISIBLE
                bottomBar.setBackgroundColor(getResources().getColor(R.color.invisible));
                if(camera.getFacing() != Facing.BACK){
                    flashOn.setVisibility(View.GONE);
                    flashOff.setVisibility(View.GONE);
                }
                selectAudioView.setVisibility(View.GONE);
                selectIcon.setVisibility(View.GONE);
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.setVisibility(View.VISIBLE);
                mChronometer.start();
            }
        }
    }

    private void createVideoFolder() {
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        mVideoFolder = root;
        if(!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    private File createVideoFileName() throws IOException {
        @SuppressLint("SimpleDateFormat") String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "VIDEO_" + timestamp + "_";
        File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
        mVideoFileName = videoFile.getAbsolutePath();
        return videoFile;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean valid = true;
        for (int grantResult : grantResults) {
            valid = valid && grantResult == PackageManager.PERMISSION_GRANTED;
        }
        if (valid && !camera.isOpened()) {
            camera.open();
        }
    }

    private ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.i(TAG,"onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.i(TAG,"onLoadingChanged");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.i(TAG,"onPlayerStateChanged: playWhenReady = "+(playWhenReady)
                    +" playbackState = "+playbackState);
            switch (playbackState){
                case ExoPlayer.STATE_ENDED:
                    Log.i(TAG,"Playback ended!");
                    //Stop playback and return to start position
                    break;
                case ExoPlayer.STATE_READY:
                    Log.i(TAG,"State ready!");
                    isAudioReady = true;
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    Log.i(TAG,"Playback buffering!");
                    break;
                case ExoPlayer.STATE_IDLE:
                    Log.i(TAG,"ExoPlayer idle!");
                    break;
            }
        }

    };

    private void initializePlayer(){
        // Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        //Initialize the player
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "AudioPlayerInCamera"));

        MediaSource audio = new ExtractorMediaSource(audioUri, dataSourceFactory, new DefaultExtractorsFactory(), null, null);

        player.addListener(eventListener);
        player.prepare(audio);

    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == REQUEST_AUDIO && requestCode == REQUEST_AUDIO) {
            audioUri = Uri.parse(intent.getStringExtra("audioUri"));
            //Toast.makeText(CameraActivity.this,"Select audio before recording." + intent.getStringExtra("audioUri"),Toast.LENGTH_LONG).show();
        }
    }



}
