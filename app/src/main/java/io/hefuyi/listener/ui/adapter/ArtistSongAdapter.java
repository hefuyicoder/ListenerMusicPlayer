package io.hefuyi.listener.ui.adapter;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import io.hefuyi.listener.MusicPlayer;
import io.hefuyi.listener.R;
import io.hefuyi.listener.dataloader.ArtistAlbumLoader;
import io.hefuyi.listener.mvp.model.Album;
import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.util.ATEUtil;
import io.hefuyi.listener.util.ListenerUtil;
import io.hefuyi.listener.util.NavigationUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by hefuyi on 2016/11/24.
 */

public class ArtistSongAdapter extends RecyclerView.Adapter<ArtistSongAdapter.ItemHolder>{

    private List<Song> arraylist;
    private Activity mContext;
    private long artistID;
    private long[] songIDs;

    public ArtistSongAdapter(Activity context, List<Song> arraylist, long artistID) {
        this.arraylist = arraylist;
        this.mContext = context;
        this.artistID = artistID;
        if (arraylist!=null){
            this.songIDs = getSongIds();
        }
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == 0) {
            View v0 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.artist_detail_albums_header, viewGroup, false);
            return new ItemHolder(v0);
        } else {
            View v2 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_linear_layout_item, viewGroup, false);
            return new ItemHolder(v2);
        }
    }

    @Override
    public void onBindViewHolder(ItemHolder itemHolder, int i) {

        if (getItemViewType(i) == 0) {
            //nothing
            setUpAlbums(itemHolder.albumsRecyclerView);
        } else {
            Song localItem = arraylist.get(i);
            itemHolder.title.setText(localItem.title);
            itemHolder.artist.setText(localItem.artistName);
            itemHolder.album.setText(localItem.albumName);

            Glide.with(mContext)
                    .load(ListenerUtil.getAlbumArtUri(localItem.albumId).toString())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .error(ATEUtil.getDefaultAlbumDrawable(mContext))
                    .centerCrop()
                    .into(itemHolder.albumArt);

            if (MusicPlayer.getCurrentAudioId() == localItem.id) {
                itemHolder.title.setTextColor(ATEUtil.getThemeAccentColor(mContext));
            } else {
                itemHolder.title.setTextColor(ATEUtil.getThemeTextColorPrimary(mContext));
            }

            setOnPopupMenuListener(itemHolder, i - 1);
        }

    }

    @Override
    public void onViewRecycled(ItemHolder itemHolder) {

        if (itemHolder.getItemViewType() == 0)
            clearExtraSpacingBetweenCards(itemHolder.albumsRecyclerView);

    }

    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    @Override
    public int getItemViewType(int position) {
        int viewType;
        if (position == 0) {
            viewType = 0;
        } else viewType = 1;
        return viewType;
    }

    public void setSongList(List<Song> songList) {
        this.arraylist = songList;
        this.songIDs = getSongIds();
        notifyDataSetChanged();
    }

    private void setUpAlbums(final RecyclerView albumsRecyclerview) {

        albumsRecyclerview.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        albumsRecyclerview.setHasFixedSize(true);

        //to add spacing between cards
        int spacingInPixels = mContext.getResources().getDimensionPixelSize(R.dimen.spacing_card);
        albumsRecyclerview.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        albumsRecyclerview.setNestedScrollingEnabled(false);

        ArtistAlbumLoader.getAlbumsForArtist(mContext, artistID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Album>>() {
                    @Override
                    public void call(List<Album> albumList) {
                        ArtistAlbumAdapter mAlbumAdapter = new ArtistAlbumAdapter(mContext,albumList);
                        albumsRecyclerview.setAdapter(mAlbumAdapter);
                    }
                });

    }

    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

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
                                ids[0] = arraylist.get(position + 1).id;
                                MusicPlayer.playNext(mContext, ids, -1, ListenerUtil.IdType.NA);
                                break;
                            case R.id.popup_song_addto_playlist:
                                ListenerUtil.showAddPlaylistDialog(mContext,new long[]{arraylist.get(position + 1).id});
                                break;
                            case R.id.popup_song_addto_queue:
                                long[] id = new long[1];
                                id[0] = arraylist.get(position + 1).id;
                                MusicPlayer.addToQueue(mContext, id, -1, ListenerUtil.IdType.NA);
                                break;
                            case R.id.popup_song_goto_album:
                                NavigationUtil.goToAlbum(mContext, arraylist.get(position + 1).albumId,
                                        arraylist.get(position + 1).title);
                                break;
                            case R.id.popup_song_delete:
                                long[] deleteIds = {arraylist.get(position + 1).id};
                                ListenerUtil.showDeleteDialog(mContext, arraylist.get(position + 1).title, deleteIds,
                                        new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                arraylist.remove(position + 1);
                                                songIDs = getSongIds();
                                                notifyDataSetChanged();
                                            }
                                        });
                                break;
                        }
                        return false;
                    }
                });
                menu.inflate(R.menu.popup_song);
                menu.getMenu().findItem(R.id.popup_song_goto_artist).setVisible(false);
                menu.show();
            }
        });
    }

    private void clearExtraSpacingBetweenCards(RecyclerView albumsRecyclerview) {
        //to clear any extra spacing between cards
        int spacingInPixelstoClear = -(mContext.getResources().getDimensionPixelSize(R.dimen.spacing_card));
        albumsRecyclerview.addItemDecoration(new SpacesItemDecoration(spacingInPixelstoClear));

    }

    public long[] getSongIds() {
        List<Song> actualArraylist = new ArrayList<Song>(arraylist);
        actualArraylist.remove(0);
        long[] ret = new long[actualArraylist.size()];
        for (int i = 0; i < actualArraylist.size(); i++) {
            ret[i] = actualArraylist.get(i).id;
        }
        return ret;
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView artist;
        TextView album;
        ImageView albumArt;
        ImageView popupMenu;
        RecyclerView albumsRecyclerView;

        public ItemHolder(View view) {
            super(view);

            this.albumsRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_album);

            this.title = (TextView) view.findViewById(R.id.text_item_title);
            this.artist = (TextView) view.findViewById(R.id.text_item_subtitle);
            this.album = (TextView) view.findViewById(R.id.text_item_subtitle_2);
            this.albumArt = (ImageView) view.findViewById(R.id.image);
            this.popupMenu = (ImageView) view.findViewById(R.id.popup_menu);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.playAll(mContext, songIDs, getAdapterPosition() - 1, artistID, ListenerUtil.IdType.Artist, false);
                }
            }, 100);
        }

    }

    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {

            //the padding from left
            outRect.left = space;


        }
    }
}
