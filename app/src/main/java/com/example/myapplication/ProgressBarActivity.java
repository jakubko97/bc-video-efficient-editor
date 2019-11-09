package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;


public class ProgressBarActivity extends AppCompatActivity {

    CircularProgressBar progressBar;
    int duration;
    String[] command;
    String path;
    private TextView percentageBar;

    ServiceConnection mConnection;
    FFMpegService ffMpegService;
    Integer res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_bar);

        progressBar = (CircularProgressBar) findViewById(R.id.progressBar);
        percentageBar = (TextView) findViewById(R.id.percentageBar);
        progressBar.setProgressMax(100);

        final Intent i = getIntent();

        if (i != null) {

            duration = i.getIntExtra("duration", 0);
            command = i.getStringArrayExtra("command");
            path = i.getStringExtra("destination");

            final Intent myIntent = new Intent(ProgressBarActivity.this, FFMpegService.class);
            myIntent.putExtra("duration", String.valueOf(duration));
            myIntent.putExtra("command", command);
            myIntent.putExtra("destination", path);
            startService(myIntent);

            mConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder iBinder) {
                    FFMpegService.LocalBinder binder = (FFMpegService.LocalBinder) iBinder;

                    ffMpegService = binder.getServiceInstance();
                    ffMpegService.registerClient(getParent());

                    final Observer<Integer> resultObserver = new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            res = integer;
                            percentageBar.setText(res.toString()+'%');

                            if(res<100){
                                progressBar.setProgress(res);
                            }
                            if(res==100){
                                progressBar.setProgress(res);
                                stopService(myIntent);

                                Toast.makeText(getApplicationContext(),"Video trimmed successfully",Toast.LENGTH_LONG).show();
                            }

                        }
                    };

                    ffMpegService.getPercentage().observe(ProgressBarActivity.this,resultObserver);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }

                @Override
                public void onBindingDied(ComponentName name) {

                }

                @Override
                public void onNullBinding(ComponentName name) {

                }

            };

            bindService(myIntent,mConnection, Context.BIND_AUTO_CREATE);

        }

    }
}

