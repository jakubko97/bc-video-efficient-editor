package sk.fei.videoeditor.adapters;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
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
import android.widget.SeekBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import sk.fei.videoeditor.R;
import sk.fei.videoeditor.animations.Animations;
import sk.fei.videoeditor.beans.RowItem;

public class AudioRecycleViewAdapter extends RecyclerView.Adapter<AudioRecycleViewAdapter.ViewHolder> implements Filterable {


    Context context;
    private List<RowItem> items;
    private List<RowItem> itemsFiltered;
    private RowItemsListener listener;
    int itemLayoutId;
    MediaPlayer mediaPlayer;

    RowItem lastExpandedItem;
    LinearLayout lastExpandedLayout;
    ExtendedFloatingActionButton lastAddAudio;

    public AudioRecycleViewAdapter(Context context, int itemLayout,
                                   List<RowItem> items, RowItemsListener listener) {
        this.context = context;
        this.items = items;
        this.itemsFiltered = items;
        this.listener = listener;
        this.itemLayoutId = itemLayout;
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
        LinearLayout linearLayout, layoutExpand;
        ImageView imageView;
        TextView txtTitle,txtDesc,txtDateCreated,size, currentPosition, duration;
        MaterialButton audioPlay;
        ExtendedFloatingActionButton addAudio;
        SeekBar seekBar;
        Handler handler;
        Runnable runnable;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.mediaTitle);
            imageView = itemView.findViewById(R.id.mediaIcon);
                addAudio = itemView.findViewById(R.id.addAudio);
                linearLayout = itemView.findViewById(R.id.parent);

                layoutExpand = itemView.findViewById(R.id.layoutExpand);
                layoutExpand.setTag(this);
                Log.d("tag", layoutExpand.getTag().toString());
                duration = itemView.findViewById(R.id.durationAudio);
                currentPosition = itemView.findViewById(R.id.currentPositionAudio);
                seekBar = itemView.findViewById(R.id.audioSeekBar);
                audioPlay = itemView.findViewById(R.id.mediaPlay);
                handler = new Handler();

                addAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onRowItemSelected(itemsFiltered.get(getAdapterPosition()));
                        itemsFiltered.get(getAdapterPosition()).setExpanded(false);
                    }
                });

                audioPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playSong();
                    }
                });

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                        if(fromTouch){
                            mediaPlayer.seekTo(progress);
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });


        }

        // Method in ViewHolder class
        private void bind(RowItem rowItem) {
            // Get the state
            // Set the visibility based on state

            txtTitle.setText(rowItem.getTitle());

            itemView.setTag(rowItem);
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.mipmap.audio);

                imageView.setMaxWidth(55);
                imageView.setMaxWidth(55);


                Glide
                        .with(context)
                        .load("")
                        .apply(requestOptions)
                        .into(imageView);


        }

        private void changeSeekBar() {
            if(mediaPlayer != null){
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                currentPosition.setText(getTime(mediaPlayer.getCurrentPosition()));

                if(mediaPlayer.isPlaying()){
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            changeSeekBar();
                        }
                    };
                    handler.postDelayed(runnable,1000);
                }else{
                    audioPlay.setBackgroundResource(R.drawable.play_foreground);
                }
            }
        }

        public void playSong() {

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                audioPlay.setBackgroundResource(R.drawable.play_foreground);
            }
            else {
                mediaPlayer.start();
                audioPlay.setBackgroundResource(R.drawable.pause_foreground);
                changeSeekBar();
            }
        }

//        void selectItem(RowItem rowItem) {
//            if (multiSelect) {
//                Resources res = context.getResources();
//                if (selectedItems.contains(rowItem)) {
//                    selectedItems.remove(rowItem);
//                    linearLayout.setBackgroundColor(Color.WHITE);
//                } else {
//                    selectedItems.add(rowItem);
//                    linearLayout.setBackgroundColor(Color.LTGRAY);
//                }
//                String songsFound = res.getQuantityString(R.plurals.numberOfSelectedFile, getSelectedItemsSize(),getSelectedItemsSize());
//                actionMode.setTitle(songsFound);
//            }
//        }

        void update(final RowItem rowItem) {
            linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return true;
                }
            });
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        Log.d("rowItem", rowItem.getFile().getAbsolutePath()  + " title" + rowItem.getTitle());
                    Log.d("rowItem", view.toString());

                    layoutExpand.getTag().equals(rowItem);


                    boolean show = false;
                        show = toggleLayout(!rowItem.isExpanded(), addAudio, layoutExpand);

                    rowItem.setExpanded(show);

                        if(lastExpandedItem != null){
                            if(!lastExpandedItem.equals(rowItem)){
                                lastExpandedItem.setExpanded(false);
                                    toggleLayout(lastExpandedItem.isExpanded(), lastAddAudio, lastExpandedLayout);

                                //Animations.collapse(lastExpandedLayout);
                            clearMediaPlayer();
                            initAudioPlayer(rowItem);
                           }else{
                                if(show){
                                    initAudioPlayer(rowItem);
                                }else{
                                    clearMediaPlayer();
                                }
                            }
                        }else{
                            initAudioPlayer(rowItem);
                        }

                        lastExpandedItem = rowItem;
                        lastExpandedLayout = layoutExpand;
                        lastAddAudio = addAudio;


                }
            });

    if(rowItem.isExpanded()){
        Animations.expand(layoutExpand);
    Animations.fadeView(addAudio, true);
    }else{
    Animations.collapse(layoutExpand);
    Animations.fadeView(addAudio, false);
    }

            listener.onRefreshData();
        }

        public void initAudioPlayer(RowItem rowItem){
            if(mediaPlayer == null){
                mediaPlayer = new MediaPlayer();
            }
            try {
                mediaPlayer.setDataSource(rowItem.getFile().getAbsolutePath());
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            duration.setText(getTime(mediaPlayer.getDuration()));
            seekBar.setMax(mediaPlayer.getDuration());
        }

        private void clearMediaPlayer() {
            if(mediaPlayer != null){
                mediaPlayer.stop();
                mediaPlayer.release();
                audioPlay.setBackgroundResource(R.drawable.play_foreground);
                runnable = null;
                mediaPlayer = null;
            }
        }
    }



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
        RowItem rowItem = itemsFiltered.get(position);

        holder.bind(rowItem);
        holder.update(rowItem);

    }

    @Override
    public int getItemCount() {
        return itemsFiltered.size();
    }

    public interface RowItemsListener {
        void onRowItemSelected(RowItem rowItem);
        void onRefreshData();
    }

    public void onPauseMediaPlayer(){
        if(mediaPlayer != null){
            mediaPlayer.pause();
        }
    }

    public void onRestartMediaPLayer(){
        if(mediaPlayer != null){
           if(lastExpandedItem.isExpanded()){
              toggleLayout(lastExpandedItem.isExpanded(),lastAddAudio, lastExpandedLayout);
              Log.d("adapter", "Last expanded: "+ lastExpandedItem.getTitle());
           }
        }
    }

    private boolean toggleLayout(boolean isExpanded, ExtendedFloatingActionButton b, LinearLayout layoutExpand) {
        Animations.fadeView(b, isExpanded);
        if (isExpanded) {
            Animations.expand(layoutExpand);
        } else {
            Animations.collapse(layoutExpand);
        }
        return isExpanded;

    }

}
