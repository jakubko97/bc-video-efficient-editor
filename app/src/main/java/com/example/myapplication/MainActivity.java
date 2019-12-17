package com.example.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static int VIDEO_REQUEST = 101;
    Uri selectedUri;
    private Button openVideo, captureVideo;
    MediaPlayer audio;
    int audioDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openVideo = (Button) findViewById(R.id.openVideo);
        captureVideo = (Button) findViewById(R.id.captureVideo);

        audio = MediaPlayer.create(this, R.raw.audios);
        audioDuration = audio.getDuration()/1000;

        openVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideo(v);
            }
        });

        captureVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureVideo(v);
            }
        });
    }

    public void captureVideo(View v){

        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        videoIntent.putExtra("android.intent.extra.durationLimit", audioDuration);

        if(videoIntent.resolveActivity(getPackageManager()) != null){

            startActivityForResult(videoIntent,VIDEO_REQUEST);

        }
    }

    public void openVideo(View v){

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("video/mp4");
        startActivityForResult(i,100);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == RESULT_OK){

            selectedUri = data.getData();

            Intent i = new Intent(MainActivity.this,VideoActivity.class);
            i.putExtra("uri",selectedUri.toString());
            startActivity(i);
        }

        if(requestCode==VIDEO_REQUEST && resultCode==RESULT_OK){

            selectedUri = data.getData();

            Intent i = new Intent(MainActivity.this,VideoActivity.class);
            i.putExtra("uri",selectedUri.toString());
            startActivity(i);

        }
    }
}
