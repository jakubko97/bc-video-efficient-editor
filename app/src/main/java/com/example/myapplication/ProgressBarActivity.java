package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

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

            WorkRequest mRequest = new OneTimeWorkRequest.Builder(FFMpegService.class).setInputData(createInputData()).build();

            WorkManager mWorkManager = WorkManager.getInstance();
            WorkManager.getInstance().enqueue(mRequest);

            mWorkManager.getWorkInfoByIdLiveData(mRequest.getId()).observe(this, new Observer<WorkInfo>() {
                @Override
                public void onChanged(@Nullable WorkInfo workInfo) {
                    if (workInfo != null) {
                        Integer res = workInfo.getOutputData().getInt("percentage",0);


                        if(res<100){
                                progressBar.setProgress(res);
                        }

                        if(res==100){
                                progressBar.setProgress(res);

                                Toast.makeText(getApplicationContext(),"Video trimmed successfully",Toast.LENGTH_LONG).show();
                         }

                        Toast.makeText(getApplicationContext(),"workInfo "+res,Toast.LENGTH_LONG).show();

                    }
                }
            });
//
        }

    }

    public Data createInputData(){
        return new Data.Builder()
                .putInt("duration", duration)
                .putStringArray("command", command)
                .putString("destination", path)
                .build();
    }
}

