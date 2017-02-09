package io.hefuyi.listener.dataloader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.util.PreferencesUtility;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by hefuyi on 2016/11/4.
 */

public class AlbumSongLoader {

    public static Observable<List<Song>> getSongsForAlbum(final Context context, final long albumID) {
        return Observable.create(new Observable.OnSubscribe<List<Song>>() {
            @Override
            public void call(Subscriber<? super List<Song>> subscriber) {
                Cursor cursor = makeAlbumSongCursor(context, albumID);
                List<Song> arrayList = new ArrayList<Song>();
                if ((cursor != null) && (cursor.moveToFirst()))
                    do {
                        long id = cursor.getLong(0);
                        String title = cursor.getString(1);
                        String artist = cursor.getString(2);
                        String album = cursor.getString(3);
                        int duration = cursor.getInt(4);
                        int trackNumber = cursor.getInt(5);
                        /*This fixes bug where some track numbers displayed as 100 or 200*/
                        while (trackNumber >= 1000) {
                            trackNumber -= 1000; //When error occurs the track numbers have an extra 1000 or 2000 added, so decrease till normal.
                        }
                        long artistId = cursor.getInt(6);
                        long albumId = albumID;

                        arrayList.add(new Song(id, albumId, artistId, title, artist, album, duration, trackNumber));
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

    private static Cursor makeAlbumSongCursor(Context context, long albumID) {
        ContentResolver contentResolver = context.getContentResolver();
        final String albumSongSortOrder = PreferencesUtility.getInstance(context).getAlbumSongSortOrder();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String string = "is_music=1 AND title != '' AND album_id=" + albumID;
        Cursor cursor = contentResolver.query(uri, new String[]{"_id", "title", "artist", "album", "duration", "track", "artist_id"}, string, null, albumSongSortOrder);
        return cursor;
    }
}
