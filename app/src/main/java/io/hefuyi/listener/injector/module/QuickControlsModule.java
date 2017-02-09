package io.hefuyi.listener.injector.module;

import dagger.Module;
import dagger.Provides;
import io.hefuyi.listener.mvp.contract.QuickControlsContract;
import io.hefuyi.listener.mvp.presenter.QuickControlsPresenter;
import io.hefuyi.listener.mvp.usecase.GetLyric;
import io.hefuyi.listener.respository.interfaces.Repository;

/**
 * Created by hefuyi on 2016/11/7.
 */
@Module
public class QuickControlsModule {

    @Provides
    QuickControlsContract.Presenter getQuickControlsPresenter(GetLyric getLyric) {
        return new QuickControlsPresenter(getLyric);
    }

    @Provides
    GetLyric getLyricUsecase(Repository repository) {
        return new GetLyric(repository);
    }

}
