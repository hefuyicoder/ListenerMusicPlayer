package io.hefuyi.listener.injector.module;

import dagger.Module;
import dagger.Provides;
import io.hefuyi.listener.mvp.contract.ArtistDetailContract;
import io.hefuyi.listener.mvp.presenter.ArtistDetailPresenter;
import io.hefuyi.listener.mvp.usecase.GetArtistInfo;
import io.hefuyi.listener.respository.interfaces.Repository;

/**
 * Created by hefuyi on 2016/11/13.
 */
@Module
public class ArtistInfoModule {

    @Provides
    GetArtistInfo getArtistInfoUsecase(Repository repository) {
        return new GetArtistInfo(repository);
    }

    @Provides
    ArtistDetailContract.Presenter getArtistDetailPresenter() {
        return new ArtistDetailPresenter();
    }
}
