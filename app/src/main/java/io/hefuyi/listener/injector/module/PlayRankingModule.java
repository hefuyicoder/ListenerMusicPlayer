package io.hefuyi.listener.injector.module;

import dagger.Module;
import dagger.Provides;
import io.hefuyi.listener.mvp.contract.PlayRankingContract;
import io.hefuyi.listener.mvp.presenter.PlayRankingPresenter;
import io.hefuyi.listener.mvp.usecase.GetSongs;
import io.hefuyi.listener.respository.interfaces.Repository;

/**
 * Created by hefuyi on 2016/12/9.
 */
@Module
public class PlayRankingModule {

    @Provides
    GetSongs getSongsUsecase(Repository repository) {
        return new GetSongs(repository);
    }

    @Provides
    PlayRankingContract.Presenter getPlayRankingPresenter(GetSongs getSongs) {
        return new PlayRankingPresenter(getSongs);
    }
}
