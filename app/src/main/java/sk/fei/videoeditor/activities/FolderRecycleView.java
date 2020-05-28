package sk.fei.videoeditor.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.SimpleItemAnimator;


import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sk.fei.videoeditor.R;
import sk.fei.videoeditor.adapters.FolderRecycleViewAdapter;
import sk.fei.videoeditor.beans.Album;
import sk.fei.videoeditor.dialogs.About;
import sk.fei.videoeditor.fetch.FetchFiles;
import sk.fei.videoeditor.fetch.StoragePath;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.GridLayout.HORIZONTAL;

public class FolderRecycleView extends AppCompatActivity implements FolderRecycleViewAdapter.RowItemsListener, Serializable {

    private static final int PERMISSION_REQUEST_CODE = 100;
    RecyclerView recyclerView;
    private MenuItem searchMenuItem;
    String fileType = ".mp4";
    File root = Environment.getExternalStorageDirectory();
    SearchView searchView;
    FolderRecycleViewAdapter adapter;
    int itemLayout;
    TextView noItemsText, numberOfFolders, pathTitle;
    ImageView noItems;
    Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    Boolean isSDSupportedDevice = Environment.isExternalStorageRemovable();
    LinearLayout linearLayout;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.overridePendingTransition(R.anim.slide, R.anim.slide);
       // setAnimation();
        setContentView(R.layout.recycle_list_view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.my_gallery));

        initViews();
        if (checkPermission()) {
            fetchFiles();
        }else{
            requestPermission();
        }
    }

    //Your Slide animation
    public void setAnimation(){
        if(Build.VERSION.SDK_INT>20) {
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.RIGHT);
            slide.setDuration(650);
            slide.setInterpolator(new DecelerateInterpolator());
            getWindow().setExitTransition(slide);
            getWindow().setEnterTransition(slide);
        }
    }

    private void initViews(){
        numberOfFolders = findViewById(R.id.numberOfFiles);
        noItems = findViewById(R.id.noItems);
        noItemsText = findViewById(R.id.noItemsText);
        recyclerView = findViewById(R.id.recycleList);
        itemLayout = R.layout.folder_item;
        pathTitle = findViewById(R.id.path);
        linearLayout = findViewById(R.id.media_root_layout);


    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    private void fetchFiles(){

            List<Album> albums = new ArrayList<>();
//        if(isSDSupportedDevice && isSDPresent)
//        {
//            albums = FetchFiles.getFiles(root,fileType);
//        }
//        else
//        {
        Log.d("sd", "is sd present ? "+ isSDPresent);

        StoragePath storagePath;
        storagePath = new StoragePath(getExternalFilesDirs(null));

        String[] storages;
        storages = storagePath.getDeviceStorages();

        for(int i = 0; i < storages.length; i++){
            Log.d("storage", storages[i]);
        }

        for(int i = 0; i < storages.length; i++){
            albums.addAll(FetchFiles.getFiles(new File(storages[i]),fileType));
        }
//        }
            if(!albums.isEmpty()){
                noItems.setVisibility(View.GONE);
                noItemsText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                numberOfFolders.setVisibility(View.VISIBLE);
                String songsFound = getResources().getQuantityString(R.plurals.numberOfFolders,albums.size(),albums.size());
                numberOfFolders.setText(songsFound);
                adapter = new FolderRecycleViewAdapter(this, itemLayout,albums,this);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
                //recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
                recyclerView.setHasFixedSize(true);
                // Removes blinks
                ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
                DividerItemDecoration itemDecor = new DividerItemDecoration(getApplicationContext(), HORIZONTAL);
                recyclerView.addItemDecoration(itemDecor);
            }
            else {
                getSupportActionBar().setSubtitle("");
                recyclerView.setVisibility(View.GONE);
                numberOfFolders.setVisibility(View.GONE);
                noItems.setVisibility(View.VISIBLE);
                noItemsText.setText("No video founded");
                noItemsText.setVisibility(View.VISIBLE);
                Snackbar snackbar = Snackbar
                        .make(linearLayout, "You have to upload video (.mp4) to your phone's storage before the process.",Snackbar.LENGTH_INDEFINITE)
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
//        if (!searchView.isIconified()) {
//            searchView.setIconified(true);
//            return;
//        }
        super.onBackPressed();
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.about) {
            About.CreateDialog(this);
        }
        if(item.getItemId() == android.R.id.home){
            supportFinishAfterTransition();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        menu.findItem(R.id.search).setVisible(false);
//        SearchManager searchManager = (SearchManager)
//                getSystemService(Context.SEARCH_SERVICE);
//        searchMenuItem = menu.findItem(R.id.search);
//        searchView = (SearchView) searchMenuItem.getActionView();
//
//        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        //searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRefreshData() {
        String songsFound = getResources().getQuantityString(R.plurals.numberOfFolders,adapter.getItemCount(),adapter.getItemCount());
        numberOfFolders.setText(songsFound);
    }

//    @Override
//    public boolean onQueryTextSubmit(String query) {
//        adapter.getFilter().filter(query);
//        return false;
//    }
//
//    @Override
//    public boolean onQueryTextChange(String newText) {
//        adapter.getFilter().filter(newText);
//
//        return true;
//    }


    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(FolderRecycleView.this,          android.Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(FolderRecycleView.this, new String[]
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


    @Override
    public void onRowItemSelected(Album album, ImageView sharedImageView) {

//        if (searchView.isShown()) {
//            searchMenuItem.collapseActionView();
//            searchView.setQuery("", false);
//        }

        Intent i = new Intent(FolderRecycleView.this, GalleryRecycleView.class);
        Bundle args = new Bundle();
        args.putSerializable("rowItems", album.getRowItems());
        i.putExtra("bundle",args);
        i.putExtra("title",album.getName());
        //i.putExtra("rowItems", album.getRowItems());
        if(Build.VERSION.SDK_INT>20){
//            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.shared_element_transition));
//            view.setTransitionName("videoTrim");
            Pair<View, String> p1 = Pair.create(sharedImageView, ViewCompat.getTransitionName(sharedImageView));
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this,p1);
            startActivity(i,activityOptionsCompat.toBundle());
        }else {
            startActivity(i);
        }

    }
}
