package io.hefuyi.listener.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;
import java.util.List;

import io.hefuyi.listener.Constants;
import io.hefuyi.listener.MusicPlayer;
import io.hefuyi.listener.R;
import io.hefuyi.listener.dataloader.PlaylistLoader;
import io.hefuyi.listener.dataloader.PlaylistSongLoader;
import io.hefuyi.listener.mvp.model.Playlist;
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


/**
 * Created by hefuyi on 2016/12/4.
 */

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ItemHolder> implements FastScrollRecyclerView.SectionedAdapter{

    private List<Playlist> arraylist;
    private Fragment mFragment;
    private Context mContext;
    private boolean isGrid;

    public PlaylistAdapter(Fragment fragment, List<Playlist> arraylist) {
        if (arraylist == null) {
            this.arraylist = new ArrayList<>();
        } else {
            this.arraylist = arraylist;
        }
        this.mFragment = fragment;
        this.mContext = fragment.getContext();
        this.isGrid = PreferencesUtility.getInstance(mFragment.getContext()).getPlaylistView() == Constants.PLAYLIST_VIEW_GRID;
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
        final Playlist localItem = arraylist.get(i);

        itemHolder.title.setText(localItem.name);
        itemHolder.songcount.setText(ListenerUtil.makeLabel(mContext, R.plurals.Nsongs, localItem.songCount));
        itemHolder.subtitle1.setVisibility(View.GONE);
        itemHolder.divider.setVisibility(View.GONE);

        PlaylistSongLoader.getSongsInPlaylist(mContext, localItem.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Song>>() {
                    @Override
                    public void call(List<Song> playlistsongs) {
                        String uri = "";
                        long firstAlbumID = -1;
                        if (playlistsongs.size() != 0) {
                            firstAlbumID = playlistsongs.get(0).albumId;
                            uri = ListenerUtil.getAlbumArtUri(firstAlbumID).toString();
                        }
                        itemHolder.playlistArt.setTag(R.string.playlistArt,firstAlbumID);

                        Glide.with(itemHolder.itemView.getContext())
                                .load(uri)
                                .asBitmap()
                                .placeholder(ATEUtil.getDefaultAlbumDrawable(mContext))
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                        if (isGrid) {
                                            itemHolder.footer.setBackgroundColor(ATEUtil.getThemeAlbumDefaultPaletteColor(mContext));
                                        }
                                        itemHolder.playlistArt.setImageDrawable(ATEUtil.getDefaultAlbumDrawable(mContext));
                                        itemHolder.title.setTextColor(ATEUtil.getThemeTextColorPrimary(mContext));
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
                                                        itemHolder.playlistArt.setImageBitmap(resource);
                                                        itemHolder.title.setTextColor(ColorUtil.getOpaqueColor(detailColor));
                                                        itemHolder.songcount.setTextColor(detailColor);
                                                        itemHolder.popupMenu.setColorFilter(detailColor);
                                                    }
                                                }
                                            });
                                        } else {
                                            itemHolder.playlistArt.setImageBitmap(resource);
                                        }
                                    }
                                });
                    }
                });

        if (ListenerUtil.isLollipop())
            itemHolder.playlistArt.setTransitionName("transition_album_art" + i);

        setOnPopupMenuListener(itemHolder, i);
    }

    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    public void setPlaylist(List<Playlist> playlists) {
        this.arraylist.clear();
        this.arraylist.addAll(playlists);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        if (arraylist == null || arraylist.size() == 0)
            return "";
        return Character.toString(arraylist.get(position).name.charAt(0));
    }

    private void setOnPopupMenuListener(final ItemHolder itemHolder, final int position) {
        itemHolder.popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        final Playlist playlist = arraylist.get(itemHolder.getAdapterPosition());
                        switch (item.getItemId()) {
                            case R.id.popup_playlist_rename:
                                new MaterialDialog.Builder(mContext)
                                        .title(R.string.rename_playlist)
                                        .positiveText("确定")
                                        .negativeText(R.string.cancel)
                                        .input(null, playlist.name, false, new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                MusicPlayer.renamePlaylist(mContext, playlist.id, input.toString());
                                                itemHolder.title.setText(input.toString());
                                                Toast.makeText(mContext, R.string.rename_playlist_success, Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .show();
                                break;
                            case R.id.popup_playlist_addto_playlist:
                                getSongListIdByPlaylist(playlist.id)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<long[]>() {
                                            @Override
                                            public void call(long[] ids) {
                                                ListenerUtil.showAddPlaylistDialog(mFragment.getActivity(),ids);
                                            }
                                        });
                                break;
                            case R.id.popup_playlist_addto_queue:
                                getSongListIdByPlaylist(playlist.id)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<long[]>() {
                                            @Override
                                            public void call(long[] ids) {
                                                MusicPlayer.addToQueue(mContext, ids, -1, ListenerUtil.IdType.Playlist);
                                            }
                                        });
                                break;
                            case R.id.popup_playlist_delete:
                                new MaterialDialog.Builder(mContext)
                                        .title(R.string.delete_playlist)
                                        .positiveText(R.string.delete)
                                        .negativeText(R.string.cancel)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                PlaylistLoader.deletePlaylists(mContext, playlist.id);
                                                arraylist.remove(itemHolder.getAdapterPosition());
                                                notifyItemRemoved(itemHolder.getAdapterPosition());
                                                Toast.makeText(mContext, R.string.delete_playlist_success, Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .show();
                                break;
                        }
                        return false;
                    }
                });
                menu.inflate(R.menu.popup_playlist);
                menu.show();
            }
        });
    }

    private Observable<long[]> getSongListIdByPlaylist(long playlistId) {
        return PlaylistSongLoader.getSongsInPlaylist(mContext, playlistId)
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
        private TextView subtitle1;
        private View divider;
        private TextView songcount;
        private ImageView playlistArt;
        private View footer;
        private ImageView popupMenu;

        public ItemHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.text_item_title);
            this.subtitle1 = (TextView) view.findViewById(R.id.text_item_subtitle);
            this.divider = view.findViewById(R.id.divider_subtitle);
            this.songcount = (TextView) view.findViewById(R.id.text_item_subtitle_2);
            this.playlistArt = (ImageView) view.findViewById(R.id.image);
            this.footer = view.findViewById(R.id.footer);
            this.popupMenu = (ImageView) view.findViewById(R.id.popup_menu);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavigationUtil.navigateToPlaylistDetail(mFragment.getActivity(), arraylist.get(getAdapterPosition()).id,
                    String.valueOf(title.getText()), (long) playlistArt.getTag(R.string.playlistArt),
                    new Pair<View, String>(playlistArt, "transition_album_art" + getAdapterPosition()));
        }

    }
}
