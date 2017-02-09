package io.hefuyi.listener.mvp.presenter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.List;

import io.hefuyi.listener.mvp.contract.AlbumDetailContract;
import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.mvp.usecase.GetAlbumSongs;
import io.hefuyi.listener.util.ATEUtil;
import io.hefuyi.listener.util.ListenerUtil;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by hefuyi on 2016/12/3.
 */

public class AlbumDetailPresenter implements AlbumDetailContract.Presenter {

    private GetAlbumSongs mUsecase;
    private AlbumDetailContract.View mView;
    private CompositeSubscription mCompositeSubscription;

    public AlbumDetailPresenter(GetAlbumSongs getAlbumSongs) {
        mUsecase = getAlbumSongs;
    }

    @Override
    public void subscribe(long albumID) {
        loadAlbumArt(albumID);
        loadAlbumSongs(albumID);
    }

    @Override
    public void attachView(AlbumDetailContract.View view) {
        mView = view;
        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    public void subscribe() {
        throw new RuntimeException("please call subscribe(long albumID)");
    }

    @Override
    public void unsubscribe() {
        mCompositeSubscription.clear();
    }

    @Override
    public void loadAlbumSongs(long albumID) {
        mCompositeSubscription.clear();
        Subscription subscription = mUsecase.execute(new GetAlbumSongs.RequestValues(albumID))
                .getSongList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Song>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<Song> songList) {
                        mView.showAlbumSongs(songList);
                    }
                });
        mCompositeSubscription.add(subscription);
    }

    @Override
    public void loadAlbumArt(long albumID) {
        Glide.with(mView.getContext())
                .load(ListenerUtil.getAlbumArtUri(albumID))
                .asBitmap()
                .priority(Priority.IMMEDIATE)
                .error(ATEUtil.getDefaultAlbumDrawable(mView.getContext()))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        mView.showAlbumArt(errorDrawable);
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        mView.showAlbumArt(resource);
                    }
                });
    }
}
