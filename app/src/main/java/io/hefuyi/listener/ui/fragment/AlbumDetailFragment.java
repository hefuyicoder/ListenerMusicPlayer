package io.hefuyi.listener.ui.fragment;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import com.afollestad.appthemeengine.ATE;

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
import io.hefuyi.listener.event.MetaChangedEvent;
import io.hefuyi.listener.injector.component.AlbumSongsComponent;
import io.hefuyi.listener.injector.component.ApplicationComponent;
import io.hefuyi.listener.injector.component.DaggerAlbumSongsComponent;
import io.hefuyi.listener.injector.module.AlbumSongsModel;
import io.hefuyi.listener.mvp.contract.AlbumDetailContract;
import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.ui.adapter.AlbumSongsAdapter;
import io.hefuyi.listener.util.ATEUtil;
import io.hefuyi.listener.util.ColorUtil;
import io.hefuyi.listener.util.DensityUtil;
import io.hefuyi.listener.util.ListenerUtil;
import io.hefuyi.listener.util.PreferencesUtility;
import io.hefuyi.listener.util.SortOrder;
import io.hefuyi.listener.widget.DividerItemDecoration;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumDetailFragment extends Fragment implements AlbumDetailContract.View {

    @Inject
    AlbumDetailContract.Presenter mPresenter;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.app_bar)
    AppBarLayout appBarLayout;
    @BindView(R.id.fab_play)
    FloatingActionButton fabPlay;
    @BindView(R.id.album_art)
    ImageView albumArt;
    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    private PreferencesUtility mPreferences;
    private Context context;
    private AlbumSongsAdapter mAdapter;
    private long albumID = -1;
    private String albumName;
    private int primaryColor = -1;

    public static AlbumDetailFragment newInstance(long id, String name, boolean useTransition, String transitionName) {
        AlbumDetailFragment fragment = new AlbumDetailFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.ALBUM_ID, id);
        args.putString(Constants.ALBUM_NAME, name);
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
            albumID = getArguments().getLong(Constants.ALBUM_ID);
            albumName = getArguments().getString(Constants.ALBUM_NAME);
        }
        context = getActivity();
        mPreferences = PreferencesUtility.getInstance(context);
        mAdapter = new AlbumSongsAdapter(getActivity(), albumID);
    }

    private void injectDependences() {
        ApplicationComponent applicationComponent = ((ListenerApp) getActivity().getApplication()).getApplicationComponent();
        AlbumSongsComponent albumSongsComponent = DaggerAlbumSongsComponent.builder()
                .applicationComponent(applicationComponent)
                .albumSongsModel(new AlbumSongsModel())
                .build();
        albumSongsComponent.inject(this);
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

        ATE.apply(this, ATEUtil.getATEKey(context));

        if (getArguments().getBoolean("transition")) {
            albumArt.setTransitionName(getArguments().getString("transition_name"));
        }

        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST, false));

        setupToolbar();

        mPresenter.subscribe(albumID);
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
        inflater.inflate(R.menu.album_song_sort_by, menu);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_az:
                mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_A_Z);
                mPresenter.loadAlbumSongs(albumID);
                return true;
            case R.id.menu_sort_by_za:
                mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_Z_A);
                mPresenter.loadAlbumSongs(albumID);
                return true;
            case R.id.menu_sort_by_year:
                mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_YEAR);
                mPresenter.loadAlbumSongs(albumID);
                return true;
            case R.id.menu_sort_by_duration:
                mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_DURATION);
                mPresenter.loadAlbumSongs(albumID);
                return true;
            case R.id.menu_sort_by_track_number:
                mPreferences.setAlbumSongSortOrder(SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST);
                mPresenter.loadAlbumSongs(albumID);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupToolbar() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout.setTitle(albumName);
    }

    @Override
    public void showAlbumSongs(List<Song> songList) {
        mAdapter.setSongList(songList);
    }

    @Override
    public void showAlbumArt(Bitmap bitmap) {
        albumArt.setImageBitmap(bitmap);
        if (ATEUtil.getATEKey(getActivity()).equals("dark_theme")) {
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

    @Override
    public void showAlbumArt(Drawable drawable) {
        albumArt.setImageDrawable(drawable);
        primaryColor = ATEUtil.getThemePrimaryColor(getContext());
        collapsingToolbarLayout.setContentScrimColor(primaryColor);
        collapsingToolbarLayout.setStatusBarScrimColor(ColorUtil.getStatusBarColor(primaryColor));
    }

    @OnClick(R.id.fab_play)
    public void onFabPlayClick() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AlbumSongsAdapter adapter = (AlbumSongsAdapter) recyclerView.getAdapter();
                MusicPlayer.playAll(getActivity(), adapter.getSongIds(), 0, albumID, ListenerUtil.IdType.Album, false);
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
}
