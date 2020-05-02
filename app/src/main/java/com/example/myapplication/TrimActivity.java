package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;

public class TrimActivity extends AppCompatActivity {

    Uri uri;
    String TAG = "jakubko";
    private SimpleExoPlayerView playerView;
    private SimpleExoPlayer player;
    private TextView textViewLeft, textViewRight, textViewCenter;
    private boolean isPlaying;
    private boolean initValues = false;
    private Handler myHandler = new Handler();
    private RangeSeekBar rangeSeekBar;
    MediaSource videoSource;

    String mode;
    int duration;
    String filePrefix;
    String[] command;
    File dest;
    String originalPath;
    String audioPath;
    Uri audioUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim);

        textViewLeft = (TextView) findViewById(R.id.tvvLeft);
        textViewRight = (TextView) findViewById(R.id.tvvRight);
        textViewCenter = (TextView) findViewById(R.id.tvDuration);
        rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);

        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {

            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                player.seekTo(((Number)minValue).longValue());

                Log.d("rangeSeekBar", "minValue: " + minValue + "maxValue :" + maxValue);

                textViewLeft.setText(getTime(bar.getSelectedMinValue().intValue()));
                textViewRight.setText(getTime(bar.getSelectedMaxValue().intValue()));
                textViewCenter.setText(getTime(bar.getSelectedMaxValue().intValue()-bar.getSelectedMinValue().intValue()));
            }
        });


        Intent i = getIntent();

        if (i != null) {

            uri = Uri.parse(i.getStringExtra("uri"));
            mode = i.getStringExtra("mode");

            if(mode.equals("withAudio")){
                audioUri = Uri.parse(i.getStringExtra("audioUri"));
            }



            isPlaying = true;
        }

    }

    public String getTime(int miliseconds) {

        int ms = miliseconds % 1000;
        int rem = miliseconds / 1000;
        int mn = rem / 60;
        int sec = rem % 60;
        int hr = rem / 3600;

        return String.format("%02d", hr) + ':' + String.format("%02d", mn) + ':' + String.format("%02d", sec) + '.' + String.format("%03d", ms);

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.trim) {

            final AlertDialog.Builder alert = new AlertDialog.Builder(TrimActivity.this);

            LinearLayout linearLayout = new LinearLayout(TrimActivity.this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(50, 0, 100, 0);
            final EditText input = new EditText(TrimActivity.this);
            input.setLayoutParams(lp);
            input.setGravity(Gravity.TOP | Gravity.START);
            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            linearLayout.addView(input, lp);

            alert.setMessage("Nastav názov videa");
            alert.setTitle("Zmeň názov videa");
            alert.setView(linearLayout);
            alert.setNegativeButton("zruš", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            alert.setPositiveButton("odoslať", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    filePrefix = input.getText().toString();

                    trimVideo(rangeSeekBar.getSelectedMinValue().intValue(),
                            rangeSeekBar.getSelectedMaxValue().intValue(), filePrefix);

                    Intent myintent = new Intent(TrimActivity.this, VideoActivity.class);
                    myintent.putExtra("duration", duration);
                    myintent.putExtra("command", command);
                    myintent.putExtra("mode", "trimmedVideo");
                    myintent.putExtra("destination", dest.getAbsolutePath());
                    startActivity(myintent);

                    finish();
                    dialog.dismiss();

                }
            });

            alert.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void trimVideo(int startMs, int endMs, String fileName) {

        File folder = new File(Environment.getExternalStorageDirectory() + "/VideoEditor");
        if (!folder.exists()) {
            folder.mkdir();
        }

        filePrefix = fileName;

        String fileExt = ".mp4";
        dest = new File(folder, filePrefix + fileExt);
        originalPath = getRealPathFromUri(getApplicationContext(), uri);
        audioPath = getRealPathFromUri(getApplicationContext(), audioUri);


        duration = (endMs - startMs);

        Log.d("jakubko", "Start Ms:" + startMs + "\nEnd Ms:" + endMs + "\nDuration:" + duration);
        Toast.makeText(getApplicationContext(), getTime(duration), Toast.LENGTH_LONG).show();

        if(audioUri != null ){
            command = new String[]{"-i", audioPath, "-ss", getTime(startMs), "-i", originalPath,"-t", "" + getTime(duration), "-r","30", "-c:v", "copy", "-c:a", "aac", "-shortest", dest.getAbsolutePath()};
        }else{
            //zvolenie strihanie audio/video suboru, ale bez pridaneho audia

            command = new String[]{"-ss", "" + getTime(startMs), "-y", "-i", originalPath, "-t", "" + getTime(duration), "-c:v", "libx264", "-preset", "ultrafast", "-crf", "17", "-c:a", "copy", dest.getAbsolutePath()};
            //command = new String[]{"-ss", "" + getTime(startMs), "-y", "-i", originalPath, "-t", "" + getTime(duration), "-r" , "30", "-filter:v", "setpts=(1/3)*PTS", dest.getAbsolutePath()};
        }

        //command = new String[]{"-i", originalPath, "-i", audioPath, "-map" ,"0:v", "-map", "1:a", "-c", "copy", "-shortest", "-filter:v" ,"setpts=0.5*PTS", dest.getAbsolutePath()};

    }

    private String getRealPathFromUri(Context context, Uri contentUri) {

        Cursor cursor = null;

        try {

            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);

            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
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
        Log.d("jakubko2", "current position: " + (int) player.getCurrentPosition() + "integer Duration :" + (int) player.getDuration());

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
        // Prepare the player with the source.
        player.prepare(videoSource);
        //myHandler.postDelayed(UpdateSongTime,100);
        return player;
    }

    private void setProgress() {

        if(myHandler == null)myHandler = new Handler();
        //Make sure you update Seekbar on UI thread
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                if (player != null && isPlaying) {

                    textViewLeft.setText(getTime((int) player.getCurrentPosition()));
                    myHandler.postDelayed(this, 100);
                }
            }
        });
    }

    public void initRangeSeekBarValues(){
        textViewLeft.setText(getTime((int) player.getCurrentPosition()));
        textViewRight.setText(getTime((int) player.getDuration()));

        rangeSeekBar.setRangeValues(0, (int) player.getDuration());
        rangeSeekBar.setSelectedMaxValue((int) player.getDuration());
        rangeSeekBar.setSelectedMinValue(0);
        rangeSeekBar.setEnabled(true);
    }

}

