package io.hefuyi.listener.dataloader;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import io.hefuyi.listener.mvp.model.Song;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by hefuyi on 2016/11/6.
 */

public class QueueLoader {

    public static Observable<List<Song>> getQueueSongs(final Context context) {
        return Observable.create(new Observable.OnSubscribe<List<Song>>() {
            @Override
            public void call(Subscriber<? super List<Song>> subscriber) {
                final List<Song> mSongList = new ArrayList<>();
                Cursor mCursor = new NowPlayingCursor(context);

                if (mCursor.moveToFirst()) {
                    do {

                        final long id = mCursor.getLong(0);

                        final String songName = mCursor.getString(1);

                        final String artist = mCursor.getString(2);

                        final long albumId = mCursor.getLong(3);

                        final String album = mCursor.getString(4);

                        final int duration = mCursor.getInt(5);

                        final long artistid = mCursor.getInt(6);

                        final int tracknumber = mCursor.getInt(7);

                        final Song song = new Song(id, albumId, artistid, songName, artist, album, duration, tracknumber);

                        mSongList.add(song);
                    } while (mCursor.moveToNext());
                }
                subscriber.onNext(mSongList);
                subscriber.onCompleted();
                mCursor.close();
                mCursor = null;
            }
        });
    }
}
