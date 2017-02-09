package io.hefuyi.listener.dataloader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import io.hefuyi.listener.mvp.model.Album;
import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.util.PreferencesUtility;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;


/**
 * Created by hefuyi on 2016/11/3.
 */

public class AlbumLoader {

    private static Observable<Album> getAlbum(final Cursor cursor) {
        return Observable.create(new Observable.OnSubscribe<Album>() {
            @Override
            public void call(Subscriber<? super Album> subscriber) {
                Album album = new Album();
                if (cursor != null) {
                    if (cursor.moveToFirst())
                        album = new Album(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getLong(3), cursor.getInt(4), cursor.getInt(5));
                }
                if (cursor != null){
                    cursor.close();
                }
                subscriber.onNext(album);
                subscriber.onCompleted();
            }
        });
    }

    public static Observable<List<Album>> getFavourateAlbums(final Context context) {
        return SongLoader.getFavoriteSong(context).flatMap(new Func1<List<Song>, Observable<Song>>() {
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

    public static Observable<List<Album>> getRecentlyPlayedAlbums(final Context context) {
        return TopTracksLoader.getTopRecentSongs(context).flatMap(new Func1<List<Song>, Observable<Song>>() {
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

    private static Observable<List<Album>> getAlbumsForCursor(final Cursor cursor) {
        return Observable.create(new Observable.OnSubscribe<List<Album>>() {
            @Override
            public void call(Subscriber<? super List<Album>> subscriber) {
                List<Album> arrayList = new ArrayList<Album>();
                if ((cursor != null) && (cursor.moveToFirst()))
                    do {
                        arrayList.add(new Album(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getLong(3), cursor.getInt(4), cursor.getInt(5)));
                    }
                    while (cursor.moveToNext());
                if (cursor != null) {
                    cursor.close();
                }
                subscriber.onNext(arrayList);
                subscriber.onCompleted();
            }
        });
    }

    public static Observable<List<Album>> getAllAlbums(Context context) {
        return getAlbumsForCursor(makeAlbumCursor(context, null, null));
    }

    public static Observable<Album> getAlbum(Context context, long id) {
        return getAlbum(makeAlbumCursor(context, "_id=?", new String[]{String.valueOf(id)}));
    }

    public static Observable<List<Album>> getAlbums(Context context, String paramString) {
        return getAlbumsForCursor(makeAlbumCursor(context, "album LIKE ? or artist LIKE ? ",
                new String[]{"%" + paramString + "%", "%" + paramString + "%"}));
    }

    private static Cursor makeAlbumCursor(Context context, String selection, String[] paramArrayOfString) {
        final String albumSortOrder = PreferencesUtility.getInstance(context).getAlbumSortOrder();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{"_id", "album", "artist", "artist_id", "numsongs", "minyear"}, selection, paramArrayOfString, albumSortOrder);

        return cursor;
    }
}
