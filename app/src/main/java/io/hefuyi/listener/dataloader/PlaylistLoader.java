package io.hefuyi.listener.dataloader;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import io.hefuyi.listener.mvp.model.Playlist;
import io.hefuyi.listener.util.ListenerUtil;
import rx.Observable;
import rx.Subscriber;

import static io.hefuyi.listener.util.ListenerUtil.MUSIC_ONLY_SELECTION;

/**
 * Created by hefuyi on 2016/11/4.
 */

public class PlaylistLoader {

    private static ArrayList<Playlist> mPlaylistList;

    public static Observable<List<Playlist>> getPlaylists(final Context context, final boolean defaultIncluded) {
        return Observable.create(new Observable.OnSubscribe<List<Playlist>>() {
            @Override
            public void call(Subscriber<? super List<Playlist>> subscriber) {
                mPlaylistList = new ArrayList<>();

                if (defaultIncluded)
                    makeDefaultPlaylists(context);

                Cursor mCursor = makePlaylistCursor(context);

                if (mCursor != null && mCursor.moveToFirst()) {
                    do {

                        final long id = mCursor.getLong(0);

                        final String name = mCursor.getString(1);

                        final int songCount = getSongCountForPlaylist(context, id);

                        final Playlist playlist = new Playlist(id, name, songCount);

                        mPlaylistList.add(playlist);
                    } while (mCursor.moveToNext());
                }
                if (mCursor != null) {
                    mCursor.close();
                    mCursor = null;
                }
                subscriber.onNext(mPlaylistList);
                subscriber.onCompleted();
            }
        });
    }

    private static void makeDefaultPlaylists(Context context) {
        final Resources resources = context.getResources();

        final Playlist topTracks = new Playlist(ListenerUtil.PlaylistType.Favourate.mId,
                resources.getString(ListenerUtil.PlaylistType.Favourate.mTitleId), -1);
        mPlaylistList.add(topTracks);
    }

    private static Cursor makePlaylistCursor(final Context context) {
        return context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{
                        BaseColumns._ID,
                        MediaStore.Audio.PlaylistsColumns.NAME
                }, null, null, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
    }

    private static int getSongCountForPlaylist(final Context context, final long playlistId) {
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                new String[]{BaseColumns._ID}, MUSIC_ONLY_SELECTION, null, null);

        if (c != null) {
            int count = 0;
            if (c.moveToFirst()) {
                count = c.getCount();
            }
            c.close();
            c = null;
            return count;
        }

        return 0;
    }

    public static void deletePlaylists(Context context, long playlistId) {
        Uri localUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append("_id IN (");
        localStringBuilder.append((playlistId));
        localStringBuilder.append(")");
        context.getContentResolver().delete(localUri, localStringBuilder.toString(), null);
    }

}
