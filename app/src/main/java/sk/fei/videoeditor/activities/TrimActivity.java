package sk.fei.videoeditor.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import sk.fei.videoeditor.R;

public class TrimActivity extends AppCompatActivity {

    Uri uri;
    String TAG = "TrimActivity";
    private SimpleExoPlayerView playerView;
    private SimpleExoPlayer player;
    private TextView textViewLeft, textViewRight, textViewCenter;
    private boolean initValues = false;
    MediaSource videoSource;

    String mode;
    int duration;
    String[] command;
    File dest;
    String originalPath;
    String audioPath;
    Uri audioUri;
    CrystalRangeSeekbar rangeSeekBar;

    int minValueChanged = 0;
    int maxValueChanged = 0;

    File mVideoFolder;
    String mVideoFileName;

    private int startMs;
    private int endMs;

    public int getStartMs() {
        return startMs;
    }

    public void setStartMs(int startMs) {
        this.startMs = startMs;
    }

    public int getEndMs() {
        return endMs;
    }

    public void setEndMs(int endMs) {
        this.endMs = endMs;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initLayoutControls();

        Intent i = getIntent();
        if (i != null) {
            uri = Uri.parse(i.getStringExtra("uri"));
            mode = i.getStringExtra("mode");
            audioUri = Uri.parse(i.getStringExtra("audioUri"));
        }
    }

    private void initLayoutControls() {
        textViewLeft = findViewById(R.id.tvvLeft);
        textViewRight = findViewById(R.id.tvvRight);
        textViewCenter = findViewById(R.id.tvDuration);
        rangeSeekBar = findViewById(R.id.rangeSeekbar1);

        createVideoFolder();
        // set listener
        rangeSeekBar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                int gap= maxValue.intValue()-minValue.intValue();

                if((minValue.intValue() != minValueChanged)){
                    player.seekTo((minValue).longValue());
                    minValueChanged = minValue.intValue();
                }

                textViewLeft.setText(getTime(minValue.intValue()));
                textViewRight.setText(getTime(maxValue.intValue()));
                textViewCenter.setText(getTime(gap));

            }
        });


// set final value listener
        rangeSeekBar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
                Log.d("CRS=>", minValue + " : " + maxValue);
            }
        });

    }

    @SuppressLint("DefaultLocale")
    public String getTime(int miliseconds) {

        int ms = miliseconds % 1000;
        int rem = miliseconds / 1000;
        int hr = rem / 3600;
        int remHr = rem % 3600;
        int mn = remHr / 60;
        int sec = remHr % 60;

//        if(hr==0){
//            return  String.format("%02d", mn) + ':' + String.format("%02d", sec)+ ':' + String.format("%03d", ms);
//        }
        return String.format("%02d", hr) + ':' + String.format("%02d", mn) + ':' + String.format("%02d", sec)+ ':' + String.format("%03d", ms);

    }

//    @Override
//    public void onBackPressed() {
//        Log.d("trim", "onBackPressed Called");
//        Intent setIntent = new Intent(Intent.ACTION_MAIN);
//        setIntent.addCategory(Intent.CATEGORY_HOME);
//        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(setIntent);
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.homeAsUp :
                Log.d(TAG,"homeAsUP");
                onBackPressed();
                break;
            case R.id.trim :
                try {
                    trimVideo(rangeSeekBar.getSelectedMinValue().intValue(),
                            rangeSeekBar.getSelectedMaxValue().intValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent myintent = new Intent(TrimActivity.this, VideoActivity.class);
                myintent.putExtra("duration", duration);
                myintent.putExtra("startMs", getStartMs());
                myintent.putExtra("endMs", getEndMs());
                myintent.putExtra("command", command);
                myintent.putExtra("audioUri", audioUri.toString());
                myintent.putExtra("videoUri", uri.toString());
                myintent.putExtra("mode", "trimmedVideo");
                myintent.putExtra("destination", dest.getAbsolutePath());
                startActivity(myintent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void trimVideo(int startMs, int endMs) throws IOException {

        setStartMs(startMs);
        setEndMs(endMs);

        dest = createVideoFileName();
        duration = (endMs - startMs);

        Log.d("jakubko", audioUri.getPath() +" ..... "+ uri.getPath() + "............."+ dest.getAbsolutePath());

        //command = new String[]{"-ss", getTime(startMs), "-y", "-i", uri.getPath(),"-to",getTime(endMs), "-i", audioUri.getPath(), "-r","30", "-c:v", "copy", "-c:a", "aac", "-shortest", dest.getAbsolutePath()};
            //command = new String[]{"-ss", "" + getTime(startMs), "-y", "-i", originalPath, "-t", "" + getTime(duration), "-c:v", "libx264", "-preset", "ultrafast", "-crf", "17", "-c:a", "copy", dest.getAbsolutePath()};
            //command = new String[]{"-ss", "" + getTime(startMs), "-y", "-i", originalPath, "-t", "" + getTime(duration), "-r" , "30", "-filter:v", "setpts=(1/3)*PTS", dest.getAbsolutePath()};

        command = new String[]{"-i", uri.getPath(), "-i", audioUri.getPath(), "-map", "0:v", "-map", "1:a", "-c", "copy", "-shortest", dest.getAbsolutePath()};

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
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
    protected void onStart() {
        super.onStart();
        createPlayers();
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
                    //setPlayPause(false);
                    //player.seekTo(0);
                    break;
                case ExoPlayer.STATE_READY:
                    Log.i(TAG,"ExoPlayer ready! pos: "+player.getCurrentPosition()
                            +" max: "+getTime((int)player.getDuration()));
                    //setProgress();
                    if(!initValues){
                        initRangeSeekBarValues();
                        initValues = true;
                    }

                    //setRangeSeekBar();
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

    @Override

    public void onPause() {
        super.onPause();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void createPlayers() {
        if (player != null) {
            player.release();
        }
        player = createFullPlayer();
        playerView = findViewById(R.id.exoplayer);
        playerView.setPlayer(player);

    }

    private SimpleExoPlayer createFullPlayer() {
        // Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        LoadControl loadControl = new DefaultLoadControl();
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        //Initialize the player

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "CloudinaryExoplayer"));

        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        // This is the MediaSource representing the media to be played.
        videoSource = new ExtractorMediaSource(uri,
                dataSourceFactory, extractorsFactory, null, null);


        player.addListener(eventListener);
        player.setPlayWhenReady(true);
        player.prepare(videoSource);

        return player;
    }


    public void initRangeSeekBarValues(){
        textViewLeft.setText(getTime((int) player.getCurrentPosition()));
        textViewRight.setText(getTime((int) player.getDuration()));
        textViewCenter.setText(getTime((int) player.getDuration()));

        rangeSeekBar.setMaxValue((int) player.getDuration());
        rangeSeekBar.setMinValue(0);
        rangeSeekBar.setEnabled(true);

        maxValueChanged = (int)player.getDuration();
    }

}

