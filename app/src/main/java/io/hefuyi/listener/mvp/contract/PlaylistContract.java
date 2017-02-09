package io.hefuyi.listener.mvp.contract;

import java.util.List;

import io.hefuyi.listener.mvp.model.Playlist;
import io.hefuyi.listener.mvp.presenter.BasePresenter;
import io.hefuyi.listener.mvp.view.BaseView;

/**
 * Created by hefuyi on 2016/12/4.
 */

public interface PlaylistContract {

    interface View extends BaseView{

        void showPlaylist(List<Playlist> playlists);

        void showEmptyView();

    }

    interface Presenter extends BasePresenter<View>{

        void loadPlaylist();
    }
}
