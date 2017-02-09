package io.hefuyi.listener.mvp.usecase;

import java.util.List;

import io.hefuyi.listener.mvp.model.Playlist;
import io.hefuyi.listener.respository.interfaces.Repository;
import rx.Observable;

/**
 * Created by hefuyi on 2016/12/5.
 */

public class GetPlaylists extends UseCase<GetPlaylists.RequestValues,GetPlaylists.ResponseValue>{

    private final Repository mRepository;

    public GetPlaylists(Repository repository) {
        this.mRepository = repository;
    }

    @Override
    public ResponseValue execute(RequestValues requestValues) {
        return new ResponseValue(mRepository.getPlaylists(requestValues.defaultIncluded));
    }

    public static final class RequestValues implements UseCase.RequestValues{

        private boolean defaultIncluded;

        public RequestValues(boolean defaultIncluded) {
            this.defaultIncluded = defaultIncluded;
        }

    }

    public static final class ResponseValue implements UseCase.ResponseValue {

        private final Observable<List<Playlist>> mListObservable;

        public ResponseValue(Observable<List<Playlist>> listObservable) {
            mListObservable = listObservable;
        }

        public Observable<List<Playlist>> getPlaylists(){
            return mListObservable;
        }
    }
}
