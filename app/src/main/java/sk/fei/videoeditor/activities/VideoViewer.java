package sk.fei.videoeditor.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.Objects;

import sk.fei.videoeditor.BuildConfig;
import sk.fei.videoeditor.R;

public class VideoViewer extends AppCompatActivity implements AdsMediaSource.MediaSourceFactory {

    String TAG = "videoViewActivity";
    Uri uri;
    File videoFile;
    private final String STATE_RESUME_WINDOW = "resumeWindow";
    private final String STATE_RESUME_POSITION = "resumePosition";

    private SimpleExoPlayerView playerView;
    private MediaSource mVideoSource;

    private Dialog mFullScreenDialog;
    private  DataSource.Factory dataSourceFactory;
    private ImageView close, share, delete;

    private SimpleExoPlayer player;

    private int mResumeWindow;
    private long mResumePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_video_view);
        onNewIntent(getIntent());

        dataSourceFactory =
                new DefaultDataSourceFactory(
                        this, Util.getUserAgent(this, getString(R.string.app_name)));

        if (savedInstanceState != null) {
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW);
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if(extras != null){
            Log.d("worker","filePath in view = "+ extras.getString("filePath"));

            if(extras.containsKey("filePath"))
            {
                Log.d("worker","filePath in view = "+ extras.getString("filePath"));
                videoFile = new File(Objects.requireNonNull(extras.getString("filePath")));
                uri = Uri.parse(extras.getString("filePath"));
            }
        }

        super.onNewIntent(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow);
        outState.putLong(STATE_RESUME_POSITION, mResumePosition);

        super.onSaveInstanceState(outState);
    }


    private void initFullscreenDialog() {

        mFullScreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                    mFullScreenDialog.dismiss();
                    closeAllActivities();
            }
        };

    }

    private void openFullscreenDialog() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        ((ViewGroup) playerView.getParent()).removeView(playerView);
        mFullScreenDialog.addContentView(playerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(VideoViewer.this, R.mipmap.ic_fullscreen_skrink_foreground));
        mFullScreenDialog.show();

    }


    private void initExoPlayer() {

        player = ExoPlayerFactory.newSimpleInstance(this);
        playerView.setPlayer(player);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

        boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;

        if (haveResumePosition) {
            Log.i("DEBUG"," haveResumePosition ");
            player.seekTo(mResumeWindow, mResumePosition);
        }
        //String contentUrl = getString(R.string.video_view);
        mVideoSource = buildMediaSource(Uri.parse(videoFile.getAbsolutePath()));
        Log.i("DEBUG"," mVideoSource "+mVideoSource);
        player.prepare(mVideoSource);
        player.setPlayWhenReady(true);

    }

    private void shareVideo(){
        Uri uriFromFile = FileProvider.getUriForFile(VideoViewer.this, BuildConfig.APPLICATION_ID + ".provider",videoFile);
        //Uri uriFromFile = Uri.fromFile(videoFile);
        Log.d(TAG, "uri: "+ uriFromFile);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, uriFromFile); // for media share
        sendIntent.setType("video/*");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    private void closeAllActivities(){
        Intent intent = new Intent(getApplicationContext(), MediaFileRecycleView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void initExoControlers(){
        playerView =  findViewById(R.id.exoplayer);
        playerView.findViewById(R.id.exo_save).setVisibility(View.GONE);

        close = playerView.findViewById(R.id.exo_close);
        share = playerView.findViewById(R.id.exo_share);
        delete = playerView.findViewById(R.id.exo_delete);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFullScreenDialog.dismiss();
                closeAllActivities();
            }
        });


        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareVideo();
            }
        });


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteVideo();
            }
        });


    }

    private void deleteVideo(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(VideoViewer.this);

        LinearLayout linearLayout = new LinearLayout(VideoViewer.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(50, 0, 100, 0);

        alert.setMessage("The video file will be deleted.");
        alert.setView(linearLayout);
        alert.setNegativeButton("STORNO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(videoFile.exists()){
                    if(videoFile.delete()){
                        mFullScreenDialog.dismiss();

                        Toast.makeText(VideoViewer.this,"Video has been deleted succesfully.",Toast.LENGTH_LONG).show();

                        dialog.dismiss();
                        Intent intent = new Intent(getApplicationContext(), MediaFileRecycleView.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Removes other Activities from stack
                        startActivity(intent);
                    }else{
                        Toast.makeText(VideoViewer.this,"Some problem with deleting video.",Toast.LENGTH_LONG).show();
                    }

                }
            }
        });

        alert.show();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (playerView == null) {
            initExoControlers();
            initFullscreenDialog();
        }
        initExoPlayer();
        openFullscreenDialog();

            ((ViewGroup) playerView.getParent()).removeView(playerView);
            mFullScreenDialog.addContentView(playerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
           // mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(VideoViewer.this, R.mipmap.ic_fullscreen_skrink_foreground));
            mFullScreenDialog.show();

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


    @Override
    protected void onPause() {

        super.onPause();

        if (playerView != null && player != null) {
            mResumeWindow = player.getCurrentWindowIndex();
            mResumePosition = Math.max(0, player.getContentPosition());

            player.release();
        }


    }

    @Override
    public MediaSource createMediaSource(Uri uri) {
        return buildMediaSource(uri);
    }

    @Override
    public int[] getSupportedTypes() {
        return new int[] {C.TYPE_DASH, C.TYPE_HLS, C.TYPE_OTHER};
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

}
