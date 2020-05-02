package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Mode;
import com.otaliastudios.cameraview.controls.VideoCodec;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;
import com.otaliastudios.cameraview.size.SizeSelector;
import com.otaliastudios.cameraview.size.SizeSelectors;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {

    File output;
    private CameraView camera;
    private ImageButton mRecordImageButton, flipCam;
    private Chronometer mChronometer;

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
    private final static boolean USE_FRAME_PROCESSOR = true;

    private boolean mIsRecording = false;
    private final static boolean DECODE_BITMAP = false;

    private File mVideoFolder;
    private String mVideoFileName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_video);
        camera = findViewById(R.id.camera);
        camera.setLifecycleOwner(this);
        camera.setMode(Mode.VIDEO);
        camera.setVideoCodec(VideoCodec.H_264);

        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        flipCam = findViewById(R.id.flip_facing_camera);

        createVideoFolder();

        mRecordImageButton = (ImageButton) findViewById(R.id.videoOnlineImageButton);
        mRecordImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRecording) {
                    onRecordingStopped();

                } else {
                    mIsRecording = true;
                    mRecordImageButton.setImageResource(R.mipmap.btn_video_busy);
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
                intent.putExtra("mode","captureVideo");
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
            }
        });

// Select output file. Make sure you have write permissions.


// Later... stop recording. This will trigger onVideoTaken().

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.destroy();
    }

    private void captureVideo() {
        //camera.takeVideo(new File(mVideoFileName));
        if (!camera.isTakingVideo()) {
            camera.takeVideo(new File(mVideoFileName));
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
                mRecordImageButton.setImageResource(R.mipmap.btn_video_online);
            }
        });
        mIsRecording = false;
        camera.stopVideo();
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
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        mVideoFolder = new File(movieFile, "video_editor_bakalarka");
        if(!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    private File createVideoFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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

}
