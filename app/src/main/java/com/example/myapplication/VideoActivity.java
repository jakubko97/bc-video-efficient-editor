package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

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

public class VideoActivity extends AppCompatActivity {

    Uri uri;
    Uri audioUri;
    private Button btnonce, btnstop, btnplay, btntrim;
    private VideoView vv;
    private MediaController mediacontroller;
    private String mode;

    SimpleExoPlayer player;
    private boolean isContinuously = false;
    private ProgressBar progressBar;

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

        progressBar = (ProgressBar) findViewById(R.id.progrss);
        btnonce = (Button) findViewById(R.id.btnonce);
        btnstop = (Button) findViewById(R.id.btnstop);
        btnplay = (Button) findViewById(R.id.btnplay);
        btntrim = (Button) findViewById(R.id.btntrim);

        if(mode == "captureVideo"){
            btntrim.setVisibility(View.INVISIBLE);
        }
        //vv = (VideoView) findViewById(R.id.vv);

        mediacontroller = new MediaController(this);
        mediacontroller.setAnchorView(vv);

        btnstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //vv.pause();
            }
        });

        btntrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(VideoActivity.this,TrimActivity.class);
                i.putExtra("uri",uri.toString());
                startActivity(i);
            }
        });
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

        MediaSource audio = new ExtractorMediaSource(rawResourceDataSource.getUri(), dataSourceFactory, new DefaultExtractorsFactory(), null, null);
        MediaSource video = new ExtractorMediaSource(uri, dataSourceFactory, new DefaultExtractorsFactory(), null, null);

        player.setPlaybackParameters(new PlaybackParameters(1.0f));


        MergingMediaSource mergedMediaSource = new MergingMediaSource(video,audio);

        ConcatenatingMediaSource mergedSource = new ConcatenatingMediaSource();
        mergedSource.addMediaSource(mergedMediaSource);
        player.prepare(mergedSource);



        // Prepare the player with the source.
        //player.prepare(mergedSource);

    }
}