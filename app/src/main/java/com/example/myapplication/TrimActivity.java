package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
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
import android.widget.VideoView;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;

public class TrimActivity extends AppCompatActivity {

    Uri uri;
    private VideoView vv;
    private ImageView imageView;
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

        vv = (VideoView) findViewById(R.id.videoView);
        imageView = (ImageView) findViewById(R.id.imageView);
        textViewLeft = (TextView) findViewById(R.id.tvvLeft);
        textViewRight = (TextView) findViewById(R.id.tvvRight);
        rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);

        Intent i = getIntent();

        if(i!=null){
            String imgPath = i.getStringExtra("uri");
            uri = Uri.parse(imgPath);
            vv.setVideoURI(uri);
            vv.start();
            isPlaying = true;
        }

        setListeners();


        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {
                vv.start();

                duration = vv.getDuration();

                textViewLeft.setText(getTime(vv.getCurrentPosition()));
                textViewRight.setText(getTime(vv.getDuration()));

                rangeSeekBar.setRangeValues(0,vv.getDuration());
                rangeSeekBar.setSelectedMaxValue(vv.getDuration());
                rangeSeekBar.setSelectedMinValue(0);
                rangeSeekBar.setEnabled(true);

                rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                        vv.seekTo((int)minValue);


                        textViewLeft.setText(getTime((int)bar.getSelectedMinValue()));
                        textViewRight.setText(getTime((int)bar.getSelectedMaxValue()));
                    }
                });

                myHandler.postDelayed(UpdateSongTime,100);
            }
        });

    }

    public String getTime(int miliseconds){

        int ms = miliseconds % 1000;
        int rem = miliseconds / 1000;
        int mn = rem / 60;
        int sec = rem % 60;
        int hr = rem / 3600;

        return String.format("%02d",hr) + ':' + String.format("%02d",mn) + ':' + String.format("%02d",sec) + '.' + String.format("%03d",ms);

    }

    private void setListeners() {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying){
                    imageView.setImageResource(R.drawable.ic_play_foreground);
                    vv.pause();
                    isPlaying = false;
                }else {
                    vv.start();
                    imageView.setImageResource(R.drawable.ic_pause);
                    isPlaying = true;
                    //myHandler.postDelayed(UpdateImageView,1500);
                }

            }
        });
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            textViewLeft.setText(getTime(vv.getCurrentPosition()));

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

            vv.pause();

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
}

