package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

public class VideoActivity extends AppCompatActivity {

    Uri uri;
    Uri audioUri;
    private Button btntrim, btnsave;
    private String mode;
    SimpleExoPlayer player;
    double duration;
    File dest;
    String filePrefix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        Intent i = getIntent();

        if(i!=null){
            String imgPath = i.getStringExtra("uri");
            mode = i.getStringExtra("mode");
            uri = Uri.parse(imgPath);
        }

        btntrim = (Button) findViewById(R.id.btntrim);
        btnsave = (Button) findViewById(R.id.btnsave);

        Log.d("jakubko",mode+"= captureVideo");
        if(mode.equals("captureVideo")){
            btntrim.setEnabled(false);
        }

        btntrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(VideoActivity.this,TrimActivity.class);
                i.putExtra("uri",uri.toString());
                i.putExtra("audioUri",audioUri.toString());
                startActivity(i);
            }
        });

        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder alert = new AlertDialog.Builder(VideoActivity.this);

                LinearLayout linearLayout = new LinearLayout(VideoActivity.this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(50, 0, 100, 0);
                final EditText input = new EditText(VideoActivity.this);
                input.setLayoutParams(lp);
                input.setGravity(Gravity.TOP | Gravity.START);
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                linearLayout.addView(input, lp);

                alert.setMessage("Set video name");
                alert.setTitle("Change video name");
                alert.setView(linearLayout);
                alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                alert.setPositiveButton("save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        filePrefix = input.getText().toString();

                        File folder = new File(Environment.getExternalStorageDirectory() + "/SavedVideos");
                        if (!folder.exists()) {

                            folder.mkdir();
                        }

                        String fileExt = ".mp4";
                        dest = new File(folder, filePrefix + fileExt);
                        String originalPath = getRealPathFromUri(getApplicationContext(), uri);
                        String audioPath = "/storage/emulated/0/audios.mp3";

                        int audioDuration = getDurationOfFile(audioPath);
                        int videoDuration = getDurationOfFile(originalPath);
                        double coef = (double)audioDuration/(double)videoDuration;
                        duration = coef*(double)videoDuration;

                        //String[] command = new String[]{"-i", originalPath, "-i", audioPath, "-map" ,"0:v", "-map", "1:a", "-c", "copy", "-shortest", dest.getAbsolutePath()};
                        String[] command = new String[]{"-i", originalPath, "-filter_complex", "setpts=PTS*"+coef+"[v]", "-map", "[v]", "-b:v", "2097k" ,"-r", "60", dest.getAbsolutePath()};
                        Log.d("jakubko","coeficient = "+coef);
                        Log.d("jakubko","videoDuration = "+videoDuration);

                        Intent saveIntent = new Intent(VideoActivity.this, ProgressBarActivity.class);
                        saveIntent.putExtra("duration", (int)duration);

                        saveIntent.putExtra("command", command);
                        startActivity(saveIntent);
                        finish();
                        dialog.dismiss();

                    }
                });

                alert.show();


            }
        });
    }

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

    private String getRealPathFromUri(Context context, Uri contentUri) {

        Cursor cursor = null;

        try {

            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);

            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            initializePlayer();
        } catch (RawResourceDataSource.RawResourceDataSourceException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player!=null) {
            player.release();
            player = null;
        }
    }

    private void initializePlayer() throws RawResourceDataSource.RawResourceDataSourceException {
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

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "CloudinaryExoplayer"));

        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        // This is the MediaSource representing the media to be played.

//        MediaSource videoSource = new ExtractorMediaSource(uri,
//                dataSourceFactory, extractorsFactory, null, null);

        DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(R.raw.audios));
        final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(this);
        rawResourceDataSource.open(dataSpec);

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return rawResourceDataSource;
            }
        };

        audioUri = rawResourceDataSource.getUri();

        MediaSource audio = new ExtractorMediaSource(audioUri, dataSourceFactory, new DefaultExtractorsFactory(), null, null);
        MediaSource video = new ExtractorMediaSource(uri, dataSourceFactory, new DefaultExtractorsFactory(), null, null);

        player.setPlaybackParameters(new PlaybackParameters(1.0f));

        MergingMediaSource mergedMediaSource = new MergingMediaSource(video,audio);

        ConcatenatingMediaSource mergedSource = new ConcatenatingMediaSource();
        mergedSource.addMediaSource(mergedMediaSource);

        player.prepare(video);

    }

//    private void workManager(String[] command){
//        WorkRequest mRequest = new OneTimeWorkRequest.Builder(FFMpegService.class).setInputData(createInputData(command)).build();
//
//        WorkManager mWorkManager = WorkManager.getInstance();
//        WorkManager.getInstance().enqueue(mRequest);
//
//        mWorkManager.getWorkInfoByIdLiveData(mRequest.getId()).observe(this, new Observer<WorkInfo>() {
//            @Override
//            public void onChanged(@Nullable WorkInfo workInfo) {
//                if (workInfo != null && WorkInfo.State.SUCCEEDED == workInfo.getState()) {
//                    Data progress = workInfo.getProgress();
//
//                        Toast.makeText(getApplicationContext(),"Video connected successfully",Toast.LENGTH_LONG).show();
//
//                }
//            }
//        });
//    }

//    private Data createInputData(String[] command){
//        return new Data.Builder()
//                .putStringArray("command", command)
//                .build();
//    }
}