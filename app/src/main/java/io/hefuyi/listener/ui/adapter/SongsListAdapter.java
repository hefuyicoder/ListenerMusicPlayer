package io.hefuyi.listener.ui.adapter;

import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import io.hefuyi.listener.Constants;
import io.hefuyi.listener.MusicPlayer;
import io.hefuyi.listener.R;
import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.util.ATEUtil;
import io.hefuyi.listener.util.DensityUtil;
import io.hefuyi.listener.util.ListenerUtil;
import io.hefuyi.listener.util.NavigationUtil;
import io.hefuyi.listener.widget.fastscroller.FastScrollRecyclerView;

public class SongsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    public int currentlyPlayingPosition;
    private List<Song> arraylist;
    private AppCompatActivity mContext;
    private long[] songIDs;
    private boolean withHeader;
    private float topPlayScore;
    private String action;

    public SongsListAdapter(AppCompatActivity context, List<Song> arraylist, String action, boolean withHeader) {
        if (arraylist == null) {
            this.arraylist = new ArrayList<>();
        } else {
            this.arraylist = arraylist;

        }
        this.mContext = context;
        this.songIDs = getSongIds();
        this.withHeader = withHeader;
        this.action = action;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && withHeader) {
            return Type.TYPE_PLAY_SHUFFLE;
        } else {
            return Type.TYPE_SONG;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            RecyclerView.ViewHolder viewHolder = null;
            switch (viewType) {
                case Type.TYPE_PLAY_SHUFFLE:
                    View playShuffle = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_play_shuffle, viewGroup, false);
                    ImageView imageView = (ImageView) playShuffle.findViewById(R.id.play_shuffle);
                    imageView.getDrawable().setColorFilter(ATEUtil.getThemeAccentColor(mContext), PorterDuff.Mode.SRC_IN);
                    viewHolder = new PlayShuffleViewHoler(playShuffle);
                    break;
                case Type.TYPE_SONG:
                    View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_linear_layout_item, viewGroup, false);
                    viewHolder = new ItemHolder(v);
                    break;
            }
            return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case Type.TYPE_PLAY_SHUFFLE:
                break;
            case Type.TYPE_SONG:
                ItemHolder itemHolder = (ItemHolder) holder;
                Song localItem;
                if (withHeader){
                    localItem = arraylist.get(position - 1);
                }else {
                    localItem = arraylist.get(position);
                }

                itemHolder.title.setText(localItem.title);
                itemHolder.artist.setText(localItem.artistName);
                itemHolder.album.setText(localItem.albumName);

                Glide.with(holder.itemView.getContext()).load(ListenerUtil.getAlbumArtUri(localItem.albumId).toString())
                        .error(ATEUtil.getDefaultAlbumDrawable(mContext))
                        .placeholder(ATEUtil.getDefaultAlbumDrawable(mContext))
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .centerCrop()
                        .into(itemHolder.albumArt);

                if (MusicPlayer.getCurrentAudioId() == localItem.id) {
                    itemHolder.title.setTextColor(ATEUtil.getThemeAccentColor(mContext));
                } else {
                    itemHolder.title.setTextColor(ATEUtil.getThemeTextColorPrimary(mContext));
                }

                if (topPlayScore != 0) {
                    itemHolder.playscore.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) itemHolder.playscore.getLayoutParams();
                    int full = DensityUtil.getScreenWidth(mContext);
                    layoutParams.width = (int) (full * (localItem.getPlayCountScore() / topPlayScore));
                }

                setOnPopupMenuListener(itemHolder, position);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (withHeader && arraylist.size() != 0) {
            return (null != arraylist ? arraylist.size() + 1 : 0);
        } else {
            return (null != arraylist ? arraylist.size() : 0);
        }
    }

    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

        final int realSongPosition;
        if (withHeader) {
            realSongPosition = position - 1;
        } else {
            realSongPosition = position;
        }

        itemHolder.popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.popup_song_play_next:
                                long[] ids = new long[1];
                                ids[0] = arraylist.get(realSongPosition).id;
                                MusicPlayer.playNext(mContext, ids, -1, ListenerUtil.IdType.NA);
                                break;
                            case R.id.popup_song_goto_album:
                                NavigationUtil.goToAlbum(mContext, arraylist.get(realSongPosition).albumId,
                                        arraylist.get(realSongPosition).title);
                                break;
                            case R.id.popup_song_goto_artist:
                                NavigationUtil.goToArtist(mContext, arraylist.get(realSongPosition).artistId,
                                        arraylist.get(realSongPosition).artistName);
                                break;
                            case R.id.popup_song_addto_queue:
                                long[] id = new long[1];
                                id[0] = arraylist.get(realSongPosition).id;
                                MusicPlayer.addToQueue(mContext, id, -1, ListenerUtil.IdType.NA);
                                break;
                            case R.id.popup_song_addto_playlist:
                                ListenerUtil.showAddPlaylistDialog(mContext,new long[]{arraylist.get(realSongPosition).id});
                                break;
                            case R.id.popup_song_delete:
                                long[] deleteIds = {arraylist.get(realSongPosition).id};
                                switch (action) {
                                    case Constants.NAVIGATE_PLAYLIST_FAVOURATE:
                                        ListenerUtil.showDeleteFromFavourate(mContext,deleteIds);
                                        break;
                                    case Constants.NAVIGATE_PLAYLIST_RECENTPLAY:
                                        ListenerUtil.showDeleteFromRecentlyPlay(mContext,deleteIds);
                                        break;
                                    default:
                                        ListenerUtil.showDeleteDialog(mContext, arraylist.get(realSongPosition).title, deleteIds,
                                                new MaterialDialog.SingleButtonCallback() {
                                                    @Override
                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                        arraylist.remove(realSongPosition);
                                                        songIDs = getSongIds();
                                                        notifyItemRemoved(position);
                                                    }
                                                });
                                        break;
                                }
                                break;
                        }
                        return false;
                    }
                });
                menu.inflate(R.menu.popup_song);
                menu.show();
            }
        });
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
        if (arraylist.size() != 0) {
            this.topPlayScore = arraylist.get(0).getPlayCountScore();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        if (arraylist == null || arraylist.size() == 0||(withHeader && position == 0))
            return "";

        if (withHeader){
            position = position - 1;
        }
        Character ch = arraylist.get(position).title.charAt(0);
        if (Character.isDigit(ch)) {
            return "#";
        } else
            return Character.toString(ch);
    }

    public static class Type {
        public static final int TYPE_PLAY_SHUFFLE=0;
        public static final int TYPE_SONG = 1;
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private TextView artist;
        private TextView album;
        private ImageView albumArt;
        private ImageView popupMenu;
        private View playscore;

        public ItemHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.text_item_title);
            this.artist = (TextView) view.findViewById(R.id.text_item_subtitle);
            this.album = (TextView) view.findViewById(R.id.text_item_subtitle_2);
            this.albumArt = (ImageView) view.findViewById(R.id.image);
            this.popupMenu = (ImageView) view.findViewById(R.id.popup_menu);
            this.playscore = view.findViewById(R.id.playscore);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.playAll(mContext, songIDs, getAdapterPosition() - 1, -1, ListenerUtil.IdType.NA, false);
                }
            }, 100);

        }
    }

    public class PlayShuffleViewHoler extends RecyclerView.ViewHolder implements View.OnClickListener {
        public PlayShuffleViewHoler(View view) {
            super(view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.playAll(mContext, songIDs, -1, -1, ListenerUtil.IdType.NA, true);
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


