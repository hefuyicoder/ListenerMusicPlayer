package io.hefuyi.listener.mvp.contract;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import io.hefuyi.listener.mvp.presenter.BasePresenter;
import io.hefuyi.listener.mvp.view.BaseView;

/**
 * Created by hefuyi on 2016/11/24.
 */

public interface ArtistDetailContract {

    interface View extends BaseView {

        Context getContext();

        void showArtistArt(Bitmap bitmap);

        void showArtistArt(Drawable drawable);

    }

    interface Presenter extends BasePresenter<View> {

        void subscribe(long artistID);

        void loadArtistArt(long artistID);
    }

}
