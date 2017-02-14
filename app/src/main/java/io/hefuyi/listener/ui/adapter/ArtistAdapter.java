package io.hefuyi.listener.ui.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;

import java.util.List;

import javax.inject.Inject;

import io.hefuyi.listener.Constants;
import io.hefuyi.listener.ListenerApp;
import io.hefuyi.listener.MusicPlayer;
import io.hefuyi.listener.R;
import io.hefuyi.listener.api.model.ArtistInfo;
import io.hefuyi.listener.api.model.Artwork;
import io.hefuyi.listener.dataloader.ArtistSongLoader;
import io.hefuyi.listener.injector.component.ApplicationComponent;
import io.hefuyi.listener.injector.component.ArtistInfoComponent;
import io.hefuyi.listener.injector.component.DaggerArtistInfoComponent;
import io.hefuyi.listener.injector.module.ArtistInfoModule;
import io.hefuyi.listener.mvp.model.Artist;
import io.hefuyi.listener.mvp.model.ArtistArt;
import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.mvp.usecase.GetArtistInfo;
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


public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ItemHolder> implements FastScrollRecyclerView.SectionedAdapter{

    @Inject
    GetArtistInfo getArtistInfo;
    private List<Artist> arraylist;
    private Activity mContext;
    private boolean isGrid;
    private String action;

    public ArtistAdapter(Activity context, List<Artist> arraylist) {
        this.arraylist = arraylist;
        this.mContext = context;
        this.isGrid = PreferencesUtility.getInstance(mContext).isArtistsInGrid();
        injectDependences(context);
    }

    public ArtistAdapter(Activity context, String action) {
        this.mContext = context;
        this.isGrid = PreferencesUtility.getInstance(mContext).isArtistsInGrid();
        this.action = action;
        injectDependences(context);
    }

    private void injectDependences(Activity context) {
        ApplicationComponent applicationComponent = ((ListenerApp) context.getApplication()).getApplicationComponent();
        ArtistInfoComponent artistInfoComponent = DaggerArtistInfoComponent.builder()
                .applicationComponent(applicationComponent)
                .artistInfoModule(new ArtistInfoModule())
                .build();
        artistInfoComponent.injectForAdapter(this);
    }

    public void setArtistList(List<Artist> arraylist) {
        this.arraylist = arraylist;
        notifyDataSetChanged();
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
    public void onBindViewHolder(final ItemHolder itemHolder, int i) {
        final Artist localItem = arraylist.get(i);

        itemHolder.name.setText(localItem.name);
        itemHolder.albumCount.setText(ListenerUtil.makeLabel(mContext, R.plurals.Nalbums, localItem.albumCount));
        itemHolder.songCount.setText(ListenerUtil.makeLabel(mContext, R.plurals.Nsongs, localItem.songCount));

        String artistArtJson = PreferencesUtility.getInstance(mContext).getArtistArt(localItem.id);
        if (TextUtils.isEmpty(artistArtJson)) {
            getArtistInfo.execute(new GetArtistInfo.RequestValues(localItem.name))
                    .getArtistInfo()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorReturn(new Func1<Throwable, ArtistInfo>() {
                        @Override
                        public ArtistInfo call(Throwable throwable) {
                            Toast.makeText(itemHolder.itemView.getContext(), R.string.load_artist_fail, Toast.LENGTH_SHORT).show();
                            return null;
                        }
                    })
                    .subscribe(new Action1<ArtistInfo>() {
                        @Override
                        public void call(ArtistInfo artistInfo) {
                            if (artistInfo != null && artistInfo.mArtist != null && artistInfo.mArtist.mArtwork != null) {
                                List<Artwork> artworks = artistInfo.mArtist.mArtwork;
                                ArtistArt artistArt = new ArtistArt(artworks.get(0).mUrl, artworks.get(1).mUrl,
                                        artworks.get(2).mUrl, artworks.get(3).mUrl);
                                PreferencesUtility.getInstance(mContext).setArtistArt(localItem.id, new Gson().toJson(artistArt));
                                loadArtistArt(artistArt, itemHolder);
                            }
                        }
                    });

        }else {
            ArtistArt artistArt = new Gson().fromJson(artistArtJson, ArtistArt.class);
            loadArtistArt(artistArt, itemHolder);
        }

        if (ListenerUtil.isLollipop())
            itemHolder.artistImage.setTransitionName("transition_artist_art" + i);

        setOnPopupMenuListener(itemHolder, i);

    }

    private void loadArtistArt(ArtistArt artistArt, final ItemHolder itemHolder) {
        if (isGrid) {
            Glide.with(mContext)
                    .load(artistArt.getExtralarge())
                    .asBitmap()
                    .placeholder(ATEUtil.getDefaultSingerDrawable(mContext))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            itemHolder.artistImage.setImageDrawable(ATEUtil.getDefaultSingerDrawable(mContext));
                            itemHolder.name.setTextColor(ATEUtil.getThemeTextColorPrimary(mContext));
                            itemHolder.albumCount.setTextColor(ATEUtil.getThemeTextColorSecondly(mContext));
                            itemHolder.songCount.setTextColor(ATEUtil.getThemeTextColorSecondly(mContext));
                            itemHolder.popupMenu.setColorFilter(mContext.getResources().getColor(R.color.background_floating_material_dark));
                            itemHolder.footer.setBackgroundColor(ATEUtil.getThemeAlbumDefaultPaletteColor(mContext));
                        }

                        @Override
                        public void onResourceReady(final Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            new Palette.Builder(resource).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    Palette.Swatch swatch = ColorUtil.getMostPopulousSwatch(palette);
                                    if (swatch != null) {
                                        int color = swatch.getRgb();
                                        itemHolder.footer.setBackgroundColor(color);

                                        int detailColor = swatch.getTitleTextColor();
                                        itemHolder.artistImage.setImageBitmap(resource);
                                        itemHolder.name.setTextColor(ColorUtil.getOpaqueColor(detailColor));
                                        itemHolder.albumCount.setTextColor(detailColor);
                                        itemHolder.songCount.setTextColor(detailColor);
                                        itemHolder.popupMenu.setColorFilter(detailColor);
                                    }
                                }
                            });
                        }
                    });
        }else {
            Glide.with(mContext)
                    .load(artistArt.getLarge())
                    .placeholder(ATEUtil.getDefaultSingerDrawable(mContext))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .error(ATEUtil.getDefaultSingerDrawable(mContext))
                    .into(itemHolder.artistImage);
        }
    }

    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    public void updateDataSet(List<Artist> arrayList) {
        this.arraylist = arrayList;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        if (arraylist == null || arraylist.size() == 0)
            return "";
        return Character.toString(arraylist.get(position).name.charAt(0));
    }

    private void setOnPopupMenuListener(final ArtistAdapter.ItemHolder itemHolder, final int position) {
        itemHolder.popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);
                int adapterPosition = itemHolder.getAdapterPosition();
                final Artist artist = arraylist.get(adapterPosition);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.popup_artist_addto_queue:
                                getSongListIdByArtist(arraylist.get(position).id)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<long[]>() {
                                            @Override
                                            public void call(long[] ids) {
                                                MusicPlayer.addToQueue(mContext, ids, -1, ListenerUtil.IdType.NA);
                                            }
                                        });
                                break;
                            case R.id.popup_artist_addto_playlist:
                                getSongListIdByArtist(arraylist.get(position).id)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<long[]>() {
                                            @Override
                                            public void call(long[] ids) {
                                                ListenerUtil.showAddPlaylistDialog(mContext,ids);
                                            }
                                        });
                                break;
                            case R.id.popup_artist_delete:
                                switch (action) {
                                    case Constants.NAVIGATE_PLAYLIST_FAVOURATE:
                                        getSongListIdByArtist(artist.id)
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
                                        getSongListIdByArtist(artist.id)
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
                                        ArtistSongLoader.getSongsForArtist(mContext,arraylist.get(position).id)
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
                menu.inflate(R.menu.popup_artist);
                menu.show();
            }
        });
    }

    private Observable<long[]> getSongListIdByArtist(long id) {
        return ArtistSongLoader.getSongsForArtist(mContext, id)
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
        private TextView name;
        private TextView albumCount;
        private TextView songCount;
        private ImageView artistImage;
        private ImageView popupMenu;
        private View footer;

        public ItemHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.text_item_title);
            this.albumCount = (TextView) view.findViewById(R.id.text_item_subtitle);
            this.songCount = (TextView) view.findViewById(R.id.text_item_subtitle_2);
            this.artistImage = (ImageView) view.findViewById(R.id.image);
            this.popupMenu = (ImageView) view.findViewById(R.id.popup_menu);
            this.footer = view.findViewById(R.id.footer);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavigationUtil.navigateToArtist(mContext, arraylist.get(getAdapterPosition()).id, arraylist.get(getAdapterPosition()).name,
                    new Pair<View, String>(artistImage, "transition_artist_art" + getAdapterPosition()));
        }

    }
}




