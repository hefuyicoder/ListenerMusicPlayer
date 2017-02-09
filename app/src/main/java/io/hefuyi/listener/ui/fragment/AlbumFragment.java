package io.hefuyi.listener.ui.fragment;


import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.ATE;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.hefuyi.listener.Constants;
import io.hefuyi.listener.ListenerApp;
import io.hefuyi.listener.R;
import io.hefuyi.listener.RxBus;
import io.hefuyi.listener.event.FavourateSongEvent;
import io.hefuyi.listener.event.MediaUpdateEvent;
import io.hefuyi.listener.event.RecentlyPlayEvent;
import io.hefuyi.listener.injector.component.AlbumsComponent;
import io.hefuyi.listener.injector.component.ApplicationComponent;
import io.hefuyi.listener.injector.component.DaggerAlbumsComponent;
import io.hefuyi.listener.injector.module.ActivityModule;
import io.hefuyi.listener.injector.module.AlbumsModule;
import io.hefuyi.listener.mvp.contract.AlbumsContract;
import io.hefuyi.listener.mvp.model.Album;
import io.hefuyi.listener.ui.adapter.AlbumAdapter;
import io.hefuyi.listener.util.ATEUtil;
import io.hefuyi.listener.util.PreferencesUtility;
import io.hefuyi.listener.util.SortOrder;
import io.hefuyi.listener.widget.DividerItemDecoration;
import io.hefuyi.listener.widget.fastscroller.FastScrollRecyclerView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumFragment extends Fragment implements AlbumsContract.View{

    @Inject
    AlbumsContract.Presenter mPresenter;
    @BindView(R.id.recyclerview)
    FastScrollRecyclerView recyclerView;
    @BindView(R.id.view_empty)
    View emptyView;
    private AlbumAdapter mAdapter;
    private GridLayoutManager layoutManager;
    private RecyclerView.ItemDecoration itemDecoration;
    private PreferencesUtility mPreferences;
    private boolean isGrid;
    private String action;

    public static AlbumFragment newInstance(String action) {

        Bundle args = new Bundle();
        switch (action) {
            case Constants.NAVIGATE_ALLSONG:
                args.putString(Constants.PLAYLIST_TYPE, action);
                break;
            case Constants.NAVIGATE_PLAYLIST_RECENTADD:
                args.putString(Constants.PLAYLIST_TYPE, action);
                break;
            case Constants.NAVIGATE_PLAYLIST_RECENTPLAY:
                args.putString(Constants.PLAYLIST_TYPE, action);
                break;
            case Constants.NAVIGATE_PLAYLIST_FAVOURATE:
                args.putString(Constants.PLAYLIST_TYPE, action);
                break;
            default:
                throw new RuntimeException("wrong action type");
        }
        AlbumFragment fragment = new AlbumFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectDependences();
        mPresenter.attachView(this);
        mPreferences = PreferencesUtility.getInstance(getActivity());
        isGrid = mPreferences.isAlbumsInGrid();
        if (isGrid) {
            layoutManager = new GridLayoutManager(getActivity(), 2);
        } else {
            layoutManager = new GridLayoutManager(getActivity(), 1);
        }

        if (getArguments() != null) {
            action = getArguments().getString(Constants.PLAYLIST_TYPE);
        }
        mAdapter = new AlbumAdapter(getActivity(),action);

    }

    private void injectDependences() {
        ApplicationComponent applicationComponent = ((ListenerApp) getActivity().getApplication()).getApplicationComponent();
        AlbumsComponent albumsComponent = DaggerAlbumsComponent.builder()
                .applicationComponent(applicationComponent)
                .activityModule(new ActivityModule(getActivity()))
                .albumsModule(new AlbumsModule())
                .build();
        albumsComponent.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        ATE.apply(this, ATEUtil.getATEKey(getActivity()));

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
        setItemDecoration();
        mPresenter.loadAlbums(action);
        subscribeMediaUpdateEvent();
        if (Constants.NAVIGATE_PLAYLIST_FAVOURATE.equals(action)) {
            subscribeFavourateSongEvent();
        } else if (Constants.NAVIGATE_PLAYLIST_RECENTPLAY.equals(action)) {
            subscribeRecentlyPlayEvent();
        } else {
            subscribeMediaUpdateEvent();
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.unsubscribe();
        RxBus.getInstance().unSubscribe(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_show_as, menu);
        if (Constants.NAVIGATE_ALLSONG.equals(action)) {
            inflater.inflate(R.menu.album_sort_by, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_az:
                mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_A_Z);
                mPresenter.loadAlbums(action);
                return true;
            case R.id.menu_sort_by_za:
                mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_Z_A);
                mPresenter.loadAlbums(action);
                return true;
            case R.id.menu_sort_by_number_of_songs:
                mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS);
                mPresenter.loadAlbums(action);
                return true;
            case R.id.menu_show_as_list:
                if (isGrid) {
                    mPreferences.setAlbumsInGrid(false);
                    isGrid = false;
                    updateLayoutManager(1);
                }
                return true;
            case R.id.menu_show_as_grid:
                if (!isGrid) {
                    mPreferences.setAlbumsInGrid(true);
                    isGrid = true;
                    updateLayoutManager(2);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void showAlbums(List<Album> albumList) {
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        mAdapter.setAlbumsList(albumList);
    }

    @Override
    public void showEmptyView() {
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void setItemDecoration() {
        if (isGrid) {
            int spacingInPixels = getActivity().getResources().getDimensionPixelSize(R.dimen.spacing_card_album_grid);
            itemDecoration = new SpacesItemDecoration(spacingInPixels);
        } else {
            itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST, true);
        }
        recyclerView.addItemDecoration(itemDecoration);
    }

    private void updateLayoutManager(int column) {
        recyclerView.removeItemDecoration(itemDecoration);
        mAdapter = new AlbumAdapter(getActivity(), action);
        recyclerView.setAdapter(mAdapter);
        layoutManager.setSpanCount(column);
        layoutManager.requestLayout();
        setItemDecoration();
        mPresenter.loadAlbums(action);
    }

    private void subscribeMediaUpdateEvent() {
        Subscription subscription = RxBus.getInstance()
                .toObservable(MediaUpdateEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .debounce(1, TimeUnit.SECONDS)
                .subscribe(new Action1<MediaUpdateEvent>() {
                    @Override
                    public void call(MediaUpdateEvent event) {
                        mPresenter.loadAlbums(action);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }

    private void subscribeFavourateSongEvent() {
        Subscription subscription = RxBus.getInstance()
                .toObservable(FavourateSongEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FavourateSongEvent>() {
                    @Override
                    public void call(FavourateSongEvent event) {
                        mPresenter.loadAlbums(action);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }

    private void subscribeRecentlyPlayEvent() {
        Subscription subscription = RxBus.getInstance()
                .toObservable(RecentlyPlayEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RecentlyPlayEvent>() {
                    @Override
                    public void call(RecentlyPlayEvent event) {
                        mPresenter.loadAlbums(action);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }

    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position % 2 == 0) {
                outRect.left = 0;
                outRect.top = space;
                outRect.right = space / 2;
            } else {
                outRect.left = space / 2;
                outRect.top = space;
                outRect.right = 0;
            }
        }
    }
}
