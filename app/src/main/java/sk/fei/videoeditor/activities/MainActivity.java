package sk.fei.videoeditor.activities;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import sk.fei.videoeditor.R;


public class MainActivity extends AppCompatActivity {

    Uri selectedAudio;
    private CardView openVideo, captureVideo, setAudio, about, myVideos, help;
    String audioPath;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openVideo = findViewById(R.id.openVideo);
        captureVideo = findViewById(R.id.recordVideo);
        setAudio = findViewById(R.id.setAudio);
        about = findViewById(R.id.about);
        myVideos = findViewById(R.id.myVideos);
        help = findViewById(R.id.help);


        Intent i = getIntent();

        if(i != null){
            audioPath = i.getStringExtra("audioPath");

            if(audioPath != null ){
                selectedAudio = Uri.parse(audioPath);
            }

        }

        openVideo.setOnClickListener(v -> {
            if(audioPath != null) {
                openVideo();
            }else{
                Toast.makeText(MainActivity.this, "Please Select Audio.", Toast.LENGTH_SHORT).show();

            }
        });


        help.setOnClickListener(v -> openHelp());

        myVideos.setOnClickListener(v -> openMyVideos());

        captureVideo.setOnClickListener(v -> {
            if(audioPath != null) {
                captureVideo();
            }else{
                Toast.makeText(MainActivity.this, "Please Select Audio.", Toast.LENGTH_SHORT).show();
            }
        });

        setAudio.setOnClickListener(v -> setAudio(v));

        about.setOnClickListener(v -> openAbout());
    }

    private void openHelp() {
        Intent i = new Intent(MainActivity.this, Help.class);
        startActivity(i);
    }

    private void openMyVideos() {
        Intent i = new Intent(MainActivity.this, MediaFileDetail.class);
        i.putExtra("mode","myVideos");
        startActivity(i);
    }

    private void openAbout() {
        Intent i = new Intent(MainActivity.this,AboutActivity.class);
        startActivity(i);
    }


    private void captureVideo() {

        Intent i = new Intent(MainActivity.this,CameraActivity.class);
        i.putExtra("audioUri",selectedAudio.toString());
        i.putExtra("mode","withAudio");
        startActivity(i);

    }

    private void openVideo(){

        Intent i = new Intent(MainActivity.this, MediaFileDetail.class);
        i.putExtra("mode","video");
        i.putExtra("audioUri",selectedAudio.toString());
        startActivity(i);

    }

    private void setAudio(View v){

        Intent i = new Intent(MainActivity.this, MediaFileDetail.class);
        i.putExtra("mode","audio");
        startActivity(i);

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }
}
