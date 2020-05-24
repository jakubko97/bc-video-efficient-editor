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
import sk.fei.videoeditor.activities.MediaFileRecycleView;
import sk.fei.videoeditor.beans.RowItem;

public class VideoRecycleViewAdapter extends RecyclerView.Adapter<VideoRecycleViewAdapter.ViewHolder> implements Filterable {

    Context context;
    ImageView imageView;
    private List<RowItem> items;
    private List<RowItem> itemsFiltered;
    private RowItemsListener listener;
    private boolean isActionMode;
    int itemLayoutId;

    ActionMode actionMode;

    private boolean multiSelect = false;
    private ArrayList<RowItem> selectedItems = new ArrayList<>();

    public int getSelectedItemsSize() {
        return selectedItems.size();
    }

    public List<RowItem> getItemsFiltered() {
        return itemsFiltered;
    }

    public VideoRecycleViewAdapter (Context context, int itemLayoutId,
                                     List<RowItem> items, RowItemsListener listener, boolean isActionMode) {
        this.context = context;
        this.items = items;
        this.itemsFiltered = items;
        this.listener = listener;
        this.itemLayoutId = itemLayoutId;
        this.isActionMode = isActionMode;
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
                listener.onRefreshData();
            }
        };
    }



    /*private view holder class*/
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView txtTitle,txtDesc,txtDateCreated,size;
        LinearLayout linearLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            if(itemLayoutId == R.layout.gallery_view){

                txtTitle = itemView.findViewById(R.id.mediaTitle);
                imageView = itemView.findViewById(R.id.mediaIcon);
                linearLayout = itemView.findViewById(R.id.row_item_root);

            } else if(itemLayoutId == R.layout.audio){

            txtTitle = itemView.findViewById(R.id.mediaTitle);
            //txtDesc = itemView.findViewById(R.id.mediaDescription);
            imageView = itemView.findViewById(R.id.mediaIcon);
            txtDateCreated = itemView.findViewById(R.id.mediaDateCreated);
            linearLayout = itemView.findViewById(R.id.row_item_root);
            size = itemView.findViewById(R.id.mediaSize);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    listener.onRowItemSelected(itemsFiltered.get(getAdapterPosition()));
                }
            });
        }

        // Method in ViewHolder class
        private void bind(RowItem rowItem) {
            // Get the state
            // Set the visibility based on state
            if(itemLayoutId == R.layout.audio){
                //txtDesc.setText(rowItem.getDesc());
                size.setText(rowItem.getSize());

                SimpleDateFormat timeStampFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                String dateStr = timeStampFormat.format(rowItem.getDateCreated());

                dateStr = dateStr.replace("-", ".");

                txtDateCreated.setText(dateStr);
            }

            txtTitle.setText(rowItem.getTitle());


                Glide
                        .with(context)
                        .load(rowItem.getFile())
                        .error(R.drawable.broken_image_foreground)
                        .override(140,100)
                        .transition(DrawableTransitionOptions.withCrossFade(750))
                        .into(imageView);

        }

        void selectItem(RowItem rowItem) {
            if (multiSelect) {
                Resources res = context.getResources();
                if (selectedItems.contains(rowItem)) {
                    selectedItems.remove(rowItem);
                    linearLayout.setBackgroundColor(Color.WHITE);
                } else {
                    selectedItems.add(rowItem);
                    linearLayout.setBackgroundColor(Color.LTGRAY);
                }
                String songsFound = res.getQuantityString(R.plurals.numberOfSelectedFile, getSelectedItemsSize(),getSelectedItemsSize());
                actionMode.setTitle(songsFound);
            }
        }

        void update(final RowItem rowItem) {
            //txtTitle.setText(rowItem.getTitle() + "");
            if (selectedItems.contains(rowItem)) {
                linearLayout.setBackgroundColor(Color.LTGRAY);
            } else {
                linearLayout.setBackgroundColor(Color.WHITE);
            }
            Log.d("adapter", "update called");

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(isActionMode){
                    actionMode = ((AppCompatActivity)view.getContext()).startSupportActionMode(actionModeCallbacks);
                    }
                    selectItem(rowItem);
                    return true;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.Q)
                @Override
                public void onClick(View view) {
                    selectItem(rowItem);
                    if(!multiSelect){
                        listener.onRowItemSelected(itemsFiltered.get(getAdapterPosition()));
                    }
                }
            });

            listener.onRefreshData();
        }

    }

    private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            multiSelect = true;
            menu.add("DELETE");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            for (RowItem rowItem : selectedItems) {
                itemsFiltered.remove(rowItem);
                rowItem.getFile().delete();
            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            multiSelect = false;
            selectedItems.clear();
            notifyDataSetChanged();

        }
    };



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

//        String songsFound = context.getResources().getQuantityString(R.plurals.numberOfSongsAvailable, getItemCount(),getItemCount());
//        ((AppCompatActivity)viewHolder.itemView.getContext()).getSupportActionBar().setSubtitle(songsFound);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data model based on position
        RowItem rowItem = itemsFiltered.get(position);

        // Set item views based on your views and data model
        holder.bind(rowItem);
        holder.update(rowItem);

        Log.d("rowItem", "title = " +rowItem.getTitle());
        Log.d("rowItem", "item count  = " + getItemCount());

//        String songsFound = context.getResources().getQuantityString(R.plurals.numberOfSongsAvailable, getItemCount(),getItemCount());
//        ((AppCompatActivity)holder.itemView.getContext()).getSupportActionBar().setSubtitle(songsFound);
    }

    @Override
    public int getItemCount() {
        return itemsFiltered.size();
    }

    public interface RowItemsListener {
        void onRowItemSelected(RowItem rowItem);
        void onRefreshData();
    }




}