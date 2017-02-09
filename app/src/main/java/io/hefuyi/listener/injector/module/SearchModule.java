package io.hefuyi.listener.injector.module;

import dagger.Module;
import dagger.Provides;
import io.hefuyi.listener.mvp.contract.SearchContract;
import io.hefuyi.listener.mvp.presenter.SearchPresenter;
import io.hefuyi.listener.mvp.usecase.GetSearchResult;
import io.hefuyi.listener.respository.interfaces.Repository;

/**
 * Created by hefuyi on 2017/1/3.
 */
@Module
public class SearchModule {

    @Provides
    SearchContract.Presenter getSearchPresenter(GetSearchResult getSearchResult) {
        return new SearchPresenter(getSearchResult);
    }

    @Provides
    GetSearchResult getSearchResultUsecase(Repository repository) {
        return new GetSearchResult(repository);
    }
}
