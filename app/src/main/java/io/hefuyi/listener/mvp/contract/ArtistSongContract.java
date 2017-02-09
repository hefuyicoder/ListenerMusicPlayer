package io.hefuyi.listener.mvp.contract;

import java.util.List;

import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.mvp.presenter.BasePresenter;
import io.hefuyi.listener.mvp.view.BaseView;

/**
 * Created by hefuyi on 2016/11/25.
 */

public interface ArtistSongContract {

    interface View extends BaseView {

        void showSongs(List<Song> songList);
    }

    interface Presenter extends BasePresenter<View> {

        void subscribe(long artistID);

        void loadSongs(long artistID);
    }

}
