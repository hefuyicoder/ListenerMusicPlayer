package io.hefuyi.listener.ui.adapter;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.hefuyi.listener.MusicPlayer;
import io.hefuyi.listener.R;
import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.util.ATEUtil;
import io.hefuyi.listener.util.ColorUtil;
import io.hefuyi.listener.util.ListenerUtil;

/**
 * Created by hefuyi on 2016/12/26.
 */

public class PlayqueueSongsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private int currentlyPlayingPosition;
    private List<Song> arraylist;
    private AppCompatActivity mContext;
    private long[] songIDs;
    private Palette.Swatch mSwatch;

    public PlayqueueSongsAdapter(AppCompatActivity context, List<Song> arraylist) {
        if (arraylist == null) {
            this.arraylist = new ArrayList<>();
        } else {
            this.arraylist = arraylist;
        }
        this.mContext = context;
        this.songIDs = getSongIds();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View song = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_linear_layout_item, viewGroup, false);
        return new ItemHolder(song);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemHolder itemHolder = (ItemHolder) holder;
        Song localItem;
        localItem = arraylist.get(position);

        itemHolder.title.setText(localItem.title);
        itemHolder.artist.setText(localItem.artistName);
        itemHolder.album.setText(localItem.albumName);

        if (mSwatch != null) {
            itemHolder.title.setTextColor(mSwatch.getBodyTextColor());
            itemHolder.artist.setTextColor(mSwatch.getTitleTextColor());
            itemHolder.album.setTextColor(mSwatch.getTitleTextColor());

            if (MusicPlayer.getQueuePosition() == position) {
                itemHolder.playIndicator.setVisibility(View.VISIBLE);
                itemHolder.playIndicator.setBackgroundColor(ColorUtil.getBlackWhiteColor(mSwatch.getRgb()));
            } else {
                itemHolder.playIndicator.setVisibility(View.GONE);
            }
        }

        Glide.with(holder.itemView.getContext()).load(ListenerUtil.getAlbumArtUri(localItem.albumId).toString())
                .error(ATEUtil.getDefaultAlbumDrawable(mContext))
                .placeholder(ATEUtil.getDefaultAlbumDrawable(mContext))
                .centerCrop()
                .into(itemHolder.albumArt);
    }

    @Override
    public int getItemCount() {
        return arraylist.size();
    }

    public long[] getSongIds() {
        int songNum = arraylist.size();
        long[] ret = new long[songNum];
        for (int i = 0; i < songNum; i++) {
            ret[i] = arraylist.get(i).id;
        }
        return ret;
    }

    public void setSongList(List<Song> arraylist) {
        this.arraylist = arraylist;
        this.songIDs = getSongIds();
        notifyDataSetChanged();
    }

    public void setPaletteSwatch(Palette.Swatch swatch) {
        mSwatch = swatch;
        notifyDataSetChanged();
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private TextView artist;
        private TextView album;
        private ImageView albumArt;
        private ImageView popupMenu;
        private View playIndicator;

        public ItemHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.text_item_title);
            this.artist = (TextView) view.findViewById(R.id.text_item_subtitle);
            this.album = (TextView) view.findViewById(R.id.text_item_subtitle_2);
            this.albumArt = (ImageView) view.findViewById(R.id.image);
            this.popupMenu = (ImageView) view.findViewById(R.id.popup_menu);
            this.playIndicator = view.findViewById(R.id.now_playing_indicator);

            popupMenu.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_clear_white_36dp));
            popupMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MusicPlayer.removeFromQueue(getAdapterPosition());
                    arraylist.remove(getAdapterPosition());
                    songIDs = getSongIds();
                    notifyItemRemoved(getAdapterPosition());
                }
            });
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.playAll(mContext, songIDs, getAdapterPosition(), -1, ListenerUtil.IdType.NA, false);
                    Handler handler1 = new Handler();
                    handler1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notifyItemChanged(currentlyPlayingPosition);
                            notifyItemChanged(getAdapterPosition());
                            currentlyPlayingPosition = getAdapterPosition();
                        }
                    }, 50);
                }
            }, 100);
        }
    }
}
