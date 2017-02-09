package io.hefuyi.listener.dataloader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
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

    private static final long[] sEmptyList = new long[0];

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

    private static Observable<Song> getSongForCursor(final Cursor cursor) {
        return Observable.create(new Observable.OnSubscribe<Song>() {
            @Override
            public void call(Subscriber<? super Song> subscriber) {
                Song song = new Song();
                if ((cursor != null) && (cursor.moveToFirst())) {
                    long id = cursor.getLong(0);
                    String title = cursor.getString(1);
                    String artist = cursor.getString(2);
                    String album = cursor.getString(3);
                    int duration = cursor.getInt(4);
                    int trackNumber = cursor.getInt(5);
                    long artistId = cursor.getInt(6);
                    long albumId = cursor.getLong(7);
                    String path = cursor.getString(8);

                    song = new Song(id, albumId, artistId, title, artist, album, duration, trackNumber,path);
                }

                if (cursor != null){
                    cursor.close();
                }
                subscriber.onNext(song);
                subscriber.onCompleted();
            }
        });
    }

    public static long[] getSongListForCursor(Cursor cursor) {
        if (cursor == null) {
            return sEmptyList;
        }
        final int len = cursor.getCount();
        final long[] list = new long[len];
        cursor.moveToFirst();
        int columnIndex = -1;
        try {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
        } catch (final IllegalArgumentException notaplaylist) {
            columnIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);
        }
        for (int i = 0; i < len; i++) {
            list[i] = cursor.getLong(columnIndex);
            cursor.moveToNext();
        }
        cursor.close();
        cursor = null;
        return list;
    }

    public static Observable<List<Song>> getAllSongs(Context context) {
        return getSongsForCursor(makeSongCursor(context, null, null));
    }

    public static Observable<Song> getSongForID(Context context, long id) {
        return getSongForCursor(makeSongCursor(context, "_id=" + String.valueOf(id), null));
    }

    public static Observable<List<Song>> getSongsForIDs(Context context, long[] ids) {
        StringBuilder selection = new StringBuilder();
        selection.append(BaseColumns._ID);
        selection.append(" IN (");

        for (int i = 0; i < ids.length - 1; i++) {
            selection.append(ids[i]);
            selection.append(",");
        }
        if (ids.length != 0) {
            selection.append(ids[ids.length - 1]);
        }
        selection.append(")");

        return getSongsForCursor(makeSongCursor(context, selection.toString(), null, null));
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
