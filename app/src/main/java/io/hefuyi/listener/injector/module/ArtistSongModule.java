package io.hefuyi.listener.injector.module;

import dagger.Module;
import dagger.Provides;
import io.hefuyi.listener.mvp.contract.ArtistSongContract;
import io.hefuyi.listener.mvp.presenter.ArtistSongPresenter;
import io.hefuyi.listener.mvp.usecase.GetArtistSongs;
import io.hefuyi.listener.respository.interfaces.Repository;

/**
 * Created by hefuyi on 2016/11/25.
 */
@Module
public class ArtistSongModule {

    @Provides
    GetArtistSongs getArtistSongsUsecase(Repository repository) {
        return new GetArtistSongs(repository);
    }

    @Provides
    ArtistSongContract.Presenter getArtistSongPresenter(GetArtistSongs getArtistSongs) {
        return new ArtistSongPresenter(getArtistSongs);
    }
}
