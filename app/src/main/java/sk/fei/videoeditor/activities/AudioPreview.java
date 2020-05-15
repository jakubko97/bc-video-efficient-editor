package sk.fei.videoeditor.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import sk.fei.videoeditor.R;

public class AudioPreview extends AppCompatActivity {

    MediaPlayer mediaPlayer = new MediaPlayer();
    SeekBar seekBar;
    private Button play,confirm,storno;
    int position;
    String audioPath;
    File audioFile;
    TextView audioName, currentPosition, duration;
    Handler handler;
    Runnable runnable;
    ImageView videoPreviewImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_preview);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_left_to_right);

        play = findViewById(R.id.play);
        audioName = findViewById(R.id.audioName);
        confirm = findViewById(R.id.confirmAudio);
        storno = findViewById(R.id.stornoAudio);
        currentPosition = findViewById(R.id.currentPositionAudio);
        duration = findViewById(R.id.durationAudio);
        handler = new Handler();
        videoPreviewImage = findViewById(R.id.videoPreviewImage);

        Intent i = getIntent();

        if (i != null) {
            //position = i.getIntExtra("position",0);
            audioPath = i.getStringExtra("audioPath");
        }
        audioFile = new File(audioPath);

        audioName.setText(audioFile.getName().replace(".mp3",""));


        mediaPlayer = MediaPlayer.create(this,Uri.parse(audioFile.getAbsolutePath()));
        duration.setText(getTime(mediaPlayer.getDuration()));

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                seekBar.setMax(mp.getDuration());
                mediaPlayer.start();
                play.setBackgroundResource(R.drawable.pause_foreground);
                changeSeekBar();
            }
        });


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSong();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearMediaPlayer();
                Intent i = new Intent(AudioPreview.this,MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.putExtra("audioPath",audioFile.getAbsolutePath());
                startActivity(i);
            }
        });

        storno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        seekBar = findViewById(R.id.audioSeekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                if(fromTouch){
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void changeSeekBar() {
        if(mediaPlayer != null){
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        currentPosition.setText(getTime(mediaPlayer.getCurrentPosition()));

        if(mediaPlayer.isPlaying()){
        runnable = new Runnable() {
            @Override
            public void run() {
                changeSeekBar();
            }
        };
        handler.postDelayed(runnable,1000);
        }else{
            play.setBackgroundResource(R.drawable.play_foreground);
        }
        }
    }

    public void playSong() {

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                play.setBackgroundResource(R.drawable.play_foreground);
            }
            else {
                mediaPlayer.start();
                play.setBackgroundResource(R.drawable.pause_foreground);
                changeSeekBar();
            }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
    }

    private void clearMediaPlayer() {
        if(mediaPlayer != null){
        mediaPlayer.stop();
        mediaPlayer.release();
        runnable = null;
        mediaPlayer = null;
        }
    }

    public String getTime(int miliseconds) {

        int ms = miliseconds % 1000;
        int rem = miliseconds / 1000;
        int hr = rem / 3600;
        int remHr = rem % 3600;
        int mn = remHr / 60;
        int sec = remHr % 60;

        if(hr==0){
            return  String.format("%02d", mn) + ':' + String.format("%02d", sec);
        }
        return String.format("%02d", hr) + ':' + String.format("%02d", mn) + ':' + String.format("%02d", sec);

    }


}
