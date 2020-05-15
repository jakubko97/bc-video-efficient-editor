package sk.fei.videoeditor.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ClippingMediaSource;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.otaliastudios.cameraview.VideoResult;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import sk.fei.videoeditor.R;
import sk.fei.videoeditor.workers.FFMpegWorker;

public class VideoActivity extends AppCompatActivity implements AdsMediaSource.MediaSourceFactory {

    Uri uri;
    String TAG = "jakubko";
    private String mode;
    SimpleExoPlayer player;
    int duration;
    File dest;
    File dest2;
    String[] command;
    String[] command2;
    String path;
    String audioPath;
    private Uri audioUri;
    private static VideoResult videoResult;
    private boolean isSaved = false;
    private int startMs;
    private int endMs;
    Constraints mConstraints;
    MediaSource video,audio;
    private  DataSource.Factory dataSourceFactory;

    public static void setVideoResult(@Nullable VideoResult result) {
        videoResult = result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        Intent i = getIntent();

        dataSourceFactory =
                new DefaultDataSourceFactory(
                        this, Util.getUserAgent(this, getString(R.string.video_preview)));

        File folder = new File(Environment.getExternalStorageDirectory() + "/My Video Editor");
        if (!folder.exists()) {
            folder.mkdir();
        }

        @SuppressLint("SimpleDateFormat") String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "VIDEO_" + timestamp + "_";

        String fileExt = ".mp4";
        dest = new File(folder, prepend + fileExt);
        dest2 = new File(folder, "FINAL_"+prepend + fileExt);

        mConstraints = new Constraints.Builder()
                .setRequiresStorageNotLow(true)
                .setRequiresBatteryNotLow(true)
                .build();

        if(i!=null) {
            String imgPath = i.getStringExtra("uri");
            mode = i.getStringExtra("mode");

            if(mode.equals("trimmedVideo")){
                duration = i.getIntExtra("duration", 0);
                //command = i.getStringArrayExtra("command");
                path = i.getStringExtra("destination");
                startMs = i.getIntExtra("startMs", 0);
                endMs = i.getIntExtra("endMs", 0);
                audioUri = Uri.parse(i.getStringExtra("audioUri"));
                uri = Uri.fromFile(new File(Objects.requireNonNull(i.getStringExtra("videoUri"))));

                command = new String[]{"-y","-i", uri.getPath(),"-c","copy","-ss","" + getTime(startMs),"-t","" + getTime(duration), dest.getAbsolutePath()};

            }
            else{
                Log.d("jakubko", "mode: "+mode);

                duration = videoResult.getMaxDuration();
                audioUri = Uri.parse(i.getStringExtra("audioUri"));
                //audioPath = getRealPathFromUri(getApplicationContext(), audioUri);
                Log.d("jakubko", "audio Path: "+audioPath +" audio uri "+ audioUri);

                uri = Uri.fromFile(videoResult.getFile());

                if(mode.equals("maxDurationReached")){
                    command = new String[]{"-i" ,videoResult.getFile().getPath(), "-i", audioUri.getPath(), "-map", "0:v", "-map", "1:a", "-c", "copy", "-shortest", dest.getAbsolutePath()};

                }
                else if(mode.equals("earlyStop")){
                    double pts = getVideoDuration()/(double)getAudioDuration();

                    //command = new String[]{"-i", videoResult.getFile().getPath(), "-filter_complex", "setpts=PTS/"+pts+"[v]", "-map", "[v]", dest.getAbsolutePath()};
                    command = new String[]{"-i", videoResult.getFile().getPath(), "-filter_complex", "setpts=PTS/"+pts+"[v]", "-map", "[v]", "-preset", "ultrafast", dest.getAbsolutePath()};
                    command2 = new String[]{"-i", dest.getAbsolutePath(), "-i", audioUri.getPath(), "-map", "0:v", "-map", "1:a", "-c", "copy", "-shortest", dest2.getAbsolutePath()};

                    Log.d("jakubko","setpts=PTS/"+pts+"[v]" + getVideoDuration() +"/"+getAudioDuration());
                }

            }


        }

    }

    public int getAudioDuration(){
        MediaPlayer mp  = new MediaPlayer();
        try {
            mp.setDataSource(this, audioUri);
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mp.getDuration();
    }

    public int getVideoDuration(){
        MediaPlayer mp  = new MediaPlayer();
        try {
            mp.setDataSource(videoResult.getFile().getPath());
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mp.getDuration();
    }


    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem saveButton = menu.findItem(R.id.save);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.video_preview_save,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.save :
                OneTimeWorkRequest mRequest = new OneTimeWorkRequest.Builder(FFMpegWorker.class)
                        .setInputData(createInputData(command,duration,dest.getPath()))
                        .build();

                Log.d("jakubko", "mode = " + mode);

                WorkManager mWorkManager = WorkManager.getInstance();

                if(mode.equals("earlyStop")){
                    OneTimeWorkRequest mRequest2 = new OneTimeWorkRequest.Builder(FFMpegWorker.class)
                            .setInputData(createInputData(command2,duration,dest2.getPath()))
                            .build();
                    mWorkManager.beginWith(mRequest).then(mRequest2).enqueue();
                }else{
                    Log.d("jakubko", "command first source = " + command[1] + " second source = " + command[3]);
                    mWorkManager.beginWith(mRequest).enqueue();
                }
                mWorkManager.getWorkInfoByIdLiveData(mRequest.getId()).observe(this, workInfo -> {
                    if (workInfo != null) {
                        Log.d("jakubko", "work state = " + workInfo.getState());
                    }
                });
        }

        return super.onOptionsItemSelected(item);
    }

    public Data createInputData(String[] command, int duration, String path){
        return new Data.Builder()
                .putStringArray("command", command)
                .putInt("duration", duration)
                .putString("path",path)
                .build();
    }

    private ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.i(TAG,"onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.i(TAG,"onLoadingChanged");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.i(TAG,"onPlayerStateChanged: playWhenReady = "+(playWhenReady)
                    +" playbackState = "+playbackState);
            switch (playbackState){
                case ExoPlayer.STATE_ENDED:
                    Log.i(TAG,"Playback ended!");
                    //Stop playback and return to start position

                    break;
                case ExoPlayer.STATE_READY:
                    Log.i(TAG,"State ready!");

                    break;
                case ExoPlayer.STATE_BUFFERING:
                    Log.i(TAG,"Playback buffering!");
                    break;
                case ExoPlayer.STATE_IDLE:
                    Log.i(TAG,"ExoPlayer idle!");
                    break;
            }
        }

    };

    public int getDurationOfFile(String filePath){
        // load data file
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(filePath);

        String out = "";
        // get mp3 info

        // convert duration to minute:seconds
        String duration =
                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Log.d("jakubko", duration);
        int dur = Integer.parseInt(duration);
        int ms = (dur % 60000);

        // close object
        metaRetriever.release();

        return dur;
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializePlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player!=null) {
            player.release();
            player = null;
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initializePlayer() {
        // Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        //Initialize the player
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        //Initialize simpleExoPlayerView
        SimpleExoPlayerView simpleExoPlayerView = findViewById(R.id.exoplayer);
        simpleExoPlayerView.setPlayer(player);

        audio = buildMediaSource(audioUri);
        video = buildMediaSource(uri);

        ConcatenatingMediaSource mergedSource = new ConcatenatingMediaSource();
        mergedSource.addMediaSource(setMediaSource(audio,video));

        player.addListener(eventListener);

        //audioPlayer.addListener(eventListener);
        player.prepare(mergedSource);
        //audioPlayer.prepare(audio);

    }

    private MediaSource buildMediaSource(Uri uri) {
        @C.ContentType int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    private MergingMediaSource setMediaSource(MediaSource audio, MediaSource video) {
        MergingMediaSource mergedMediaSource = new MergingMediaSource();
        if(mode.equals("trimmedVideo")){
            ClippingMediaSource clippingMediaSource = new ClippingMediaSource(video,startMs*1000,endMs*1000);
            mergedMediaSource = new MergingMediaSource(clippingMediaSource,audio);
            Log.d(TAG,"startMs: "+ startMs +" endMs: "+endMs +" ...set Media Source");
        }else if(mode.equals("maxDurationReached")){
            mergedMediaSource = new MergingMediaSource(video,audio);
        }
        else if(mode.equals("earlyStop")){
            mergedMediaSource = new MergingMediaSource(video,audio);
        }
        return mergedMediaSource;
    }

    @Override
    public MediaSource createMediaSource(Uri uri) {
        return buildMediaSource(uri);
    }

    @Override
    public int[] getSupportedTypes() {
        return new int[] {C.TYPE_DASH, C.TYPE_HLS, C.TYPE_OTHER};
    }
    @SuppressLint("DefaultLocale")
    public String getTime(int miliseconds) {

        int ms = miliseconds % 1000;
        int rem = miliseconds / 1000;
        int hr = rem / 3600;
        int remHr = rem % 3600;
        int mn = remHr / 60;
        int sec = remHr % 60;

//        if(hr==0){
//            return  String.format("%02d", mn) + ':' + String.format("%02d", sec)+ ':' + String.format("%03d", ms);
//        }
        return String.format("%02d", hr) + ':' + String.format("%02d", mn) + ':' + String.format("%02d", sec)+ ':' + String.format("%03d", ms);

    }

}