package io.hefuyi.listener.injector.module;

import dagger.Module;
import dagger.Provides;
import io.hefuyi.listener.mvp.contract.SongsContract;
import io.hefuyi.listener.mvp.presenter.SongsPresenter;
import io.hefuyi.listener.mvp.usecase.GetSongs;
import io.hefuyi.listener.respository.interfaces.Repository;

/**
 * Created by hefuyi on 2016/11/12.
 */
@Module
public class SongsModule {

    @Provides
    SongsContract.Presenter getSongsPresenter(GetSongs getSongs) {
        return new SongsPresenter(getSongs);
    }

    @Provides
    GetSongs getSongsUsecase(Repository repository) {
        return new GetSongs(repository);
    }
}
