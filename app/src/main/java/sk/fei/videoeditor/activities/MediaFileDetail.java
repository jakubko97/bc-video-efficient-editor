package sk.fei.videoeditor.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sk.fei.videoeditor.R;
import sk.fei.videoeditor.adapters.CustomRecycleViewAdapter;
import sk.fei.videoeditor.beans.RowItem;

import static android.widget.GridLayout.HORIZONTAL;

public class MediaFileDetail extends AppCompatActivity implements SearchView.OnQueryTextListener, CustomRecycleViewAdapter.RowItemsListener {

    RecyclerView recyclerView;
    public String[] songNames;
    public String[] descriptions;
    public List<Bitmap> images;
    List<RowItem> rowItems;

    private MenuItem searchMenuItem;
    String mode;
    String fileType;
    String audioUri;
    Bitmap bitmap;
    File root = Environment.getExternalStorageDirectory();
    ArrayList<File> files = new ArrayList<>();
    SearchView searchView;
    CustomRecycleViewAdapter adapter;
    ArrayList<File> songs = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_detail_activity);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        if(intent != null ){
            mode = intent.getStringExtra("mode");

            switch (mode) {
                case "audio":
                    fileType = ".mp3";
                    getSupportActionBar().setTitle(getString(R.string.my_music));

                    break;
                case "video":
                    fileType = ".mp4";
                    audioUri = intent.getStringExtra("audioUri");
                    getSupportActionBar().setTitle(getString(R.string.my_videos));

                    break;
                case "myVideos":
                    fileType = ".mp4";
                    root = new File(root, "My Video Editor");
                    getSupportActionBar().setTitle(getString(R.string.my_created_videos));

                    if (!root.exists()) {
                        root.mkdirs();

                        setContentView(R.layout.no_items);
                    }
                    break;
            }
        }

    }



    @Override
    protected void onStart() {
        songs = readSongs(root);

        if(!songs.isEmpty()){
            rowItems = new ArrayList<>();
            recyclerView = findViewById(R.id.audioList);

            songNames = new String[songs.size()];
            descriptions = new String[songs.size()];
            images = new ArrayList<>();

            for(int i = 0; i < songs.size(); ++i){
                files.add(songs.get(i));
                songNames[i] = songs.get(i).getName().replace(fileType,"");
                descriptions[i] = getTime(getMediaDuration(songs,i));

                images.add(bitmap);
                RowItem item = new RowItem(images.get(i),songNames[i], descriptions[i],files.get(i));
                rowItems.add(item);

            }
            adapter = new CustomRecycleViewAdapter(this, R.layout.audio,rowItems,this);

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            DividerItemDecoration itemDecor = new DividerItemDecoration(getApplicationContext(), HORIZONTAL);
            recyclerView.addItemDecoration(itemDecor);


        }else{
            setContentView(R.layout.no_items); //if no video has been created
        }

        super.onStart();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private int getMediaDuration(ArrayList<File> songs,int i) {
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(songs.get(i).getAbsolutePath());
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mp.getDuration();
    }

    private ArrayList<File> readSongs(File root){


        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = root.listFiles();

        Log.d("TAG", root.getAbsolutePath());

        for (File file : files) {
                if (file.isDirectory()) {
                    arrayList.addAll(readSongs(file));
                } else {
                    if (file.getName().endsWith(fileType)) {
                        arrayList.add(file);
                    }
                }
            }

        return arrayList;
    }
    @SuppressLint("DefaultLocale")
    public String getTime(int miliseconds) {

        int ms = miliseconds % 1000;
        int rem = miliseconds / 1000;
        int hr = rem / 3600;
        int remHr = rem % 3600;
        int mn = remHr / 60;
        int sec = remHr % 60;

        if(hr==0){
            return  String.format("%02d", mn) + ':' + String.format("%02d", sec);
        }
        return String.format("%02d", hr) + ':' + String.format("%02d", mn) + ':' + String.format("%02d", sec);

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
        if (searchView.isShown()) {
            searchMenuItem.collapseActionView();
            searchView.setQuery("", false);
        }
        switch (mode) {
            case "audio": {
                Intent i = new Intent(MediaFileDetail.this, AudioPreview.class);
                i.putExtra("audioPath", rowItem.getFile().getAbsolutePath());
                startActivity(i);
                break;
            }
            case "video": {
                Intent i = new Intent(MediaFileDetail.this, TrimActivity.class);
                i.putExtra("uri", rowItem.getFile().getAbsolutePath());
                i.putExtra("audioUri", audioUri);
                startActivity(i);
                break;
            }
            case "myVideos": {
                Intent i = new Intent(MediaFileDetail.this, VideoViewActivity.class);
                i.putExtra("filePath", rowItem.getFile().getAbsolutePath());
                startActivity(i);
                break;
            }
        }
    }
}
