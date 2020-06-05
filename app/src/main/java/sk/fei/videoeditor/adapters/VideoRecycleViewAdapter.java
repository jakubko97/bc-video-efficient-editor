package sk.fei.videoeditor.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import sk.fei.videoeditor.R;
import sk.fei.videoeditor.activities.MainActivity;
import sk.fei.videoeditor.beans.RowItem;
import sk.fei.videoeditor.dialogs.About;
import sk.fei.videoeditor.dialogs.ActionDelete;

public class VideoRecycleViewAdapter extends RecyclerView.Adapter<VideoRecycleViewAdapter.ViewHolder> implements Filterable {

    Context context;
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
                listener.onRefreshData(multiSelect);
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

            txtTitle = itemView.findViewById(R.id.mediaTitle);
            //txtDesc = itemView.findViewById(R.id.mediaDescription);
            imageView = itemView.findViewById(R.id.mediaIcon);
            txtDateCreated = itemView.findViewById(R.id.mediaDateCreated);
            linearLayout = itemView.findViewById(R.id.row_item_root);
            size = itemView.findViewById(R.id.mediaSize);

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
            listener.onRefreshData(multiSelect);
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
                @Override
                public void onClick(View view) {
                    selectItem(rowItem);
                    if(!multiSelect){
                        listener.onRowItemSelected(rowItem,imageView);
                    }
                }
            });

            listener.onRefreshData(multiSelect);
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
            if(selectedItems.size() != 0){
            ActionDelete.CreateDialog(context, selectedItems, itemsFiltered, mode);
            }
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


    }

    @Override
    public int getItemCount() {
        return itemsFiltered.size();
    }

    public interface RowItemsListener {
        void onRowItemSelected(RowItem rowItem, ImageView imageView);
        void onRefreshData(boolean multiselect);
    }




}