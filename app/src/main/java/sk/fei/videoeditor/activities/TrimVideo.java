package sk.fei.videoeditor.activities;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import android.os.Environment;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import sk.fei.videoeditor.R;

public class TrimVideo extends AppCompatActivity {

    private static final int REQUEST_AUDIO_TRIM = 101;
    private static final int REQUEST_AUDIO = 100;
    Uri uri;
    String TAG = "TrimVideo";
    private SimpleExoPlayerView playerView;
    private SimpleExoPlayer player;
    private TextView textViewLeft, textViewRight, textViewCenter;
    private boolean initValues = false;
    MediaSource videoSource;
    MediaPlayer audioPlayer;

    int duration;
    String[] command;

    Uri audioUri;
    CrystalRangeSeekbar rangeSeekBar;
    ExtendedFloatingActionButton addAudio;

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
//            mode = i.getStringExtra("mode");
//            audioUri = Uri.parse(i.getStringExtra("audioUri"));
        }
    }


    private void initLayoutControls() {
        playerView = findViewById(R.id.exoplayer);
        textViewLeft = findViewById(R.id.tvvLeft);
        textViewRight = findViewById(R.id.tvvRight);
        textViewCenter = findViewById(R.id.tvDuration);
        rangeSeekBar = findViewById(R.id.rangeSeekbar1);
        addAudio = findViewById(R.id.addAudio);

        createVideoFolder();
        // set listener

        addAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAudio(v);
                //addAudio.setEnabled(false);
                addAudio.setClickable(false);
                addAudio.setAlpha(0.5f);
            }
        });

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

    private void openAudio(View v){
        Intent i = new Intent(this, AudioFileRecycleView.class);
        //i.putExtra("mode","audio");
        startActivityForResult(i,REQUEST_AUDIO_TRIM);
    }

    private MediaPlayer initMediaPlayer(){
        audioPlayer = new MediaPlayer();
        try {
            audioPlayer.setDataSource(audioUri.getPath());
            audioPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        audioPlayer.start();

        return audioPlayer;
    }

    @SuppressLint("DefaultLocale")
    public String getTime(int miliseconds) {

        int ms = miliseconds % 1000;
        int rem = miliseconds / 1000;
        int hr = rem / 3600;
        int remHr = rem % 3600;
        int mn = remHr / 60;
        int sec = remHr % 60;

        return String.format("%02d", hr) + ':' + String.format("%02d", mn) + ':' + String.format("%02d", sec)+ '.' + String.format("%03d", ms);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                supportFinishAfterTransition();
                break;
            case R.id.trim :
                item.setEnabled(false);
                try {
                    trimVideo(rangeSeekBar.getSelectedMinValue().intValue(),
                            rangeSeekBar.getSelectedMaxValue().intValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent myintent = new Intent(TrimVideo.this, VideoPreview.class);
                myintent.putExtra("duration", duration);
                myintent.putExtra("startMs", getStartMs());
                myintent.putExtra("endMs", getEndMs());
                myintent.putExtra("command", command);
                myintent.putExtra("audioUri", audioUri.toString());
                myintent.putExtra("videoUri", uri.toString());
                myintent.putExtra("mode", "trimmedVideo");
                myintent.putExtra("destination", mVideoFileName);
                if(Build.VERSION.SDK_INT>23){
                    //ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this,playerView, playerView.getTransitionName());
                    ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeBasic();
                    startActivity(myintent,activityOptionsCompat.toBundle());
                }else {
                    startActivity(myintent);
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void trimVideo(int startMs, int endMs) throws IOException {

        setStartMs(startMs);
        setEndMs(endMs);
        createVideoFileName();
        duration = (endMs - startMs);

        Log.d("jakubko", audioUri.getPath() +" ..... "+ uri.getPath() + "............."+ mVideoFileName);

        command = new String[]{"-i", audioUri.getPath(), "-ss", getTime(startMs), "-y", "-i", uri.getPath(),"-t",getTime(duration), "-r","25", "-c:v", "copy", "-c:a", "aac", "-shortest", mVideoFileName};
        //command = new String[]{"-ss", getTime(startMs), "-y", "-i", uri.getPath(),"-to",getTime(endMs), "-i", audioUri.getPath(), "-r","30", "-c:v", "copy", "-c:a", "aac", "-shortest", dest.getAbsolutePath()};
        //command = new String[]{"-ss", "" + getTime(startMs), "-y", "-i", originalPath, "-t", "" + getTime(duration), "-c:v", "libx264", "-preset", "ultrafast", "-crf", "17", "-c:a", "copy", dest.getAbsolutePath()};
        //command = new String[]{"-ss", "" + getTime(startMs), "-y", "-i", originalPath, "-t", "" + getTime(duration), "-r" , "30", "-filter:v", "setpts=(1/3)*PTS", dest.getAbsolutePath()};
        //command = new String[]{"-i", uri.getPath(), "-i", audioUri.getPath(), "-map", "0:v", "-map", "1:a", "-c", "copy", "-shortest", dest.getAbsolutePath()};

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(audioUri !=null){
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    private void createVideoFolder() {
        File root = Environment.getExternalStorageDirectory();
        mVideoFolder = new File(root, "My Video Editor");
        if(!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    private void createVideoFileName() {
        @SuppressLint("SimpleDateFormat") String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "VIDEO_" + timestamp + "_";
        String fileExt = ".mp4";
        File file = new File(mVideoFolder, prepend + fileExt);
        mVideoFileName  = file.getAbsolutePath();
    }

    @Override
    protected void onStart() {
        super.onStart();
        createPlayers();
        addAudio.setClickable(true);
        addAudio.setAlpha(1.0f);
        if(audioUri != null){
        invalidateOptionsMenu();
        updateFAB();
        }
    }

    private void updateFAB() {
        addAudio.setIcon(getDrawable(R.drawable.ic_mode_edit_white_24dp));
        addAudio.setText("EDIT");
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

            if(audioPlayer != null && playbackState != ExoPlayer.STATE_READY){
                audioPlayer.seekTo((int)player.getCurrentPosition());
            }

            switch (playbackState){
                case ExoPlayer.STATE_ENDED:
                    Log.i(TAG,"Playback ended!");
                    //Stop playback and return to start position
                    if(audioPlayer != null){
                        audioPlayer.pause();
                    }
                    break;
                case ExoPlayer.STATE_READY:
                    Log.i(TAG,"ExoPlayer ready! pos: "+player.getCurrentPosition()
                            +" max: "+getTime((int)player.getDuration()));
                    //setProgress();
                    if(!initValues){
                        initRangeSeekBarValues();
                        initValues = true;
                    }

                    if(audioPlayer != null && playWhenReady){
                        audioPlayer.start();
                    }else{
                        if(audioPlayer != null){
                            audioPlayer.pause();
                        }

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
        if (audioPlayer!=null) {
            audioPlayer.release();
            audioPlayer = null;
        }
    }

    private void createPlayers() {
        if (player != null) {
            player.release();
        }
        player = createFullPlayer();
        if(audioUri != null){
            audioPlayer = initMediaPlayer();
            setGap(audioPlayer, player);
        }

        playerView.findViewById(R.id.exo_close).setVisibility(View.GONE);
        playerView.findViewById(R.id.exo_share).setVisibility(View.GONE);
        playerView.findViewById(R.id.exo_delete).setVisibility(View.GONE);
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
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Trim Video"));

        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        // This is the MediaSource representing the media to be played.
        videoSource = new ExtractorMediaSource(uri,
                dataSourceFactory, extractorsFactory, null, null);


        player.addListener(eventListener);
        if(audioUri != null){
            player.setVolume(0f);
        }
        player.setPlayWhenReady(true);
        player.prepare(videoSource);


        return player;
    }

    public void initRangeSeekBarValues(){
        //textViewLeft.setText(getTime((int) player.getCurrentPosition()));
        textViewRight.setText(getTime((int) player.getDuration()));
        textViewCenter.setText(getTime((int) player.getDuration()));

        rangeSeekBar.setMaxValue((int) player.getDuration());
        rangeSeekBar.setMinValue(0);

        maxValueChanged = (int)player.getDuration();
    }

    private void setGap(MediaPlayer audioPlayer, SimpleExoPlayer player){
        if(audioUri != null){
            if(audioPlayer.getDuration() < player.getDuration()){
                rangeSeekBar.setMaxValue(audioPlayer.getDuration());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_AUDIO_TRIM && resultCode == REQUEST_AUDIO) {
            audioUri = Uri.parse(intent.getStringExtra("audioUri"));

            //Toast.makeText(Camera.this,"Select audio before recording." + intent.getStringExtra("audioUri"),Toast.LENGTH_LONG).show();
        }
    }


}

