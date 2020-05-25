package sk.fei.videoeditor.adapters;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

import sk.fei.videoeditor.R;
import sk.fei.videoeditor.beans.Album;
import sk.fei.videoeditor.beans.RowItem;

public class FolderRecycleViewAdapter extends RecyclerView.Adapter<FolderRecycleViewAdapter.ViewHolder> implements Filterable {

    Context context;
    ImageView imageView;
    private List<RowItem> items;
    private List<Album> albums;
    private List<RowItem> itemsFiltered;
    private RowItemsListener listener;
    int itemLayoutId;

    private ArrayList<RowItem> selectedItems = new ArrayList<>();

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public FolderRecycleViewAdapter (Context context, int itemLayoutId,
                                     List<Album> albums, RowItemsListener listener) {
        this.context = context;
        this.albums = albums;
        this.itemsFiltered = items;
        this.listener = listener;
        this.itemLayoutId = itemLayoutId;
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
        TextView txtTitle,mediaCount;
        LinearLayout linearLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.mediaTitle);
            mediaCount = itemView.findViewById(R.id.mediaCount);
            imageView = itemView.findViewById(R.id.mediaIcon);
            linearLayout = itemView.findViewById(R.id.parent);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    // send selected contact in callback
//                    listener.onRowItemSelected(albums.get(getAdapterPosition()), imageView);
//
//                    Log.d("click",albums.get(getAdapterPosition()) + "click");
//                }
//            });
        }

        // Method in ViewHolder class
        void bind(final Album album) {
            // Get the state
            txtTitle.setText(album.getName());
            String songsFound = context.getResources().getQuantityString(R.plurals.numberOfFiles, album.getRowItems().size(),album.getRowItems().size());

            mediaCount.setText(songsFound);
            Glide
                    .with(context)
                    .load(album.getRowItems().get(0).getFile())
                    .error(R.drawable.broken_image_foreground)
                    .override(140,100)
                    .transition(DrawableTransitionOptions.withCrossFade(750))
                    .into(imageView);

        }

        void update(Album album){

            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onRowItemSelected(album, imageView);

                    Log.d("click",albums.get(getAdapterPosition()) + "click2");
                }
            });
        }

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        //LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the custom layout
        //View contactView = inflater.inflate(R.layout.item_audio, parent, false);
        View view = LayoutInflater.from(context).inflate(itemLayoutId, null);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(view);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data model based on position
        Album album = albums.get(position);

        // Set item views based on your views and data model

        holder.bind(album);
        holder.update(album);

    }

    public interface RowItemsListener {
        void onRowItemSelected(Album album, ImageView imageView);
    }

}
