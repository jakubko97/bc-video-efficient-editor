package sk.fei.videoeditor.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.view.ActionMode;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import sk.fei.videoeditor.R;
import sk.fei.videoeditor.beans.RowItem;

public class ActionDelete {

    public static void CreateDialog(Context c, List<RowItem> selectedItems, List<RowItem> itemsFiltered, ActionMode mode) {
//
        Activity activity = (Activity) c;
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(activity);
        View sheetView = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);

        CardView delete = sheetView.findViewById(R.id.bottom_sheet_delete);
        CardView storno = sheetView.findViewById(R.id.bottom_sheet_storno);
        TextView deleteText = sheetView.findViewById(R.id.bottom_sheet_delete_text);

        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();

        String deleteSelectedItems = c.getResources().getQuantityString(R.plurals.deleteSelectedItems,selectedItems.size(),selectedItems.size());
        //builder.setTitle(deleteSelectedItems);

        String text;

        if(selectedItems.size() > 1){
             text = deleteSelectedItems + " (" + selectedItems.size() + ")";
        }else{
             text = deleteSelectedItems;
        }

        deleteText.setText(text);
        deleteText.setTextColor(c.getResources().getColor(R.color.primary_accent));
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (RowItem rowItem : selectedItems) {
                    itemsFiltered.remove(rowItem);
                    rowItem.getFile().delete();
                }
                mode.finish();
                mBottomSheetDialog.dismiss();
            }
        });

        storno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetDialog.dismiss();
            }
        });

        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // Do something
            }
        });


//        AlertDialog.Builder builder = new AlertDialog.Builder(c);
//        String deleteSelectedItems = c.getResources().getQuantityString(R.plurals.deleteSelectedItems,selectedItems.size(),selectedItems.size());
//        //builder.setTitle(deleteSelectedItems);
//
//        String text;
//
//        if(selectedItems.size() > 1){
//             text = deleteSelectedItems + "(" + selectedItems.size() + ")";
//        }else{
//             text = deleteSelectedItems;
//        }
//        builder.setPositiveButton(text, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                for (RowItem rowItem : selectedItems) {
//                    itemsFiltered.remove(rowItem);
//                    rowItem.getFile().delete();
//                }
//                mode.finish();
//            }
//        });
//
//        builder.setNegativeButton("STORNO", new   DialogInterface.OnClickListener(){
//            public void onClick(DialogInterface dialog, int id){
//
//            }
//        });
//
//
//
//        AlertDialog dialog = builder.create();
//
//        //2. now setup to change color of the button
//        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface arg0) {
//                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(c.getResources().getColor(R.color.primary_accent));
//            }
//        });
//        dialog.show();
    }
}
