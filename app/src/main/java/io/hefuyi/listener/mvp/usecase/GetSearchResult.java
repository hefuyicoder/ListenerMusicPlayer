package io.hefuyi.listener.mvp.usecase;

import java.util.List;

import io.hefuyi.listener.respository.interfaces.Repository;
import rx.Observable;

/**
 * Created by hefuyi on 2017/1/3.
 */

public class GetSearchResult extends UseCase<GetSearchResult.RequestValues,GetSearchResult.ResponseValue>{

    private Repository mRepository;

    public GetSearchResult(Repository repository) {
        this.mRepository = repository;
    }

    @Override
    public ResponseValue execute(RequestValues requestValues) {
        return new ResponseValue(mRepository.getSearchResult(requestValues.getQueryString()));
    }

    public static final class RequestValues implements UseCase.RequestValues {

        private final String queryString;

        public RequestValues(String queryString) {
            this.queryString = queryString;
        }

        public String getQueryString() {
            return queryString;
        }

    }

    public static final class ResponseValue implements UseCase.ResponseValue {

        private final Observable<List<Object>> mListObservable;

        public ResponseValue(Observable<List<Object>> listObservable) {
            mListObservable = listObservable;
        }

        public Observable<List<Object>> getResultList(){
            return mListObservable;
        }
    }
}
