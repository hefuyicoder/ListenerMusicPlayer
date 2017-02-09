package io.hefuyi.listener.mvp.usecase;

import java.util.List;

import io.hefuyi.listener.Constants;
import io.hefuyi.listener.mvp.model.Album;
import io.hefuyi.listener.respository.interfaces.Repository;
import rx.Observable;

/**
 * Created by hefuyi on 2016/11/12.
 */

public class GetAlbums extends UseCase<GetAlbums.RequestValues,GetAlbums.ResponseValue>{

    private final Repository mRepository;

    public GetAlbums(Repository repository){
        mRepository = repository;
    }

    @Override
    public ResponseValue execute(RequestValues requestValues) {
        String action = requestValues.getAction();
        switch (action) {
            case Constants.NAVIGATE_ALLSONG:
                return new ResponseValue(mRepository.getAllAlbums());
            case Constants.NAVIGATE_PLAYLIST_RECENTADD:
                return new ResponseValue(mRepository.getRecentlyAddedAlbums());
            case Constants.NAVIGATE_PLAYLIST_RECENTPLAY:
                return new ResponseValue(mRepository.getRecentlyPlayedAlbums());
            case Constants.NAVIGATE_PLAYLIST_FAVOURATE:
                return new ResponseValue(mRepository.getFavourateAlbums());
            default:
                throw new RuntimeException("wrong action type");
        }
    }

    public static final class RequestValues implements UseCase.RequestValues{

        private final String action;

        public RequestValues(String action) {
            this.action = action;
        }

        public String getAction() {
            return action;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {

        private final Observable<List<Album>> mListObservable;

        public ResponseValue(Observable<List<Album>> listObservable) {
            mListObservable = listObservable;
        }

        public Observable<List<Album>> getSongList(){
            return mListObservable;
        }
    }
}
