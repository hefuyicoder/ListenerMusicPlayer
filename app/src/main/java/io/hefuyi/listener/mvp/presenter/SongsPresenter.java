package io.hefuyi.listener.mvp.presenter;

import java.util.List;

import io.hefuyi.listener.mvp.contract.SongsContract;
import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.mvp.usecase.GetSongs;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by hefuyi on 2016/11/12.
 */

public class SongsPresenter implements SongsContract.Presenter {

    private GetSongs mUsecase;
    private SongsContract.View mView;
    private CompositeSubscription mCompositeSubscription;

    public SongsPresenter(GetSongs getSongs) {
        mUsecase = getSongs;
    }

    @Override
    public void attachView(SongsContract.View view) {
        mView = view;
        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    public void subscribe() {
    }

    @Override
    public void unsubscribe() {
        mCompositeSubscription.clear();
    }

    @Override
    public void loadSongs(String action) {
        mCompositeSubscription.clear();
        Subscription subscription = mUsecase.execute(new GetSongs.RequestValues(action))
                .getSongList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Song>>() {
                    @Override
                    public void call(List<Song> songList) {
                        if (songList == null || songList.size() == 0) {
                            mView.showEmptyView();
                        } else {
                            mView.showSongs(songList);
                        }
                    }
                });
        mCompositeSubscription.add(subscription);
    }
}
