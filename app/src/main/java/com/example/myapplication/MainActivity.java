package com.example.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static int VIDEO_REQUEST = 101;
    Uri selectedUri;
    Uri selectedAudio;
    private Button openVideo, captureVideo, setAudio;
    int audioDuration;
    MediaPlayer audio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openVideo = (Button) findViewById(R.id.openVideo);
        captureVideo = (Button) findViewById(R.id.captureVideo);
        setAudio = (Button) findViewById(R.id.setAudio);

        audio = MediaPlayer.create(this, R.raw.audios);
        audioDuration = audio.getDuration()/1000;


        Log.d("audio", "audio duration: ");

        openVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideo(v);
            }
        });

        captureVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    captureVideo(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        setAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAudio(v);
            }
        });
    }



    private void captureVideo(View v) throws IOException {

        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        videoIntent.putExtra("android.intent.extra.durationLimit", audioDuration);

        if(videoIntent.resolveActivity(getPackageManager()) != null){

            startActivityForResult(videoIntent,VIDEO_REQUEST);

        }
    }

    private void openVideo(View v){

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("video/mp4");
        startActivityForResult(i,100);

    }

    private void setAudio(View v){

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("audio/mp3");
        startActivityForResult(i,102);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == RESULT_OK){

            selectedUri = data.getData();

            Intent i = new Intent(MainActivity.this,VideoActivity.class);
            i.putExtra("uri",selectedUri.toString());
            i.putExtra("mode","openVideo");
            startActivity(i);
        }

        if(requestCode==VIDEO_REQUEST && resultCode==RESULT_OK){

            selectedUri = data.getData();

            Intent i = new Intent(MainActivity.this,VideoActivity.class);
            i.putExtra("uri",selectedUri.toString());
            i.putExtra("mode","captureVideo");
            startActivity(i);

        }
        if(requestCode == 102 && resultCode == RESULT_OK){

            selectedAudio = data.getData();

            Log.d("audio", "Selected audio: "+ selectedAudio);
        }
    }

}
