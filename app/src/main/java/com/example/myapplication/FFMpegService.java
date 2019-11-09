package com.example.myapplication;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;

import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;


import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;


public class FFMpegService extends Service {

    FFmpeg ffmpeg;
    int duration;

    String[] command;
    Callbacks activity;



    public MutableLiveData<Integer> percentage;
    IBinder myBinder = new LocalBinder();

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent!=null){

            duration = Integer.parseInt(intent.getStringExtra("duration"));
            command = intent.getStringArrayExtra("command");
            try {
                loadFFmpegBinary();
                execFFmpegCommand();
            } catch (FFmpegNotSupportedException e) {
                e.printStackTrace();
            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void execFFmpegCommand() throws FFmpegCommandAlreadyRunningException {

        ffmpeg.execute(command,new ExecuteBinaryResponseHandler(){

            @Override
            public void onFailure(String message) {
                super.onFailure(message);
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

                    float timeInSec = hours+min+sec;

                    percentage.setValue((int)((timeInSec/duration)*100));

                }
            }

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFinish() {
                percentage.setValue(100);
            }

        });
    }

    @Override
    public void onCreate(){
        super.onCreate();
        try {
            loadFFmpegBinary();
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
        percentage = new MutableLiveData<>();
    }

    private void loadFFmpegBinary() throws FFmpegNotSupportedException {
        if(ffmpeg == null){
            ffmpeg = FFmpeg.getInstance(this);

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

    public FFMpegService(){
        super();
    }

    public class LocalBinder extends Binder {

        public FFMpegService getServiceInstance(){
            return FFMpegService.this;

        }
    }

    public void registerClient(Activity activity){

        this.activity = (Callbacks)activity;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return myBinder;
    }

    public MutableLiveData<Integer> getPercentage(){

        return percentage;
    }

    public interface Callbacks
    {
        void updateClient(float data);
    }

}

