package sk.fei.videoeditor.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sk.fei.videoeditor.R;
import sk.fei.videoeditor.adapters.VideoRecycleViewAdapter;
import sk.fei.videoeditor.animations.Animations;
import sk.fei.videoeditor.beans.RowItem;
import sk.fei.videoeditor.fetch.FetchFiles;

import static android.widget.GridLayout.HORIZONTAL;

public class MediaFileRecycleView extends AppCompatActivity implements SearchView.OnQueryTextListener, VideoRecycleViewAdapter.RowItemsListener {

    private static final int PERMISSION_REQUEST_CODE = 100;
    RecyclerView recyclerView;
    RowItem selectedItem;
    private MenuItem searchMenuItem;
    String mode;
    SwipeRefreshLayout mSwipeRefreshLayout;

    boolean doubleBackToExitPressedOnce = false;
    boolean isRotate = false;
    SearchView searchView;
    VideoRecycleViewAdapter adapter;
    int itemLayout;
    FloatingActionButton showActionFab, recordVideoFab, galleryFab,fab3;
    boolean isFABOpen = false;
    TextView noItemsText;
    ImageView noItems;
    SpeedDialView speedDialView;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_detail);

        initViews();
    }

    private void openVideo(){

        // Permission has already been granted
        Intent i = new Intent(this, GalleryRecycleView.class);
//        i.putExtra("mode","video");
//        i.putExtra("audioUri",selectedAudio.toString());
        startActivity(i);

    }

    private void captureVideo() {
        // Permission has already been granted
        Intent i = new Intent(this, Camera.class);
//        i.putExtra("audioUri",selectedAudio.toString());
//        i.putExtra("mode","withAudio");
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

    public void initViews(){

        noItems = findViewById(R.id.noItems);
        noItemsText = findViewById(R.id.noItemsText);
        recyclerView = findViewById(R.id.audioList);
//        showActionFab = (FloatingActionButton) findViewById(R.id.fab);
//        recordVideoFab = (FloatingActionButton) findViewById(R.id.fab1);
//        galleryFab = (FloatingActionButton) findViewById(R.id.fab2);

        speedDialView = findViewById(R.id.speedDial);
        //speedDialView.inflate(R.menu.search_menu);

//        Animations.init(recordVideoFab);
//        Animations.init(galleryFab);
        //isRotate = false;


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
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.primaryColor, getTheme()))
                        .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()))
                        .setLabel(getString(R.string.record_video))
                        .setLabelColor(getResources().getColor(R.color.material_gray_800))
                        .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()))
                        .setLabelClickable(true)
                        .setTheme(R.style.AppTheme_Purple)
                        .create()
        );

        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.openVideo, R.drawable.ic_video_library_white_24dp)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.primaryColor, getTheme()))
                        .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()))
                        .setLabelColor(getResources().getColor(R.color.material_gray_800))
                        .setLabel(getString(R.string.select_from_gallery))
                        .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()))
                        .setLabelClickable(true)
                        .setTheme(R.style.AppTheme_Purple)
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

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) speedDialView.getLayoutParams();
        params.setBehavior(new SpeedDialView.ScrollingViewSnackbarBehavior());
        speedDialView.requestLayout();

//        showActionFab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                isRotate = Animations.rotateFab(view,!isRotate);
//
//                if(isRotate){
//                    Animations.showIn(recordVideoFab);
//                    Animations.showIn(galleryFab);
//                }else{
//                    Animations.showOut(recordVideoFab);
//                    Animations.showOut(galleryFab);
//                }
//
//            }
//        });
//
//        recordVideoFab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Animations.showOut(recordVideoFab);
////                Animations.showOut(galleryFab);
//                captureVideo();
//            }
//        });
//
//        galleryFab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Animations.showOut(recordVideoFab);
////                Animations.showOut(galleryFab);
//                openVideo();
//            }
//        });
    }

    private void fetchFiles(){
         File root = Environment.getExternalStorageDirectory();
            root = new File(root, "My Video Editor");
        if (!root.exists()) {
            root.mkdirs();
            recyclerView.setVisibility(View.GONE);
            noItems.setVisibility(View.VISIBLE);
            noItemsText.setVisibility(View.VISIBLE);
        }
        else{

            List<RowItem> rowItems = new ArrayList<>();
            mSwipeRefreshLayout = findViewById(R.id.swipeRefresh);
            mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_accent);

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    fetchFiles();
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });

            String fileType = ".mp4";
            rowItems = FetchFiles.getFiles(root,fileType);

            Log.d("jakubko",  "velkost rowItems: "+ rowItems.size());
            if(!rowItems.isEmpty()){
                recyclerView.setVisibility(View.VISIBLE);
                noItems.setVisibility(View.GONE);
                noItemsText.setVisibility(View.GONE);

                itemLayout = R.layout.audio;
                adapter = new VideoRecycleViewAdapter(this, itemLayout,rowItems,this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setHasFixedSize(true);
            // Removes blinks
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            DividerItemDecoration itemDecor = new DividerItemDecoration(getApplicationContext(), HORIZONTAL);
            recyclerView.addItemDecoration(itemDecor);
            }
            else {
                getSupportActionBar().setSubtitle("");
                recyclerView.setVisibility(View.GONE);
                noItems.setVisibility(View.VISIBLE);
                noItemsText.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.home) {
            onBackPressed();
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
    public void onRowItemSelected(RowItem rowItem) {

        selectedItem = rowItem;

        if (searchView.isShown()) {
            searchMenuItem.collapseActionView();
            searchView.setQuery("", false);
        }

                Intent i = new Intent(MediaFileRecycleView.this, VideoViewer.class);
                i.putExtra("filePath", rowItem.getFile().getAbsolutePath());
                startActivity(i);

//        Animations.showOut(recordVideoFab);
//        Animations.showOut(galleryFab);
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
