package com.example.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class VideoActivity extends AppCompatActivity {

    Uri uri;
    private Button btnonce, btnstop, btnplay, btntrim;
    private VideoView vv;
    private MediaController mediacontroller;

    private boolean isContinuously = false;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        Intent i = getIntent();

        if(i!=null){
            String imgPath = i.getStringExtra("uri");
            uri = Uri.parse(imgPath);
        }

        progressBar = (ProgressBar) findViewById(R.id.progrss);
        btnonce = (Button) findViewById(R.id.btnonce);
        btnstop = (Button) findViewById(R.id.btnstop);
        btnplay = (Button) findViewById(R.id.btnplay);
        btntrim = (Button) findViewById(R.id.btntrim);
        vv = (VideoView) findViewById(R.id.vv);

        mediacontroller = new MediaController(this);
        mediacontroller.setAnchorView(vv);

        btnstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vv.pause();
            }
        });

        btntrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(VideoActivity.this,TrimActivity.class);
                i.putExtra("uri",uri.toString());
                startActivity(i);
            }
        });


        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vv.start();
            }
        });

        btnonce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isContinuously = false;
                progressBar.setVisibility(View.VISIBLE);
                vv.setMediaController(mediacontroller);
                vv.setVideoURI(uri);
                vv.requestFocus();
                vv.start();
            }
        });


        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }
}
