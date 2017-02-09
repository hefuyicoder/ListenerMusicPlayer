package io.hefuyi.listener.mvp.contract;

import java.util.List;

import io.hefuyi.listener.mvp.model.Album;
import io.hefuyi.listener.mvp.presenter.BasePresenter;
import io.hefuyi.listener.mvp.view.BaseView;

/**
 * Created by hefuyi on 2016/11/12.
 */

public interface AlbumsContract {

    interface View extends BaseView{

        void showAlbums(List<Album> albumList);

        void showEmptyView();
    }

    interface Presenter extends BasePresenter<View>{

        void loadAlbums(String action);

    }
}
