package sk.fei.videoeditor.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.inputmethodservice.Keyboard;
import android.media.Image;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import sk.fei.videoeditor.R;
import sk.fei.videoeditor.beans.RowItem;

public class CustomRecycleViewAdapter extends RecyclerView.Adapter<CustomRecycleViewAdapter.ViewHolder> implements Filterable {

    Context context;
    ImageView imageView;
    private List<RowItem> items;
    private List<RowItem> itemsFiltered;
    private RowItemsListener listener;

    public List<RowItem> getItemsFiltered() {
        return itemsFiltered;
    }

    public CustomRecycleViewAdapter (Context context, int resourceId,
                                     List<RowItem> items, RowItemsListener listener) {
        this.context = context;
        this.items = items;
        this.itemsFiltered = items;
        this.listener = listener;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    itemsFiltered = items;
                } else {
                    List<RowItem> filteredList = new ArrayList<>();
                    for (RowItem row : items) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase()) || row.getTitle().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    itemsFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = itemsFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                itemsFiltered = (ArrayList<RowItem>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }



    /*private view holder class*/
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView txtTitle;
        TextView txtDesc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.mediaTitle);
            txtDesc = itemView.findViewById(R.id.mediaDescription);
            imageView = itemView.findViewById(R.id.mediaIcon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    listener.onRowItemSelected(itemsFiltered.get(getAdapterPosition()));
                }
            });
        }


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.audio, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data model based on position
        RowItem rowItem = itemsFiltered.get(position);

        // Set item views based on your views and data model
        TextView txtTitle = holder.txtTitle;
        TextView txtDesc = holder.txtDesc;
        ImageView image = holder.imageView;

        txtTitle.setText(rowItem.getTitle());
        txtDesc.setText(rowItem.getDesc());


        if(rowItem.getFile().getName().endsWith(context.getResources().getString(R.string.audio_suffix))){
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.album_foreground);

            image.setMaxWidth(55);
            image.setMaxWidth(55);

            Glide
                    .with(context)
                    .load("")
                    .apply(requestOptions)
                    .into(image);
        }else {
            Glide
                    .with(context)
                    .asBitmap()
                    .load(rowItem.getFile())
                    .error(R.drawable.broken_image_foreground)
                    .into(image);
        }

    }

    @Override
    public int getItemCount() {
        return itemsFiltered.size();
    }

    public interface RowItemsListener {
        void onRowItemSelected(RowItem rowItem);
    }

//    public View getView(int position, View convertView, ViewGroup parent) {
//        RowItem rowItem = getItem(position);
//        ViewHolder holder;
//
//        LayoutInflater mInflater = (LayoutInflater) context
//                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//        if (convertView == null) {
//            convertView = mInflater.inflate(R.layout.audio, null);
//            holder = new ViewHolder();
//            holder.txtDesc = (TextView) convertView.findViewById(R.id.mediaDescription);
//            holder.txtTitle = (TextView) convertView.findViewById(R.id.mediaTitle);
//            holder.imageView = (ImageView) convertView.findViewById(R.id.mediaIcon);
//            convertView.setTag(holder);
//        } else
//            holder = (ViewHolder) convertView.getTag();
//
//        holder.txtDesc.setText(rowItem.getDesc());
//        holder.txtTitle.setText(rowItem.getTitle());
//        holder.imageView.setImageBitmap(rowItem.getImageId());
//
////        Glide
////                .with(context)
////                .asBitmap()
////                .load(rowItem.getImageId())
////                .into(imageView);
//
//        return convertView;
//    }
}
