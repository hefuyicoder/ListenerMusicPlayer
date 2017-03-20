package io.hefuyi.listener.dataloader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.provider.FavoriteSong;
import io.hefuyi.listener.provider.SongPlayCount;
import io.hefuyi.listener.util.PreferencesUtility;
import rx.Observable;
import rx.Subscriber;


/**
 * Created by hefuyi on 2016/11/4.
 */

public class SongLoader {

    public static Observable<List<Song>> getSongsForCursor(final Cursor cursor) {
        return Observable.create(new Observable.OnSubscribe<List<Song>>() {
            @Override
            public void call(Subscriber<? super List<Song>> subscriber) {
                List<Song> arrayList = new ArrayList<Song>();
                if ((cursor != null) && (cursor.moveToFirst()))
                    do {
                        long id = cursor.getLong(0);
                        String title = cursor.getString(1);
                        String artist = cursor.getString(2);
                        String album = cursor.getString(3);
                        int duration = cursor.getInt(4);
                        int trackNumber = cursor.getInt(5);
                        long artistId = cursor.getInt(6);
                        long albumId = cursor.getLong(7);
                        String path = cursor.getString(8);

                        arrayList.add(new Song(id, albumId, artistId, title, artist, album, duration, trackNumber, path));
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

    public static Observable<List<Song>> getFavoriteSong(final Context context) {
        Cursor cursor = FavoriteSong.getInstance(context).getFavoriteSong();
        SortedCursor retCursor = TopTracksLoader.makeSortedCursor(context, cursor, 0);
        return SongLoader.getSongsForCursor(retCursor);
    }

    public static Observable<List<Song>> getSongsWithScoreForCursor(final Cursor cursor, final Cursor scoreCursor) {
        return Observable.create(new Observable.OnSubscribe<List<Song>>() {
            @Override
            public void call(Subscriber<? super List<Song>> subscriber) {
                List<Song> arrayList = new ArrayList<Song>();
                if ((cursor != null&&scoreCursor!=null) && (cursor.moveToFirst()&&scoreCursor.moveToFirst()))
                    do {
                        long id = cursor.getLong(0);
                        String title = cursor.getString(1);
                        String artist = cursor.getString(2);
                        String album = cursor.getString(3);
                        int duration = cursor.getInt(4);
                        int trackNumber = cursor.getInt(5);
                        long artistId = cursor.getInt(6);
                        long albumId = cursor.getLong(7);
                        String path = cursor.getString(8);

                        Song song = new Song(id, albumId, artistId, title, artist, album, duration, trackNumber, path);
                        song.setPlayCountScore(scoreCursor.getFloat(scoreCursor.getColumnIndex(SongPlayCount.SongPlayCountColumns.PLAYCOUNTSCORE)));
                        arrayList.add(song);
                    }
                    while (cursor.moveToNext() && scoreCursor.moveToNext());
                if (cursor != null){
                    cursor.close();
                }
                if (scoreCursor != null) {
                    scoreCursor.close();
                }
                subscriber.onNext(arrayList);
                subscriber.onCompleted();
            }
        });
    }

    public static Observable<List<Song>> getAllSongs(Context context) {
        return getSongsForCursor(makeSongCursor(context, null, null));
    }

    public static Observable<List<Song>> searchSongs(Context context, String searchString) {
        return getSongsForCursor(makeSongCursor(context, "title LIKE ? or artist LIKE ? or album LIKE ? ",
                new String[]{"%" + searchString + "%", "%" + searchString + "%", "%" + searchString + "%"}));
    }

    public static Observable<List<Song>> getSongListInFolder(Context context, String path) {
        String[] whereArgs = new String[]{path + "%"};
        return getSongsForCursor(makeSongCursor(context, MediaStore.Audio.Media.DATA + " LIKE ?", whereArgs, null));
    }


    public static Cursor makeSongCursor(Context context, String selection, String[] paramArrayOfString) {
        final String songSortOrder = PreferencesUtility.getInstance(context).getSongSortOrder();
        return makeSongCursor(context, selection, paramArrayOfString, songSortOrder);
    }

    private static Cursor makeSongCursor(Context context, String selection, String[] paramArrayOfString, String sortOrder) {
        String selectionStatement = "is_music=1 AND title != ''";

        if (!TextUtils.isEmpty(selection)) {
            selectionStatement = selectionStatement + " AND " + selection;
        }
        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{"_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id", MediaStore.Audio.Media.DATA},
                selectionStatement, paramArrayOfString, sortOrder);

    }
}
