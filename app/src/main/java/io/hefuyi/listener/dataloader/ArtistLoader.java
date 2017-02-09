package io.hefuyi.listener.dataloader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import io.hefuyi.listener.mvp.model.Artist;
import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.util.PreferencesUtility;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;


/**
 * Created by hefuyi on 2016/11/4.
 */

public class ArtistLoader {

    private static Observable<Artist> getArtist(final Cursor cursor) {
        return Observable.create(new Observable.OnSubscribe<Artist>() {
            @Override
            public void call(Subscriber<? super Artist> subscriber) {
                Artist artist = new Artist();
                if (cursor != null) {
                    if (cursor.moveToFirst())
                        artist = new Artist(cursor.getLong(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3));
                }
                if (cursor != null){
                    cursor.close();
                }
                subscriber.onNext(artist);
                subscriber.onCompleted();
            }
        });

    }

    private static Observable<List<Artist>> getArtistsForCursor(final Cursor cursor) {
        return Observable.create(new Observable.OnSubscribe<List<Artist>>() {
            @Override
            public void call(Subscriber<? super List<Artist>> subscriber) {
                List<Artist> arrayList = new ArrayList<Artist>();
                if ((cursor != null) && (cursor.moveToFirst()))
                    do {
                        arrayList.add(new Artist(cursor.getLong(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3)));
                    }
                    while (cursor.moveToNext());
                if (cursor != null){
                    cursor.close();
                }
                subscriber.onNext(arrayList);
                subscriber.onCompleted();
            }
        });
    }

    public static Observable<List<Artist>> getFavouriteArtists(final Context context) {
        return SongLoader.getFavoriteSong(context).flatMap(new Func1<List<Song>, Observable<Song>>() {
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

    public static Observable<List<Artist>> getRecentlyPlayedArtist(final Context context) {
        return TopTracksLoader.getTopRecentSongs(context).flatMap(new Func1<List<Song>, Observable<Song>>() {
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

    public static Observable<List<Artist>> getAllArtists(Context context) {
        return getArtistsForCursor(makeArtistCursor(context, null, null));
    }

    public static Observable<Artist> getArtist(Context context, long id) {
        return getArtist(makeArtistCursor(context, "_id=?", new String[]{String.valueOf(id)}));
    }

    public static Observable<List<Artist>> getArtists(Context context, String paramString) {
        return getArtistsForCursor(makeArtistCursor(context, "artist LIKE ?", new String[]{"%" + paramString + "%"}));
    }

    private static Cursor makeArtistCursor(Context context, String selection, String[] paramArrayOfString) {
        final String artistSortOrder = PreferencesUtility.getInstance(context).getArtistSortOrder();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, new String[]{"_id", "artist", "number_of_albums", "number_of_tracks"}, selection, paramArrayOfString, artistSortOrder);
        return cursor;
    }
}
