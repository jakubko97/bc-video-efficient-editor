package sk.fei.videoeditor.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.SimpleItemAnimator;


import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import sk.fei.videoeditor.R;
import sk.fei.videoeditor.adapters.AudioRecycleViewAdapter;
import sk.fei.videoeditor.beans.RowItem;
import sk.fei.videoeditor.dialogs.About;
import sk.fei.videoeditor.fetch.FetchFiles;
import sk.fei.videoeditor.fetch.StoragePath;

import static android.widget.GridLayout.HORIZONTAL;

public class AudioFileRecycleView extends AppCompatActivity implements SearchView.OnQueryTextListener, AudioRecycleViewAdapter.RowItemsListener {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_AUDIO = 100;

    RecyclerView recyclerView;

    RowItem selectedItem;

    TextView noItemsText, numberOfSongs;
    ImageView noItems;
    private MenuItem searchMenuItem;
    String mode;
    String fileType = ".mp3";
    File root = Environment.getExternalStorageDirectory();
    SearchView searchView;
    AudioRecycleViewAdapter adapter;
    LinearLayout linearLayout;

    int itemLayout;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.overridePendingTransition(R.anim.bottom_sheet_slide_in, R.anim.bottom_sheet_slide_out);
        setContentView(R.layout.recycle_list_view);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.my_music));

        initViews();
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

    private void initViews(){
        numberOfSongs = findViewById(R.id.numberOfFiles);
        noItems = findViewById(R.id.noItems);
        noItemsText = findViewById(R.id.noItemsText);
        noItemsText.setText("NO AUDIO");
        recyclerView = findViewById(R.id.recycleList);
        linearLayout = findViewById(R.id.media_root_layout);
    }

    private void fetchFiles(){

        List<RowItem> rowItems = new ArrayList<>();

        StoragePath storagePath;
        storagePath = new StoragePath(getExternalFilesDirs(null));

        String[] storages;
        storages = storagePath.getDeviceStorages();

        for(int i = 0; i < storages.length; i++){
            Log.d("storages",storages[i]);
        }


        for(int i = 0; i < storages.length; i++){
        rowItems.addAll(FetchFiles.getAudios(new File(storages[i]),fileType));
            }

            if(!rowItems.isEmpty()){

                for(RowItem r : rowItems){
                    Log.d("rowItem",r.toString());
                }
                itemLayout = R.layout.item_audio;

                adapter = new AudioRecycleViewAdapter(this, itemLayout,rowItems,this);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
                recyclerView.setHasFixedSize(false);
                // Removes blinks
                ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
                DividerItemDecoration itemDecor = new DividerItemDecoration(getApplicationContext(), HORIZONTAL);
                recyclerView.addItemDecoration(itemDecor);
            }
            else {
                recyclerView.setVisibility(View.GONE);
                noItems.setVisibility(View.VISIBLE);
                noItemsText.setVisibility(View.VISIBLE);
                Snackbar snackbar = Snackbar
                        .make(linearLayout, "You have to upload some songs before processing.",Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        });
                snackbar.show();
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

        if (item.getItemId() == R.id.about) {
            About.CreateDialog(this);
        }
        if (item.getItemId() == android.R.id.home) {
            supportFinishAfterTransition();
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
        supportFinishAfterTransition();
    }

    @Override
    public void onRefreshData() {
        String songsFound = getResources().getQuantityString(R.plurals.numberOfSongsAvailable,adapter.getItemCount(),adapter.getItemCount());
        numberOfSongs.setText(songsFound);
    }

    @Override
    protected void onPause() {
        if(adapter != null){
        adapter.onPauseMediaPlayer();
        }
        super.onPause();
    }


    @Override
    protected void onRestart() {
        if(adapter != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                adapter.onRestartMediaPLayer();
            }
        }
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