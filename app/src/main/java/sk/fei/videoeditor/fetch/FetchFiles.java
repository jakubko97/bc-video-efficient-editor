package sk.fei.videoeditor.fetch;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import sk.fei.videoeditor.beans.RowItem;

public class FetchFiles {

    public static List<RowItem> getFiles(File root, String fileType) {
        ArrayList<File> songs = new ArrayList<>();
        songs = readSongs(root,fileType);
        List<RowItem> rowItems = new ArrayList<>();

        if (!songs.isEmpty()) {

            String[] songNames = new String[songs.size()];
            String[] descriptions = new String[songs.size()];
            Date[] dateCreated = new Date[songs.size()];

            for (int i = 0; i < songs.size(); ++i) {
                //files.add(songs.get(i));
                songNames[i] = songs.get(i).getName().replace(fileType, "");
                descriptions[i] = getTime(getMediaDuration(songs, i));
                dateCreated[i] = getDateCreated(i, songs);

                RowItem item = new RowItem(songNames[i], descriptions[i], songs.get(i), dateCreated[i]);
                String unit = "MB";
                item.setSize(getFileSize(songs.get(i), unit));
                rowItems.add(item);
            }
            Collections.sort(rowItems, new Comparator<RowItem>() {
                @Override
                public int compare(RowItem row1, RowItem row2) {
                    return (row2.getDateCreated()).compareTo(row1.getDateCreated());
                }
            });
        }

        return rowItems;
    }

    private static int getMediaDuration(ArrayList<File> songs, int i) {
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(songs.get(i).getAbsolutePath());
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mp.getDuration();
    }


    private static ArrayList<File> readSongs(File root, String fileType){


        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = root.listFiles();

        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                arrayList.addAll(readSongs(file, fileType));
            } else {
                if (file.getName().endsWith(fileType)) {
                    arrayList.add(file);
                }
            }
        }

        return arrayList;
    }


    @SuppressLint("DefaultLocale")
    private static String getTime(int miliseconds) {

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

    @SuppressLint("DefaultLocale")
    private static String getFileSize(File file, String unit){

        // Get length of file in bytes
        long fileSizeInBytes = file.length();
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        long fileSizeInKB = fileSizeInBytes / 1024;
        //  Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        double fileSizeInMB = (double)fileSizeInKB / (double)1024;

        if(unit.equals("MB")){
            return  String.format("%.2f",fileSizeInMB)+" MB";
        }
        else if(unit.equals("KB")){
            return  String.format("%.2f",fileSizeInKB)+" KB";
        }
        else{
            return  String.format("%.2f",fileSizeInMB)+" MB";
        }
    }

    private static Date getDateCreated(int position, List<File> files) {

        return new Date(files.get(position).lastModified());

    }

}

