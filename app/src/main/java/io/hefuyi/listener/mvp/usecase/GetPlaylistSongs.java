package io.hefuyi.listener.mvp.usecase;

import java.util.List;

import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.respository.interfaces.Repository;
import rx.Observable;

/**
 * Created by hefuyi on 2016/12/6.
 */

public class GetPlaylistSongs extends UseCase<GetPlaylistSongs.RequestValues,GetPlaylistSongs.ResponseValue>{

    private Repository mRepository;

    public GetPlaylistSongs(Repository repository) {
        mRepository = repository;
    }

    @Override
    public ResponseValue execute(RequestValues requestValues) {
        return new ResponseValue(mRepository.getSongsInPlaylist(requestValues.getPlaylistID()));
    }

    public static final class RequestValues implements UseCase.RequestValues{

        private long playlistID;

        public RequestValues(long playlistID) {
            this.playlistID = playlistID;
        }

        public long getPlaylistID() {
            return playlistID;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {

        private final Observable<List<Song>> mListObservable;

        public ResponseValue(Observable<List<Song>> listObservable) {
            mListObservable = listObservable;
        }

        public Observable<List<Song>> getSongList(){
            return mListObservable;
        }
    }
}
