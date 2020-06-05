package sk.fei.videoeditor.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
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
import android.transition.Explode;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.SimpleItemAnimator;


import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sk.fei.videoeditor.R;
import sk.fei.videoeditor.adapters.VideoRecycleViewAdapter;
import sk.fei.videoeditor.beans.Album;
import sk.fei.videoeditor.beans.RowItem;
import sk.fei.videoeditor.dialogs.About;
import sk.fei.videoeditor.fetch.FetchFiles;

import static android.widget.GridLayout.HORIZONTAL;

public class GalleryRecycleView extends AppCompatActivity implements SearchView.OnQueryTextListener, VideoRecycleViewAdapter.RowItemsListener, Serializable {

    private static final int PERMISSION_REQUEST_CODE = 100;
    RecyclerView recyclerView;
    RowItem selectedItem;
    private MenuItem searchMenuItem;
    SearchView searchView;
    VideoRecycleViewAdapter adapter;
    int itemLayout;
    List<RowItem> rowItems = new ArrayList<>();
    String title;
    TextView numberOfFiles, pathTitle;
    LinearLayout linearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setAnimation();
        setContentView(R.layout.recycle_list_view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
        getIntentData();
        getSupportActionBar().setTitle(title);

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
            slide.setDuration(700);
            slide.setInterpolator(new AnticipateOvershootInterpolator());
            getWindow().setExitTransition(slide);
            getWindow().setEnterTransition(slide);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    private void getIntentData(){
        Intent i = getIntent();

        if(i != null){
            Bundle args = i.getBundleExtra("bundle");
            assert args != null;
            rowItems = (ArrayList<RowItem>) args.getSerializable("rowItems");
            title = i.getStringExtra("title");
        }
    }

    private void initViews(){

        linearLayout = findViewById(R.id.media_root_layout);
        pathTitle = findViewById(R.id.path);
        numberOfFiles = findViewById(R.id.numberOfFiles);
        recyclerView = findViewById(R.id.recycleList);
        itemLayout = R.layout.gallery_view;
    }

    private void fetchFiles(){

            if(!rowItems.isEmpty()){

                pathTitle.setText("~ "+ rowItems.get(0).getParent().getAbsolutePath());
                adapter = new VideoRecycleViewAdapter(this, itemLayout,rowItems,this,false);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
                //recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
                recyclerView.setHasFixedSize(true);
                // Removes blinks
                ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
                //DividerItemDecoration itemDecor = new DividerItemDecoration(getApplicationContext(), HORIZONTAL);
                //recyclerView.addItemDecoration(itemDecor);
            }
            else {

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

        switch(item.getItemId()){
            case R.id.about :
                About.CreateDialog(this);
            case android.R.id.home :
                supportFinishAfterTransition();
                break;
            default: break;
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

        if(isVideoValid(rowItem.getFile().getAbsoluteFile().toString())) {
            Intent i = new Intent(GalleryRecycleView.this, TrimVideo.class);
            i.putExtra("uri", rowItem.getFile().getAbsolutePath());
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this,sharedImageView, ViewCompat.getTransitionName(sharedImageView));
                startActivity(i,activityOptionsCompat.toBundle());

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
                    .make(linearLayout, "The video is not supported.", Snackbar.LENGTH_LONG);
            snackbar.show();
            //Toast.makeText(MainActivity.this, "Video can not be played", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    @Override
    public void onRefreshData(boolean multiselect) {
        String songsFound = getResources().getQuantityString(R.plurals.numberOfFiles,adapter.getItemCount(),adapter.getItemCount());
        numberOfFiles.setText(songsFound);
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
