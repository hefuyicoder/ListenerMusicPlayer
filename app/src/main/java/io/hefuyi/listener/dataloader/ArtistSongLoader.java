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

public class ArtistSongLoader {

    public static Observable<List<Song>> getSongsForArtist(final Context context, final long artistID) {
        return Observable.create(new Observable.OnSubscribe<List<Song>>() {
            @Override
            public void call(Subscriber<? super List<Song>> subscriber) {
                Cursor cursor = makeArtistSongCursor(context, artistID);
                List<Song> songsList = new ArrayList<Song>();
                if ((cursor != null) && (cursor.moveToFirst()))
                    do {
                        long id = cursor.getLong(0);
                        String title = cursor.getString(1);
                        String artist = cursor.getString(2);
                        String album = cursor.getString(3);
                        int duration = cursor.getInt(4);
                        int trackNumber = cursor.getInt(5);
                        long albumId = cursor.getInt(6);
                        long artistId = artistID;

                        songsList.add(new Song(id, albumId, artistId, title, artist, album, duration, trackNumber));
                    }
                    while (cursor.moveToNext());
                if (cursor != null){
                    cursor.close();
                    cursor = null;
                }
                subscriber.onNext(songsList);
                subscriber.onCompleted();
            }
        });
    }


    private static Cursor makeArtistSongCursor(Context context, long artistID) {
        ContentResolver contentResolver = context.getContentResolver();
        final String artistSongSortOrder = PreferencesUtility.getInstance(context).getArtistSongSortOrder();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String string = "is_music=1 AND title != '' AND artist_id=" + artistID;
        return contentResolver.query(uri, new String[]{"_id", "title", "artist", "album", "duration", "track", "album_id"}, string, null, artistSongSortOrder);
    }
}
