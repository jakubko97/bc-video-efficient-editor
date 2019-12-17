package com.example.myapplication;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;


public class FFMpegService extends Worker {

    FFmpeg ffmpeg;
    int duration;

    String[] command;

    Data output;

    public MutableLiveData<Integer> percentage;

    public FFMpegService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        duration = getInputData().getInt("duration",duration);
        command = getInputData().getStringArray("command");

        percentage = new MutableLiveData<>();

        try {
            loadFFmpegBinary();
            execFFmpegCommand();

            output = new Data.Builder()
                    .putInt("percentage", percentage.getValue())
                    .build();

            Result.success(output);

        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Mp3 conversion is not supported by that device", Toast.LENGTH_LONG).show();

            Result.failure();
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();

            Result.failure();
        }

        return Result.success(output);
    }

    private void execFFmpegCommand() throws FFmpegCommandAlreadyRunningException {

        ffmpeg.execute(command,new ExecuteBinaryResponseHandler(){

            @Override
            public void onFailure(String message) {
                super.onFailure(message);
                Log.w(null,message);
            }

            @Override
            public void onSuccess(String message) {
                super.onSuccess(message);

            }

            @Override
            public void onProgress(String message) {

                String arr[];

                if(message.contains("time=")){

                    arr = message.split("time=");
                    String yalo = arr[1];

                    String abc [] = yalo.split(":");
                    String[] abcd = abc[2].split(" ");
                    String seconds = abcd[0];

                    int hours = Integer.parseInt(abc[0]);
                    hours = hours * 3600;
                    int min = Integer.parseInt(abc[1]);
                    min = min * 60;
                    float sec = Float.valueOf(seconds);

                    float timeInSec = (hours+min+sec);
                    float timeInMs = timeInSec*1000;

                    percentage.setValue((int)(timeInMs/(float)duration)*100);

                    Log.d("jakubko", "doWork timeInSec :"+ timeInMs + "percentage :" + percentage.getValue() + " duration :" + duration);
                }
            }

            @Override
            public void onStart() {
                super.onStart();
                Log.w(null,"Cut started");
            }

            @Override
            public void onFinish() {
                percentage.setValue(100);
                Log.d("jakubko","Cutting video finished");
            }

        });
    }

    private void loadFFmpegBinary() throws FFmpegNotSupportedException {
        if(ffmpeg == null){
            ffmpeg = FFmpeg.getInstance(getApplicationContext());
        }
        //Load the binary
        ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

            @Override
            public void onStart() {}

            @Override
            public void onFailure() {
                super.onFailure();
            }

            @Override
            public void onSuccess() {
                super.onSuccess();
            }

            @Override
            public void onFinish() {}
        });

    }

    public MutableLiveData<Integer> getPercentage(){

        return percentage;
    }


}
