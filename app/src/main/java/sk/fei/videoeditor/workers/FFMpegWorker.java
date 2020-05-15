package sk.fei.videoeditor.workers;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import sk.fei.videoeditor.activities.VideoViewActivity;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;


public class FFMpegWorker extends Worker {

    FFmpeg ffmpeg;
    int duration;
    String[] command;
    String path;

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;


    public FFMpegWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        duration = getInputData().getInt("duration",duration);
        command = getInputData().getStringArray("command");
        path = getInputData().getString("path");

        Log.d("jakubko","Begin work");

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

                String[] arr;

                if(message.contains("time=")){

                    arr = message.split("time=");
                    String yalo = arr[1];

                    String[] abc = yalo.split(":");
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

                    mBuilder.setProgress(100, (int)res, false);
                    mNotifyManager.notify(1, mBuilder.build());

                }
            }

            @Override
            public void onStart() {
                super.onStart();
                showNotification();
                Log.d("jakubko","Cut started");
            }

            @Override
            public void onFinish() {
                // Set progress to 100 after you are done doing your work.
                mBuilder.setContentText("Video process complete")
                        // Removes the progress bar
                        .setProgress(0,0,false);
                mNotifyManager.notify(1, mBuilder.build());
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
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFinish() {}
        });

    }

    private void showNotification(){
        mNotifyManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setContentTitle("Video file in process")
                .setContentText("Downloading")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(false)
                .setOnlyAlertOnce(true)
                .setSmallIcon(android.R.drawable.stat_sys_download_done);

        Intent notificationIntent = new Intent(getApplicationContext(), VideoViewActivity.class);
        Log.d("worker path",path);
        notificationIntent.putExtra("filePath",path);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(intent);
        mNotifyManager.notify(1, mBuilder.build());

        // Start a lengthy operation in a background thread
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                    }
                }
// Starts the thread by calling the run() method in its Runnable
        ).start();
    }


}
