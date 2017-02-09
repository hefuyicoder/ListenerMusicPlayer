package io.hefuyi.listener.mvp.contract;

import java.util.List;

import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.mvp.presenter.BasePresenter;
import io.hefuyi.listener.mvp.view.BaseView;

/**
 * Created by hefuyi on 2016/12/12.
 */

public interface FolderSongsContract {

    interface View extends BaseView{

        void showSongs(List<Song> songList);

    }

    interface Presenter extends BasePresenter<View>{

        void loadSongs(String path);

    }
}
