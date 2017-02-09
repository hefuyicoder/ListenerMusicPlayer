package io.hefuyi.listener.mvp.presenter;

import java.util.List;

import io.hefuyi.listener.mvp.contract.SearchContract;
import io.hefuyi.listener.mvp.usecase.GetSearchResult;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by hefuyi on 2017/1/3.
 */

public class SearchPresenter implements SearchContract.Presenter{

    private GetSearchResult mUsecase;
    private SearchContract.View mView;
    private Subscription mSubscription;

    public SearchPresenter(GetSearchResult getSearchResult) {
        mUsecase = getSearchResult;
    }

    @Override
    public void attachView(SearchContract.View view) {
        mView = view;
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {
        if (mSubscription != null && mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void search(String queryString) {
        if (mSubscription != null && mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        mSubscription = mUsecase.execute(new GetSearchResult.RequestValues(queryString))
                .getResultList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Object>>() {
                    @Override
                    public void call(List<Object> list) {
                        if (list != null && list.size() == 3) {
                            mView.showEmptyView();
                        } else {
                            mView.showSearchResult(list);
                        }
                    }
                });
    }
}
