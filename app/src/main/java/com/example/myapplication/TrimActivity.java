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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
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
    private ImageView imageView;
    SimpleExoPlayer player;
    private TextView textViewLeft, textViewRight;
    private boolean isPlaying;
    private Handler myHandler = new Handler();
    private RangeSeekBar rangeSeekBar;

    int duration;
    String filePrefix;
    String[] command;
    File dest;
    String originalPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim);

        //vv = (VideoView) findViewById(R.id.videoView);
        imageView = (ImageView) findViewById(R.id.imageView);
        textViewLeft = (TextView) findViewById(R.id.tvvLeft);
        textViewRight = (TextView) findViewById(R.id.tvvRight);
        rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);

        Intent i = getIntent();

        if(i!=null){
            String imgPath = i.getStringExtra("uri");
            uri = Uri.parse(imgPath);

            isPlaying = true;
        }

    }

    public String getTime(int miliseconds){

        int ms = miliseconds % 1000;
        int rem = miliseconds / 1000;
        int mn = rem / 60;
        int sec = rem % 60;
        int hr = rem / 3600;

        return String.format("%02d",hr) + ':' + String.format("%02d",mn) + ':' + String.format("%02d",sec) + '.' + String.format("%03d",ms);

    }

//    private void setListeners() {
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(isPlaying){
//                    imageView.setImageResource(R.drawable.ic_play_foreground);
//                    player.pause();
//                    isPlaying = false;
//                }else {
//                    player.start();
//                    imageView.setImageResource(R.drawable.ic_pause);
//                    isPlaying = true;
//                    //myHandler.postDelayed(UpdateImageView,1500);
//                }
//
//            }
//        });
//    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            textViewLeft.setText(getTime((int)player.getCurrentPosition()));

            myHandler.postDelayed(this, 100);
        }
    };

    private Runnable UpdateImageView = new Runnable() {
        public void run() {

            imageView.setVisibility(View.INVISIBLE);
            myHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.trim){


            final AlertDialog.Builder alert = new AlertDialog.Builder(TrimActivity.this);

            LinearLayout linearLayout = new LinearLayout(TrimActivity.this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(50,0,100,0);
            final EditText input = new EditText(TrimActivity.this);
            input.setLayoutParams(lp);
            input.setGravity(Gravity.TOP|Gravity.START);
            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            linearLayout.addView(input,lp);

            alert.setMessage("Set video name");
            alert.setTitle("Change video name");
            alert.setView(linearLayout);
            alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            alert.setPositiveButton("submit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    filePrefix = input.getText().toString();

                    trimVideo(rangeSeekBar.getSelectedMinValue().intValue(),
                            rangeSeekBar.getSelectedMaxValue().intValue(), filePrefix);

                    Intent myintent = new Intent(TrimActivity.this,ProgressBarActivity.class);
                    myintent.putExtra("duration",duration);
                    myintent.putExtra("command",command);
                    myintent.putExtra("destination",dest.getAbsolutePath());
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

        File folder = new File(Environment.getExternalStorageDirectory() + "/TrimVideos");
        if(!folder.exists()){

            folder.mkdir();
        }

        filePrefix = fileName;

        String fileExt =  ".mp4";
        dest = new File(folder,filePrefix + fileExt);
        originalPath = getRealPathFromUri(getApplicationContext(),uri);

        duration = (endMs - startMs);
        Log.d("DEBUG","Start Ms:"+ startMs + "\nEnd Ms:"+ endMs + "\nDuration:" + duration);
        Toast.makeText(getApplicationContext(),getTime(duration),Toast.LENGTH_LONG).show();
        command = new String[]{"-ss",""+getTime(startMs) , "-y", "-i", originalPath,"-t",""+getTime(duration),"-filter:v","fps=fps=30", dest.getAbsolutePath()};

    }

    private String getRealPathFromUri(Context context, Uri contentUri) {

        Cursor cursor = null;

        try{

            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri,proj,null,null,null);

            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        catch(Exception e){
            e.printStackTrace();
            return "";
        }finally{
            if(cursor!=null){
                cursor.close();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializePlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player!=null) {
            player.release();
            player = null;
        }
    }

    private void initializePlayer(){
        // Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        //Initialize the player
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);


        //Initialize simpleExoPlayerView
        SimpleExoPlayerView simpleExoPlayerView = findViewById(R.id.exoplayer);
        simpleExoPlayerView.setPlayer(player);

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "CloudinaryExoplayer"));

        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        // This is the MediaSource representing the media to be played.

        MediaSource videoSource = new ExtractorMediaSource(uri,
                dataSourceFactory, extractorsFactory, null, null);


        duration = (int)player.getDuration();
        Log.d("jakubko","duration: "+ duration + "long :" + player.getDuration());

        textViewLeft.setText(getTime((int)player.getCurrentPosition()));
        textViewRight.setText(getTime(duration));

        rangeSeekBar.setRangeValues(0,(int)player.getDuration());
        rangeSeekBar.setSelectedMaxValue((int)player.getDuration());
        rangeSeekBar.setSelectedMinValue(0);
        rangeSeekBar.setEnabled(true);

        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                //player.seekTo((long)minValue);

                textViewLeft.setText(getTime((int)bar.getSelectedMinValue()));
                textViewRight.setText(getTime((int)bar.getSelectedMaxValue()));

                Toast.makeText(getApplicationContext(),"min value: "+(int)minValue + "\nmax value: "+ (int)maxValue,Toast.LENGTH_LONG).show();
            }
        });


        myHandler.postDelayed(UpdateSongTime,100);

        // Prepare the player with the source.
        player.prepare(videoSource);

    }
}

