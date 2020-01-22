package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.concurrent.Executor;


public class ProgressBarActivity extends AppCompatActivity {

    CircularProgressBar progressBar;
    int duration;
    String[] command;
    String path;
    private TextView percentageBar;

    public ProgressBarActivity(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_bar);

        progressBar = (CircularProgressBar) findViewById(R.id.progressBar);
        percentageBar = (TextView) findViewById(R.id.percentageBar);

        final Intent i = getIntent();

        if (i != null) {

            duration = i.getIntExtra("duration", 0);
            command = i.getStringArrayExtra("command");
            path = i.getStringExtra("destination");

            OneTimeWorkRequest mRequest = new OneTimeWorkRequest.Builder(FFMpegService.class).setInputData(createInputData(command)).build();

            WorkManager mWorkManager = WorkManager.getInstance();
            mWorkManager.beginWith(mRequest).enqueue();


                    mWorkManager.getWorkInfoByIdLiveData(mRequest.getId()).observe(this, new Observer<WorkInfo>() {
                @Override
                public void onChanged(@Nullable WorkInfo workInfo) {
                    if (workInfo != null) {
                        Data progress = workInfo.getProgress();

                        int res = progress.getInt("progress", 0);

                        Log.d("jakubko","WorkState = "+ workInfo.getState());
                        Log.d("jakubko","result = "+ res);

                        if(res < 100){

                            progressBar.setProgress((float)res);
                        }

                        if(res == 100){

                            progressBar.setProgress((float)res);
                            Toast.makeText(getApplicationContext(),"Video trimmed successfully2",Toast.LENGTH_LONG).show();
                         }

                    }
                }
            });

        }

    }


    public Data createInputData(String[] command){
        return new Data.Builder()
                .putStringArray("command", command)
                .putInt("duration", duration)
                .build();
    }
}

