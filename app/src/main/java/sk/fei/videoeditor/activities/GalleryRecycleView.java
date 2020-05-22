package sk.fei.videoeditor.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.SimpleItemAnimator;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sk.fei.videoeditor.R;
import sk.fei.videoeditor.adapters.VideoRecycleViewAdapter;
import sk.fei.videoeditor.beans.Directory;
import sk.fei.videoeditor.beans.RowItem;
import sk.fei.videoeditor.fetch.FetchFiles;

import static android.widget.GridLayout.HORIZONTAL;

public class GalleryRecycleView extends AppCompatActivity implements SearchView.OnQueryTextListener, VideoRecycleViewAdapter.RowItemsListener {

    private static final int PERMISSION_REQUEST_CODE = 100;
    RecyclerView recyclerView;
    RowItem selectedItem;
    private MenuItem searchMenuItem;
    String fileType = ".mp4";
    File root = Environment.getExternalStorageDirectory();
    SearchView searchView;
    VideoRecycleViewAdapter adapter;
    int itemLayout;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_recycle_view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.my_videos));

        if (checkPermission()) {
            fetchFiles();
        }else{
            requestPermission();
        }

    }


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
            recyclerView = findViewById(R.id.videoList);

            rowItems = FetchFiles.getFiles(root,fileType);

            if(!rowItems.isEmpty()){

                itemLayout = R.layout.gallery_view;
                adapter = new VideoRecycleViewAdapter(this, itemLayout,rowItems,this,false);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
                //recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
                recyclerView.setHasFixedSize(true);
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
        Intent i = new Intent(GalleryRecycleView.this, TrimVideo.class);
                i.putExtra("uri", rowItem.getFile().getAbsolutePath());
                startActivity(i);
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
        int result = ContextCompat.checkSelfPermission(GalleryRecycleView.this,          android.Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(GalleryRecycleView.this, new String[]
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
