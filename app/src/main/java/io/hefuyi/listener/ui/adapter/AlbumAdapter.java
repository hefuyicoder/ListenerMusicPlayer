package io.hefuyi.listener.ui.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
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
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.List;

import io.hefuyi.listener.Constants;
import io.hefuyi.listener.MusicPlayer;
import io.hefuyi.listener.R;
import io.hefuyi.listener.dataloader.AlbumSongLoader;
import io.hefuyi.listener.mvp.model.Album;
import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.util.ATEUtil;
import io.hefuyi.listener.util.ColorUtil;
import io.hefuyi.listener.util.ListenerUtil;
import io.hefuyi.listener.util.NavigationUtil;
import io.hefuyi.listener.util.PreferencesUtility;
import io.hefuyi.listener.widget.fastscroller.FastScrollRecyclerView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ItemHolder> implements FastScrollRecyclerView.SectionedAdapter{

    private List<Album> arraylist;
    private Activity mContext;
    private boolean isGrid;
    private String action;

    public AlbumAdapter(Activity context, String action) {
        this.mContext = context;
        this.isGrid = PreferencesUtility.getInstance(mContext).isAlbumsInGrid();
        this.action = action;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (isGrid) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_grid_layout_item, viewGroup, false);
            return new ItemHolder(v);
        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_linear_layout_item, viewGroup, false);
            return new ItemHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(final ItemHolder itemHolder, final int i) {
        Album localItem = arraylist.get(i);

        itemHolder.title.setText(localItem.title);
        itemHolder.artist.setText(localItem.artistName);
        itemHolder.songcount.setText(ListenerUtil.makeLabel(mContext, R.plurals.Nsongs, localItem.songCount));

        Glide.with(itemHolder.itemView.getContext())
                .load(ListenerUtil.getAlbumArtUri(localItem.id).toString())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        if (isGrid) {
                            itemHolder.footer.setBackgroundColor(ATEUtil.getThemeAlbumDefaultPaletteColor(mContext));
                        }
                        itemHolder.albumArt.setImageDrawable(ATEUtil.getDefaultAlbumDrawable(mContext));
                        itemHolder.title.setTextColor(ATEUtil.getThemeTextColorPrimary(mContext));
                        itemHolder.artist.setTextColor(ATEUtil.getThemeTextColorSecondly(mContext));
                        itemHolder.songcount.setTextColor(ATEUtil.getThemeTextColorSecondly(mContext));
                        itemHolder.popupMenu.setColorFilter(mContext.getResources().getColor(R.color.background_floating_material_dark));
                    }

                    @Override
                    public void onResourceReady(final Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        if (isGrid) {
                            new Palette.Builder(resource).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    Palette.Swatch swatch = ColorUtil.getMostPopulousSwatch(palette);
                                    if (swatch != null) {
                                        int color = swatch.getRgb();
                                        itemHolder.footer.setBackgroundColor(color);

                                        int detailColor = swatch.getTitleTextColor();
                                        itemHolder.albumArt.setImageBitmap(resource);
                                        itemHolder.title.setTextColor(ColorUtil.getOpaqueColor(detailColor));
                                        itemHolder.artist.setTextColor(detailColor);
                                        itemHolder.songcount.setTextColor(detailColor);
                                        itemHolder.popupMenu.setColorFilter(detailColor);
                                    }
                                }
                            });
                        } else {
                            itemHolder.albumArt.setImageBitmap(resource);
                        }
                    }
                });

        if (ListenerUtil.isLollipop())
            itemHolder.albumArt.setTransitionName("transition_album_art" + i);

        setOnPopupMenuListener(itemHolder, i);

    }

    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    public void setAlbumsList(List<Album> arraylist) {
        this.arraylist = arraylist;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        if (arraylist == null || arraylist.size() == 0)
            return "";
        Character ch = arraylist.get(position).title.charAt(0);
        if (Character.isDigit(ch)) {
            return "#";
        } else
            return Character.toString(ch);
    }

    private void setOnPopupMenuListener(final AlbumAdapter.ItemHolder itemHolder, final int position) {
        itemHolder.popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);
                int adapterPosition = itemHolder.getAdapterPosition();
                final Album album = arraylist.get(adapterPosition);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.popup_album_addto_queue:
                                getSongListIdByAlbum(arraylist.get(position).id)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<long[]>() {
                                            @Override
                                            public void call(long[] ids) {
                                                MusicPlayer.addToQueue(mContext, ids, -1, ListenerUtil.IdType.NA);
                                            }
                                        });
                                break;
                            case R.id.popup_album_addto_playlist:
                                getSongListIdByAlbum(arraylist.get(position).id)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<long[]>() {
                                            @Override
                                            public void call(long[] ids) {
                                                ListenerUtil.showAddPlaylistDialog(mContext,ids);
                                            }
                                        });
                                break;
                            case R.id.popup_album_goto_artist:
                                NavigationUtil.goToArtist(mContext, arraylist.get(position).artistId,
                                        arraylist.get(position).artistName);
                                break;
                            case R.id.popup_artist_delete:
                                switch (action) {
                                    case Constants.NAVIGATE_PLAYLIST_FAVOURATE:
                                        getSongListIdByAlbum(album.id)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Action1<long[]>() {
                                                    @Override
                                                    public void call(long[] ids) {
                                                        ListenerUtil.showDeleteFromFavourate(mContext,ids);
                                                    }
                                                });
                                        break;
                                    case Constants.NAVIGATE_PLAYLIST_RECENTPLAY:
                                        getSongListIdByAlbum(album.id)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Action1<long[]>() {
                                                    @Override
                                                    public void call(long[] ids) {
                                                        ListenerUtil.showDeleteFromRecentlyPlay(mContext,ids);
                                                    }
                                                });
                                        break;
                                    default:
                                        AlbumSongLoader.getSongsForAlbum(mContext,arraylist.get(position).id)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Action1<List<Song>>() {
                                                    @Override
                                                    public void call(List<Song> songs) {
                                                        long[] ids = new long[songs.size()];
                                                        int i = 0;
                                                        for (Song song : songs) {
                                                            ids[i] = song.id;
                                                            i++;
                                                        }
                                                        if (ids.length == 1) {
                                                            ListenerUtil.showDeleteDialog(mContext, songs.get(0).title, ids,
                                                                    new MaterialDialog.SingleButtonCallback() {
                                                                        @Override
                                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                            arraylist.remove(position);
                                                                            notifyDataSetChanged();
                                                                        }
                                                                    });
                                                        } else {
                                                            String songCount = ListenerUtil.makeLabel(mContext,
                                                                    R.plurals.Nsongs, arraylist.get(position).songCount);
                                                            ListenerUtil.showDeleteDialog(mContext, songCount, ids,
                                                                    new MaterialDialog.SingleButtonCallback() {
                                                                        @Override
                                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                            arraylist.remove(position);
                                                                            notifyDataSetChanged();
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                        break;
                                }
                                break;
                        }
                        return false;
                    }
                });
                menu.inflate(R.menu.popup_album);
                menu.show();
            }
        });
    }

    private Observable<long[]> getSongListIdByAlbum(long albumId) {
        return AlbumSongLoader.getSongsForAlbum(mContext, albumId)
                .map(new Func1<List<Song>, long[]>() {
                    @Override
                    public long[] call(List<Song> songs) {
                        long[] ids = new long[songs.size()];
                        int i = 0;
                        for (Song song : songs) {
                            ids[i] = song.id;
                            i++;
                        }
                        return ids;
                    }
                });
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private TextView artist;
        private TextView songcount;
        private ImageView albumArt;
        private ImageView popupMenu;
        private View footer;

        public ItemHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.text_item_title);
            this.artist = (TextView) view.findViewById(R.id.text_item_subtitle);
            this.songcount = (TextView) view.findViewById(R.id.text_item_subtitle_2);
            this.albumArt = (ImageView) view.findViewById(R.id.image);
            this.popupMenu = (ImageView) view.findViewById(R.id.popup_menu);
            this.footer = view.findViewById(R.id.footer);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavigationUtil.navigateToAlbum(mContext, arraylist.get(getAdapterPosition()).id,
                    arraylist.get(getAdapterPosition()).title,
                    new Pair<View, String>(albumArt, "transition_album_art" + getAdapterPosition()));
        }

    }


}



