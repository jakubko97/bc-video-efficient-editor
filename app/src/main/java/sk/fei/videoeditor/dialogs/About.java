package sk.fei.videoeditor.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;

import sk.fei.videoeditor.R;

public class About {

    public static Dialog CreateDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.app_name);

        builder.setMessage(Html.fromHtml("The application was created as a Bachelor's thesis at the Slovak University of Technology. <br><br>" +
                " It offers efficient audio/video processing on Android. <br><br>" +
                " Uses <b>FFmpeg</b> library for Audio/Video processing, <b>ExoPlayer</b> as a MediaPlayer and <b>CameraView</b> library for Video Recording. <br><br> " +
                " Source code is available on <url>https://github.com/jakubko97/myVideoEditor/</url>"));














//        builder.setMessage("If you want to create some videos click on the button placed on the bottom right corner.");
//        builder.setMessage("You can select video from your mobile's storage or record a new video.");
//        builder.setMessage("The whole process requires audio file for both options.");
//        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//
//            }
//        });
//
//        builder.setNegativeButton("Cancel", new   DialogInterface.OnClickListener(){
//            public void onClick(DialogInterface dialog, int id){
//
//            }
//        });

        AlertDialog dialog = builder.create();
        dialog.show();

        return builder.create();
    }
}
