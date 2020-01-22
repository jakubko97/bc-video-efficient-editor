package com.example.myapplication;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
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

    public MutableLiveData<Integer> percentage;

    public FFMpegService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        // Set initial progress to 0
        setProgressAsync(new Data.Builder().putInt("progress", 1).build());
        Log.d("jakubko","FFMpegService constructor");
    }

    @NonNull
    @Override
    public Result doWork() {

        Log.d("jakubko","Beggining of doWork");
        duration = getInputData().getInt("duration",duration);
        command = getInputData().getStringArray("command");

        try {
            loadFFmpegBinary();
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Mp3 conversion is not supported by that device", Toast.LENGTH_LONG).show();

            return Result.failure();
        }
        return Result.success();
    }

    private void execFFmpegCommand() throws FFmpegCommandAlreadyRunningException {

        ffmpeg.execute(command,new ExecuteBinaryResponseHandler(){

            @Override
            public void onFailure(String message) {
                super.onFailure(message);
                Log.d("jakubko",message);
            }

            @Override
            public void onSuccess(String message) {
                super.onSuccess(message);
                Log.d("jakubko",message);
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

                    float res = (timeInMs/duration)*100;
                    Log.d("jakubko", "doWork timeInSec :"+ timeInMs + "percentage :" + res + " duration :" + duration);
                    setProgressAsync(new Data.Builder().putInt("progress", (int)res).build());
                    Toast.makeText(getApplicationContext(),(int)res+ "%",Toast.LENGTH_SHORT).show();


                }
            }

            @Override
            public void onStart() {
                super.onStart();
                Log.d("jakubko","Cut started");
            }

            @Override
            public void onFinish() {
                // Set progress to 100 after you are done doing your work.
                setProgressAsync(new Data.Builder().putInt("progress", 100).build());
                Log.d("jakubko","Cutting video finished");
                Toast.makeText(getApplicationContext(),"Video trimmed successfully",Toast.LENGTH_LONG).show();
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
                try {
                    execFFmpegCommand();
                } catch (FFmpegCommandAlreadyRunningException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {}
        });

    }

    public MutableLiveData<Integer> getPercentage(){

        return percentage;
    }


}
