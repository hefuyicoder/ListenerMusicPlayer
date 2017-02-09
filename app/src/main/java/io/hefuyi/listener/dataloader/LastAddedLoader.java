package io.hefuyi.listener.dataloader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import io.hefuyi.listener.mvp.model.Album;
import io.hefuyi.listener.mvp.model.Artist;
import io.hefuyi.listener.mvp.model.Song;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by hefuyi on 2016/11/4.
 */

public class LastAddedLoader {

    public static Observable<List<Song>> getLastAddedSongs(final Context context) {
        return Observable.create(new Observable.OnSubscribe<List<Song>>() {
            @Override
            public void call(Subscriber<? super List<Song>> subscriber) {
                List<Song> mSongList = new ArrayList<>();
                Cursor mCursor = makeLastAddedCursor(context);

                if (mCursor != null && mCursor.moveToFirst()) {
                    do {
                        long id = mCursor.getLong(0);
                        String title = mCursor.getString(1);
                        String artist = mCursor.getString(2);
                        String album = mCursor.getString(3);
                        int duration = mCursor.getInt(4);
                        int trackNumber = mCursor.getInt(5);
                        long artistId = mCursor.getInt(6);
                        long albumId = mCursor.getLong(7);

                        final Song song = new Song(id, albumId, artistId, title, artist, album, duration, trackNumber);

                        mSongList.add(song);
                    } while (mCursor.moveToNext());
                }
                if (mCursor != null) {
                    mCursor.close();
                    mCursor = null;
                }
                subscriber.onNext(mSongList);
                subscriber.onCompleted();
            }
        });
    }

    public static Observable<List<Album>> getLastAddedAlbums(final Context context) {
        return getLastAddedSongs(context).flatMap(new Func1<List<Song>, Observable<Song>>() {
            @Override
            public Observable<Song> call(List<Song> songList) {
                return Observable.from(songList);
            }
        }).distinct(new Func1<Song, Long>() {
            @Override
            public Long call(Song song) {
                return song.albumId;
            }
        }).flatMap(new Func1<Song, Observable<Album>>() {
            @Override
            public Observable<Album> call(Song song) {
                return AlbumLoader.getAlbum(context, song.albumId);
            }
        }).toList();
    }

    public static Observable<List<Artist>> getLastAddedArtist(final Context context){
        return getLastAddedSongs(context).flatMap(new Func1<List<Song>, Observable<Song>>() {
            @Override
            public Observable<Song> call(List<Song> songList) {
                return Observable.from(songList);
            }
        }).distinct(new Func1<Song, Long>() {
            @Override
            public Long call(Song song) {
                return song.artistId;
            }
        }).flatMap(new Func1<Song, Observable<Artist>>() {
            @Override
            public Observable<Artist> call(Song song) {
                return ArtistLoader.getArtist(context, song.artistId);
            }
        }).toList();
    }

    private static Cursor makeLastAddedCursor(final Context context) {
        //four weeks ago
        long fourWeeksAgo = (System.currentTimeMillis() / 1000) - (4 * 3600 * 24 * 7);
        long cutoff = 0L;
        // use the most recent of the two timestamps
        if (cutoff < fourWeeksAgo) {
            cutoff = fourWeeksAgo;
        }

        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1");
        selection.append(" AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''");
        selection.append(" AND " + MediaStore.Audio.Media.DATE_ADDED + ">");
        selection.append(cutoff);

        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{"_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id"}, selection.toString(), null, MediaStore.Audio.Media.DATE_ADDED + " DESC");
    }

}
