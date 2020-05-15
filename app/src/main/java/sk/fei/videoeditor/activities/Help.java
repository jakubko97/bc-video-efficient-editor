package sk.fei.videoeditor.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import sk.fei.videoeditor.R;

public class Help extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        }


        @Override
        protected void onResume() {
            super.onResume();
            }


         @Override
        protected void onPause() {
           super.onPause();
          }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

