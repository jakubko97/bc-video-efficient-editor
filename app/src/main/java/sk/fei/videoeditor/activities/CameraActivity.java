package sk.fei.videoeditor.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
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
import com.google.android.material.button.MaterialButton;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Audio;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Mode;
import com.otaliastudios.cameraview.controls.VideoCodec;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import sk.fei.videoeditor.R;

public class CameraActivity extends AppCompatActivity {

    File output;
    private String TAG = "cameraActivity";
    private String mode = "maxDurationReached";
    private CameraView camera;
    private MaterialButton mRecordImageButton, flipCam, flash;
    private Chronometer mChronometer;

    private SimpleExoPlayer player;

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
    private final static boolean USE_FRAME_PROCESSOR = true;

    private boolean mIsRecording = false;
    private final static boolean DECODE_BITMAP = false;

    private File mVideoFolder;
    private String mVideoFileName;
    private Uri audioUri;

    private boolean isAudioReady = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_video);
        camera = findViewById(R.id.camera);
        camera.setLifecycleOwner(this);
        camera.setMode(Mode.VIDEO);
        camera.setAudio(Audio.OFF);
        camera.setVideoCodec(VideoCodec.H_264);
        camera.setFlash(Flash.TORCH);

        mChronometer = findViewById(R.id.chronometer);
        flipCam = findViewById(R.id.flip_facing_camera);
        flash = findViewById(R.id.flash);

        createVideoFolder();

        Intent i = getIntent();

        if (i != null) {

                audioUri = Uri.parse(i.getStringExtra("audioUri"));
                camera.setVideoMaxDuration(getAudioDuration());
        }

        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    if (camera.getFlash() == Flash.ON) {
                        camera.setFlash(Flash.OFF);
                        flash.setBackgroundResource(R.drawable.flash_off_foreground);
                    } else {
                        camera.setFlash(Flash.ON);
                        flash.setBackgroundResource(R.drawable.flash_foreground);

                    }
                } else {
                    Toast.makeText(CameraActivity.this, "Your device doesn't have a flash camera..", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mRecordImageButton = findViewById(R.id.videoOnlineImageButton);
        mRecordImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRecording) {
                    onRecordingStopped();
                    mode = "earlyStop";

                } else {
                    mIsRecording = true;
                    mRecordImageButton.setBackgroundResource(R.drawable.btn_video_busy_foreground);
                    checkWriteStoragePermission();
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
                    } else {
                        camera.setFacing(Facing.FRONT);
                    }
                } else {
                    Toast.makeText(CameraActivity.this, "Your device doesn't have a front camera..", Toast.LENGTH_SHORT).show();
                }

            }
        });

        if (USE_FRAME_PROCESSOR) {
            camera.addFrameProcessor(new FrameProcessor() {
                private long lastTime = System.currentTimeMillis();

                @Override
                public void process(@NonNull Frame frame) {
                    long newTime = frame.getTime();
                    long delay = newTime - lastTime;
                    lastTime = newTime;
                    //Log.v("asdasd","Frame delayMillis:", delay, "FPS:", 1000 / delay);
                    if (DECODE_BITMAP) {
                        if (frame.getFormat() == ImageFormat.NV21
                                && frame.getDataClass() == byte[].class) {
                            byte[] data = frame.getData();
                            YuvImage yuvImage = new YuvImage(data,
                                    frame.getFormat(),
                                    frame.getSize().getWidth(),
                                    frame.getSize().getHeight(),
                                    null);
                            ByteArrayOutputStream jpegStream = new ByteArrayOutputStream();
                            yuvImage.compressToJpeg(new Rect(0, 0,
                                    frame.getSize().getWidth(),
                                    frame.getSize().getHeight()), 100, jpegStream);
                            byte[] jpegByteArray = jpegStream.toByteArray();
                            Bitmap bitmap = BitmapFactory.decodeByteArray(jpegByteArray,
                                    0, jpegByteArray.length);
                            //noinspection ResultOfMethodCallIgnored
                            bitmap.toString();
                        }
                    }
                }
            });
        }

        camera.addCameraListener(new CameraListener() {
            @Override
            public void onVideoTaken(@NonNull VideoResult result) {
                super.onVideoTaken(result);
                Log.w("daAD","onVideoTaken called! Launching activity.");
                VideoActivity.setVideoResult(result);
                Intent intent = new Intent(CameraActivity.this, VideoActivity.class);
                intent.putExtra("mode",mode);
                intent.putExtra("audioUri",audioUri.toString());
                startActivity(intent);
                Log.w("asf","onVideoTaken called! Launched activity.");
            }

            @Override
            public void onVideoRecordingStart() {
                // Notifies that the actual video recording has started.
                // Can be used to show some UI indicator for video recording or counting time.
            }

            @Override
            public void onVideoRecordingEnd() {
                // Notifies that the actual video recording has ended.
                // Can be used to remove UI indicators added in onVideoRecordingStart.
                flipCam.setVisibility(View.VISIBLE);
                flash.setVisibility(View.VISIBLE);

                onRecordingStopped();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        initializePlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.close();
        player.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.destroy();
        player.release();
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
        mRecordImageButton.getHandler().post(new Runnable() {
            public void run() {
                mRecordImageButton.setBackgroundResource(R.drawable.btn_video_online_foreground);
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
                    flash.setVisibility(View.GONE);
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
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.setVisibility(View.VISIBLE);
                mChronometer.start();
            }
        }
    }

    private void createVideoFolder() {
        File root = Environment.getExternalStorageDirectory();
        mVideoFolder = new File(root, "My Video Editor");
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
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "CloudinaryExoplayer"));

        MediaSource audio = new ExtractorMediaSource(audioUri, dataSourceFactory, new DefaultExtractorsFactory(), null, null);

        player.addListener(eventListener);
        player.prepare(audio);

    }



}
