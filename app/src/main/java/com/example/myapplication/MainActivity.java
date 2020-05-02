package com.example.myapplication;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    public static int VIDEO_REQUEST = 101;
    Uri selectedUri;
    Uri selectedAudio;
    private Button openVideo, captureVideo, setAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openVideo = (Button) findViewById(R.id.openVideo);
        captureVideo = (Button) findViewById(R.id.captureVideo);
        setAudio = (Button) findViewById(R.id.setAudio);


        openVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideo();
            }
        });

        captureVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureVideo();
            }
        });

        setAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAudio(v);
            }
        });
    }


    private void captureVideo() {

        Intent i = new Intent(MainActivity.this,CameraActivity.class);
//        i.putExtra("uriAudio",selectedAudio.toString());
//        i.putExtra("mode","withAudio");
        startActivity(i);
    }

    private void openVideo(){

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("video/mp4");
        startActivityForResult(i,100);

    }

    private void setAudio(View v){

        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload,1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == RESULT_OK){

            selectedUri = data.getData();

            Intent i = new Intent(MainActivity.this,TrimActivity.class);
            i.putExtra("uri",selectedUri.toString());

            if(selectedAudio != null){
                i.putExtra("audioUri",selectedAudio.toString());
                i.putExtra("mode","withAudio");
            }else{
                i.putExtra("mode","noAudio");
            }

            startActivity(i);
        }


        if(requestCode == 1){

            if(resultCode == RESULT_OK){

                //the selected audio.
                selectedAudio = data.getData();

                if(selectedAudio != null){
                    setAudio.setText(getFileName(selectedAudio));
                }
            }
        }
    }

    public static String getFileName(Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            fileName = path.substring(cut + 1);
        }
        return fileName.toLowerCase();
    }

    public static String getSize(Context context, Uri uri) {
        String fileSize = null;
        Cursor cursor = context.getContentResolver()
                .query(uri, null, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {

                // get file size
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (!cursor.isNull(sizeIndex)) {
                    fileSize = cursor.getString(sizeIndex);
                }
            }
        } finally {
            cursor.close();
        }
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        long fileSizeInKB = Integer.valueOf(fileSize) / 1024;
// Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        long fileSizeInMB = fileSizeInKB / 1024;


        return fileSizeInMB+" MB";
    }

}
