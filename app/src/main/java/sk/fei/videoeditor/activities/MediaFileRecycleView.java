package sk.fei.videoeditor.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.MaterialShapeUtils;
import com.google.android.material.snackbar.Snackbar;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sk.fei.videoeditor.R;
import sk.fei.videoeditor.adapters.VideoRecycleViewAdapter;
import sk.fei.videoeditor.beans.Album;
import sk.fei.videoeditor.beans.RowItem;
import sk.fei.videoeditor.dialogs.About;
import sk.fei.videoeditor.fetch.FetchFiles;

public class MediaFileRecycleView extends AppCompatActivity implements SearchView.OnQueryTextListener, VideoRecycleViewAdapter.RowItemsListener {

    private static final int PERMISSION_REQUEST_CODE = 100;
    RecyclerView recyclerView;
    RowItem selectedItem;
    private MenuItem searchMenuItem;
    SwipeRefreshLayout mSwipeRefreshLayout;

    boolean doubleBackToExitPressedOnce = false;
    SearchView searchView;
    VideoRecycleViewAdapter adapter;
    int itemLayout;
    TextView noItemsText, numberOfFiles, pathTitle;
    ImageView noItems;
    SpeedDialView speedDialView;
    CoordinatorLayout coordinatorLayout;
    boolean actionMode = false;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_detail);

        // Check if we're running on Android 5.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Apply activity transition
        } else {
            // Swap without transition
        }

        initViews();
    }

    private void openVideo(){

        // Permission has already been granted
        Intent i = new Intent(this, FolderRecycleView.class);
        if(Build.VERSION.SDK_INT>20){
            ActivityOptions options =
                    ActivityOptions.makeSceneTransitionAnimation(this);
            startActivity(i,options.toBundle());
        }else {
            startActivity(i);
        }

    }

    private void captureVideo() {
        // Permission has already been granted
        Intent i = new Intent(this, Camera.class);
        startActivity(i);
    }



    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onBackPressed() {

        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }

        if (speedDialView.isOpen()) {
            speedDialView.close();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
        }
    }

    @Override
    protected void onResume() {
        if (checkPermission()) {
            fetchFiles();
        }else{
            requestPermission();
        }
        super.onResume();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void initViews(){

        coordinatorLayout = findViewById(R.id.home_root_coordinator);
        numberOfFiles = findViewById(R.id.numberOfFiles);
        pathTitle = findViewById(R.id.path);
        noItems = findViewById(R.id.noItems);
        noItemsText = findViewById(R.id.noItemsText);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recycleList);



        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });

        speedDialView = findViewById(R.id.speedDial);
        MaterialShapeUtils.setParentAbsoluteElevation(speedDialView);

        speedDialView.setOnChangeListener(new SpeedDialView.OnChangeListener() {
            @Override
            public boolean onMainActionSelected() {
                // Call your main action here
                return false; // true to keep the Speed Dial open
            }

            @Override
            public void onToggleChanged(boolean isOpen) {
                Log.d("TAG", "Speed dial toggle state changed. Open = " + isOpen);
            }
        });

        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.recordVideo, R.drawable.ic_videocam_white_24dp)
//                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.primary_accent, getTheme()))
//                        .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()))
                        .setLabel(getString(R.string.record_video))
                        .setLabelColor(getResources().getColor(R.color.material_gray_800))
                        .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()))
                        .setLabelClickable(true)
//                        .setTheme(R.style.AppTheme_Purple)
                        .create()
        );

        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.openVideo, R.drawable.ic_video_library_white_24dp)
//                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.primary_accent, getTheme()))
//                        .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()))
                        .setLabelColor(getResources().getColor(R.color.material_gray_800))
                        .setLabel(getString(R.string.select_from_gallery))
                        .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()))
                        .setLabelClickable(true)
//                        .setTheme(R.style.AppTheme_Purple)
                        .create());

        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem speedDialActionItem) {
                switch (speedDialActionItem.getId()) {
                    case R.id.recordVideo:
                        captureVideo();
                        return false; // true to keep the Speed Dial open
                    case R.id.openVideo:
                        openVideo();
                        return false; // true to keep the Speed Dial open
                    default:
                        return false;
                }
            }
        });

    }

    private void fetchFiles(){
         File root = Environment.getExternalStorageDirectory();
            root = new File(root, "My Video Editor");

            Log.d("root", root.getAbsolutePath());
        if (!root.exists()) {
            root.mkdirs();
            recyclerView.setVisibility(View.GONE);
            numberOfFiles.setVisibility(View.GONE);
            noItems.setVisibility(View.VISIBLE);
            noItemsText.setVisibility(View.VISIBLE);
        }
        else{

            //List<RowItem> rowItems = new ArrayList<>();

            List<Album> albums = new ArrayList<>();

            String fileType = ".mp4";
            //rowItems = FetchFiles.getFiles(root,fileType);
            albums = FetchFiles.getFiles(root,fileType);

            List<RowItem> rowItems = new ArrayList<>();

            Log.d("jakubko",  "velkost rowItems: "+ rowItems.size());
            if(!albums.isEmpty()){

                rowItems = albums.get(0).getRowItems();
                recyclerView.setVisibility(View.VISIBLE);
                numberOfFiles.setVisibility(View.VISIBLE);

                pathTitle.setText("~ "+ albums.get(0).getFile().getAbsolutePath());
                noItems.setVisibility(View.GONE);
                noItemsText.setVisibility(View.GONE);

                itemLayout = R.layout.audio;

                adapter = new VideoRecycleViewAdapter(this, itemLayout,rowItems,this,true);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            //recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
            recyclerView.setHasFixedSize(true);
            // Removes blinks
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
           //DividerItemDecoration itemDecor = new DividerItemDecoration(getApplicationContext(), HORIZONTAL);
            //recyclerView.addItemDecoration(itemDecor);
            }
            else {
                getSupportActionBar().setSubtitle("");
                recyclerView.setVisibility(View.GONE);
                numberOfFiles.setVisibility(View.GONE);
                noItems.setVisibility(View.VISIBLE);
                noItemsText.setVisibility(View.VISIBLE);
            }
        }
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_accent);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            fetchFiles();
            mSwipeRefreshLayout.setRefreshing(false);
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.home) {
            onBackPressed();
        }

        if (item.getItemId() == R.id.about) {
            About.CreateDialog(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.getFilter().filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);

        return true;
    }

    @Override
    public void onRowItemSelected(RowItem rowItem, ImageView sharedImageView) {

        selectedItem = rowItem;

        if (searchView.isShown()) {
            searchMenuItem.collapseActionView();
            searchView.setQuery("", false);
        }


        if(isVideoValid(rowItem.getFile().getAbsoluteFile().toString())){

                Intent i = new Intent(MediaFileRecycleView.this, VideoViewer.class);
                i.putExtra("filePath", rowItem.getFile().getAbsolutePath());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeScaleUpAnimation(sharedImageView,(int)sharedImageView.getX(),(int)sharedImageView.getY(),sharedImageView.getWidth(),sharedImageView.getHeight());
                startActivity(i, activityOptionsCompat.toBundle());
            }
            else{
                startActivity(i);
            }
        }

//        Animations.showOut(recordVideoFab);
//        Animations.showOut(galleryFab);
    }

    @Override
    public void onRefreshData(boolean multiselect) {
        String songsFound = getResources().getQuantityString(R.plurals.numberOfFiles,adapter.getItemCount(),adapter.getItemCount());
        numberOfFiles.setText(songsFound);

        if(multiselect){
            mSwipeRefreshLayout.setEnabled(false);
        }else{
            mSwipeRefreshLayout.setEnabled(true);

        }
    }

    private boolean isVideoValid(String path){
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            return true;
        } catch (IOException e) {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Video can not be played", Snackbar.LENGTH_LONG);
            snackbar.show();
            //Toast.makeText(MediaFileRecycleView.this, "Video can not be played", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MediaFileRecycleView.this,          android.Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission() {
            ActivityCompat.requestPermissions(MediaFileRecycleView.this, new String[]
                    {android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can use local drive .");
                fetchFiles();
            } else {
                Log.e("value", "Permission Denied, You cannot use local drive .");
            }
            break;
        }
    }

}
