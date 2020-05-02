package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
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
import com.otaliastudios.cameraview.VideoResult;

import java.io.File;

public class VideoActivity extends AppCompatActivity {

    Uri uri;
    Uri audioUri;
    private String mode;
    SimpleExoPlayer player;
    int duration;
    File dest;

    String[] command;
    String path;

    private static VideoResult videoResult;

    public static void setVideoResult(@Nullable VideoResult result) {
        videoResult = result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        final VideoResult result = videoResult;

        final Intent i = getIntent();

        if(i!=null) {
            String imgPath = i.getStringExtra("uri");
            mode = i.getStringExtra("mode");

            duration = i.getIntExtra("duration", 0);
            command = i.getStringArrayExtra("command");
            path = i.getStringExtra("destination");

            if(mode.equals("trimmedVideo")){
                uri = Uri.parse(path);

                OneTimeWorkRequest mRequest = new OneTimeWorkRequest.Builder(FFMpegWorker.class).setInputData(createInputData()).build();

                WorkManager mWorkManager = WorkManager.getInstance();

                mWorkManager.enqueue(mRequest);

                mWorkManager.getWorkInfoByIdLiveData(mRequest.getId()).observe(this, workInfo -> {
                    if (workInfo.getState() == WorkInfo.State.RUNNING) {

                        Data progress = workInfo.getProgress();

                        int res = progress.getInt("progress", 0);

                        Log.d("jakubko", "WorkState = " + workInfo.getState());
                        Log.d("jakubko", "result = " + res);

                        //Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        //mediaStoreUpdateIntent.setData(Uri.fromFile(new File(path)));
                        //sendBroadcast(mediaStoreUpdateIntent);
                    }
                });
            }
            else if(mode.equals("captureVideo")){
                //uri = Uri.parse(imgPath);
                uri = Uri.fromFile(videoResult.getFile());
            }


        }

    }

    public Data createInputData(){
        return new Data.Builder()
                .putStringArray("command", command)
                .putString("dest", path)
                .putInt("duration", duration)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.video_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.btnShare) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri); // for media share
            sendIntent.setType("video/*");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }
        else if(item.getItemId() == R.id.delete){

            final AlertDialog.Builder alert = new AlertDialog.Builder(VideoActivity.this);

            LinearLayout linearLayout = new LinearLayout(VideoActivity.this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(50, 0, 100, 0);

            alert.setMessage("Ste si istý že chcete vymazať video?");
            alert.setView(linearLayout);
            alert.setNegativeButton("ZRUŠIŤ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            alert.setPositiveButton("POTVRDIŤ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(videoResult.getFile().exists()){
                        videoResult.getFile().delete();
                        finish();
                        dialog.dismiss();
                    }
                }
            });

            alert.show();

        }
        return super.onOptionsItemSelected(item);
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

        //MediaSource audio = new ExtractorMediaSource(audioUri, dataSourceFactory, new DefaultExtractorsFactory(), null, null);
        MediaSource video = new ExtractorMediaSource(uri, dataSourceFactory, new DefaultExtractorsFactory(), null, null);

        //MergingMediaSource mergedMediaSource = new MergingMediaSource(video,audio);

        //ConcatenatingMediaSource mergedSource = new ConcatenatingMediaSource();
        //mergedSource.addMediaSource(mergedMediaSource);

        player.prepare(video);

    }

}