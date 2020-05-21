package sk.fei.videoeditor.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.SimpleItemAnimator;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import sk.fei.videoeditor.R;
import sk.fei.videoeditor.adapters.AudioRecycleViewAdapter;
import sk.fei.videoeditor.beans.RowItem;
import sk.fei.videoeditor.fetch.FetchFiles;

import static android.widget.GridLayout.HORIZONTAL;

public class AudioFileRecycleView extends AppCompatActivity implements SearchView.OnQueryTextListener, AudioRecycleViewAdapter.RowItemsListener {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_AUDIO = 100;

    RecyclerView recyclerView;

    RowItem selectedItem;

    private MenuItem searchMenuItem;
    String mode;
    String fileType = ".mp3";
    File root = Environment.getExternalStorageDirectory();
    SearchView searchView;
    AudioRecycleViewAdapter adapter;

    int itemLayout;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_chooser);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.my_music));

        Intent intent = getIntent();

        if(intent != null ){
            //mode = intent.getStringExtra("mode");

        }

        if (checkPermission()) {
            fetchFiles();
        }else{
            requestPermission();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();
    }

    private void fetchFiles(){

        if (!root.exists()) {
            root.mkdirs();
            setContentView(R.layout.no_items);
        }
        else{
            List<RowItem> rowItems = new ArrayList<>();
            recyclerView = findViewById(R.id.audioList);

            rowItems = FetchFiles.getFiles(root,fileType);

            if(!rowItems.isEmpty()){

                for(RowItem r : rowItems){
                    Log.d("rowItem",r.toString());
                }
                itemLayout = R.layout.item_audio;
                adapter = new AudioRecycleViewAdapter(this, itemLayout,rowItems,this);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setHasFixedSize(false);
                // Removes blinks
                ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
                DividerItemDecoration itemDecor = new DividerItemDecoration(getApplicationContext(), HORIZONTAL);
                recyclerView.addItemDecoration(itemDecor);
            }
            else {
                setContentView(R.layout.no_items); //if no video has been created
            }
        }
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.home) {
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

        Intent intent = new Intent();
        intent.putExtra("audioUri",rowItem.getFile().getAbsolutePath());
        setResult(REQUEST_AUDIO, intent);
        finish();
//        switch (mode) {
//            case "audio": {
//                Intent i = new Intent(MediaFileRecycleView.this, AudioPreview.class);
//                i.putExtra("audioPath", rowItem.getFile().getAbsolutePath());
//                startActivity(i);
//                break;
//            }
//            case "video": {
//                Intent i = new Intent(MediaFileRecycleView.this, TrimVideo.class);
//                i.putExtra("uri", rowItem.getFile().getAbsolutePath());
//                i.putExtra("audioUri", audioUri);
//                startActivity(i);
//                break;
//            }
//            case "myVideos": {
//                Intent i = new Intent(MediaFileRecycleView.this, VideoViewer.class);
//                i.putExtra("filePath", rowItem.getFile().getAbsolutePath());
//                startActivity(i);
//                break;
//            }
//        }
    }

    @Override
    protected void onPause() {
        adapter.onPauseMediaPlayer();

        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onRestart() {
        adapter.onRestartMediaPLayer();
        super.onRestart();
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(AudioFileRecycleView.this,          android.Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(AudioFileRecycleView.this, new String[]
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