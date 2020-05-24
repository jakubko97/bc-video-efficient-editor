package sk.fei.videoeditor.adapters;

import java.io.File;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import sk.fei.videoeditor.R;
import sk.fei.videoeditor.beans.RowItem;

public class CustomListViewAdapter extends ArrayAdapter<RowItem> {

    Context context;
    ImageView imageView;

    public CustomListViewAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<RowItem> rowItems) {
        super(context, resource, textViewResourceId, rowItems);
    }


    /*private view holder class*/
    public class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
        TextView txtDesc;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        RowItem rowItem = getItem(position);
        ViewHolder holder;

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.audio, null);
            holder = new ViewHolder();
            //holder.txtDesc = convertView.findViewById(R.id.mediaDescription);
            holder.txtTitle = convertView.findViewById(R.id.mediaTitle);
            holder.imageView = convertView.findViewById(R.id.mediaIcon);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.txtDesc.setText(rowItem.getDesc());
        holder.txtTitle.setText(rowItem.getTitle());
        holder.imageView.setImageBitmap(rowItem.getImageId());

//        Glide
//                .with(context)
//                .asBitmap()
//                .load(rowItem.getImageId())
//                .into(imageView);

        return convertView;
    }
}
