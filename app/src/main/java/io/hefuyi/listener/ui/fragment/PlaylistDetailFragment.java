package io.hefuyi.listener.ui.fragment;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.hefuyi.listener.Constants;
import io.hefuyi.listener.ListenerApp;
import io.hefuyi.listener.MusicPlayer;
import io.hefuyi.listener.R;
import io.hefuyi.listener.RxBus;
import io.hefuyi.listener.dataloader.PlaylistSongLoader;
import io.hefuyi.listener.event.MetaChangedEvent;
import io.hefuyi.listener.event.PlaylistUpdateEvent;
import io.hefuyi.listener.injector.component.ApplicationComponent;
import io.hefuyi.listener.injector.component.DaggerPlaylistSongComponent;
import io.hefuyi.listener.injector.component.PlaylistSongComponent;
import io.hefuyi.listener.injector.module.PlaylistSongModule;
import io.hefuyi.listener.mvp.contract.PlaylistDetailContract;
import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.ui.adapter.PlaylistSongAdapter;
import io.hefuyi.listener.util.ATEUtil;
import io.hefuyi.listener.util.ColorUtil;
import io.hefuyi.listener.util.DensityUtil;
import io.hefuyi.listener.util.ListenerUtil;
import io.hefuyi.listener.widget.DividerItemDecoration;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistDetailFragment extends Fragment implements PlaylistDetailContract.View {

    @Inject
    PlaylistDetailContract.Presenter mPresenter;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.app_bar)
    AppBarLayout appBarLayout;
    @BindView(R.id.fab_play)
    FloatingActionButton fabPlay;
    @BindView(R.id.album_art)
    ImageView playlistArt;
    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    private Context mContext;
    private int primaryColor = -1;
    private PlaylistSongAdapter mAdapter;
    private long playlistID = -1;
    private String playlistName;
    private long firstAlbumID = -1;

    public static PlaylistDetailFragment newInstance(long playlistID, String playlistName, long firstAlbumID, boolean useTransition, String transitionName) {
        PlaylistDetailFragment fragment = new PlaylistDetailFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.PLAYLIST_ID, playlistID);
        args.putString(Constants.PLAYLIST_NAME, playlistName);
        args.putLong(Constants.FIRST_ALBUM_ID, firstAlbumID);
        args.putBoolean("transition", useTransition);
        if (useTransition)
            args.putString("transition_name", transitionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectDependences();
        mPresenter.attachView(this);

        if (getArguments() != null) {
            playlistName = getArguments().getString(Constants.PLAYLIST_NAME);
            firstAlbumID = getArguments().getLong(Constants.FIRST_ALBUM_ID);
            playlistID = getArguments().getLong(Constants.PLAYLIST_ID);
        }
        mContext = getActivity();
        mAdapter = new PlaylistSongAdapter(getContext(), playlistID, null);
    }

    private void injectDependences() {
        ApplicationComponent applicationComponent = ((ListenerApp) getActivity().getApplication()).getApplicationComponent();
        PlaylistSongComponent playlistSongComponent = DaggerPlaylistSongComponent.builder()
                .applicationComponent(applicationComponent)
                .playlistSongModule(new PlaylistSongModule())
                .build();
        playlistSongComponent.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_album_detail, container, false);
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            root.findViewById(R.id.app_bar).setFitsSystemWindows(false);
            root.findViewById(R.id.album_art).setFitsSystemWindows(false);
            root.findViewById(R.id.gradient).setFitsSystemWindows(false);
            Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
            CollapsingToolbarLayout.LayoutParams layoutParams = (CollapsingToolbarLayout.LayoutParams) toolbar.getLayoutParams();
            layoutParams.height += DensityUtil.getStatusBarHeight(getContext());
            toolbar.setLayoutParams(layoutParams);
            toolbar.setPadding(0, DensityUtil.getStatusBarHeight(getContext()), 0, 0);
        }
        return root;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        ATE.apply(this, ATEUtil.getATEKey(getActivity()));

        if (getArguments().getBoolean("transition")) {
            playlistArt.setTransitionName(getArguments().getString("transition_name"));
        }

        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST, false));

        setupToolbar();

        mPresenter.loadPlaylistSongs(playlistID);
        mPresenter.loadPlaylistArt(firstAlbumID);
        //监听歌曲删除事件,修改歌单封面
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                RxBus.getInstance().post(new PlaylistUpdateEvent());
                if (positionStart == 0) {
                    List<Song> songs = mAdapter.getSongList();
                    if (songs.size() == 0) {
                        firstAlbumID = -1;
                    } else {
                        firstAlbumID = songs.get(0).albumId;
                    }
                    mPresenter.loadPlaylistArt(firstAlbumID);
                }
            }
        });
        subscribeMetaChangedEvent();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.unsubscribe();
        RxBus.getInstance().unSubscribe(this);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_playlist_detail, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        if (primaryColor != -1 && getActivity() != null) {
            collapsingToolbarLayout.setContentScrimColor(primaryColor);
            collapsingToolbarLayout.setStatusBarScrimColor(ColorUtil.getStatusBarColor(primaryColor));
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_playlist_detail_rename:
                showRenamePlaylistDialog(playlistName);
                break;
            case R.id.action_playlist_detail_addto_playlist:
                ListenerUtil.showAddPlaylistDialog(getActivity(), mAdapter.getSongIds());
                break;
            case R.id.action_playlist_detail_addto_queue:
                MusicPlayer.addToQueue(mContext, mAdapter.getSongIds(), -1, ListenerUtil.IdType.Playlist);
                break;
            case R.id.action_playlist_detail_delete:
                showDeletePlaylistDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupToolbar() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout.setTitle(playlistName);
    }

    @OnClick(R.id.fab_play)
    public void onFabPlayClick() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MusicPlayer.playAll(getActivity(), mAdapter.getSongIds(), 0, playlistID, ListenerUtil.IdType.Playlist, false);
            }
        }, 150);
    }

    private void subscribeMetaChangedEvent() {
        Subscription subscription = RxBus.getInstance()
                .toObservable(MetaChangedEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .subscribe(new Action1<MetaChangedEvent>() {
                    @Override
                    public void call(MetaChangedEvent event) {
                        mAdapter.notifyDataSetChanged();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }

    @Override
    public void showPlaylistSongs(List<Song> songList) {
        mAdapter.setSongList(songList);
    }

    @Override
    public void showPlaylistArt(Drawable drawable) {
        if (getActivity() != null) {
            playlistArt.setImageDrawable(drawable);
        }
    }

    @Override
    public void showPlaylistArt(Bitmap bitmap) {
        playlistArt.setImageBitmap(bitmap);
        if (ATEUtil.getATEKey(mContext).equals("dark_theme")) {
            return;
        }
        new Palette.Builder(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch swatch = ColorUtil.getMostPopulousSwatch(palette);
                if (swatch != null) {
                    int color = swatch.getRgb();
                    collapsingToolbarLayout.setContentScrimColor(color);
                    collapsingToolbarLayout.setStatusBarScrimColor(ColorUtil.getStatusBarColor(color));
                    primaryColor = color;
                }
            }
        });
    }

    private void showDeletePlaylistDialog() {
        new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.delete_playlist_song) + "?")
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PlaylistSongLoader.removeFromPlaylist(mContext, mAdapter.getSongIds(), playlistID);
                        mPresenter.loadPlaylistSongs(playlistID);
                        showPlaylistArt(ATEUtil.getDefaultAlbumDrawable(mContext));
                        primaryColor = ATEUtil.getThemePrimaryColor(mContext);
                        collapsingToolbarLayout.setContentScrimColor(primaryColor);
                        collapsingToolbarLayout.setStatusBarScrimColor(ColorUtil.getStatusBarColor(primaryColor));
                        RxBus.getInstance().post(new PlaylistUpdateEvent());
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showRenamePlaylistDialog(String oldName) {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.rename_playlist)
                .positiveText("确定")
                .negativeText(R.string.cancel)
                .input(null, oldName, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        MusicPlayer.renamePlaylist(getActivity(), playlistID, input.toString());
                        collapsingToolbarLayout.setTitle(input.toString());
                        playlistName = input.toString();
                        RxBus.getInstance().post(new PlaylistUpdateEvent());
                        Toast.makeText(getActivity(), R.string.rename_playlist_success, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}
