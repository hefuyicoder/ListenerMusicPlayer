package io.hefuyi.listener.injector.module;

import dagger.Module;
import dagger.Provides;
import io.hefuyi.listener.mvp.contract.PlaylistContract;
import io.hefuyi.listener.mvp.presenter.PlaylistPresenter;
import io.hefuyi.listener.mvp.usecase.GetPlaylists;
import io.hefuyi.listener.respository.interfaces.Repository;

/**
 * Created by hefuyi on 2016/12/5.
 */
@Module
public class PlaylistModule {

    @Provides
    GetPlaylists getPlaylistUsecase(Repository repository) {
        return new GetPlaylists(repository);
    }

    @Provides
    PlaylistContract.Presenter getPlaylistPresenter(GetPlaylists getPlaylists) {
        return new PlaylistPresenter(getPlaylists);
    }
}
