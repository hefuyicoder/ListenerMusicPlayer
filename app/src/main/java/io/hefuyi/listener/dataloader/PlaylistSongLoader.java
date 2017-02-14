package io.hefuyi.listener.dataloader;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import io.hefuyi.listener.mvp.model.Song;
import rx.Observable;

public class PlaylistSongLoader {

    public static Observable<List<Song>> getSongsInPlaylist(Context context, long playlistID) {

        ArrayList<Song> mSongList = new ArrayList<>();

        final int playlistCount = countPlaylist(context, playlistID);

        Cursor cursor = makePlaylistSongCursor(context, playlistID);

        if (cursor != null) {
            boolean runCleanup = false;
            if (cursor.getCount() != playlistCount) {
                runCleanup = true;
            }

            if (!runCleanup && cursor.moveToFirst()) {
                final int playOrderCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.PLAY_ORDER);

                int lastPlayOrder = -1;
                do {
                    int playOrder = cursor.getInt(playOrderCol);
                    if (playOrder == lastPlayOrder) {
                        runCleanup = true;
                        break;
                    }
                    lastPlayOrder = playOrder;
                } while (cursor.moveToNext());
            }

            if (runCleanup) {

                cleanupPlaylist(context, playlistID, cursor);

                cursor.close();
                cursor = makePlaylistSongCursor(context, playlistID);
                if (cursor != null) {
                }
            }
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {

                final long id = cursor.getLong(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID));

                final String songName = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE));

                final String artist = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST));

                final long albumId = cursor.getLong(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID));

                final long artistId = cursor.getLong(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST_ID));

                final String album = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM));

                final long duration = cursor.getLong(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION));

                final int durationInSecs = (int) duration / 1000;

                final int tracknumber = cursor.getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TRACK));

                final Song song = new Song(id, albumId, artistId, songName, artist, album, durationInSecs, tracknumber);

                mSongList.add(song);
            } while (cursor.moveToNext());
        }
        // Close the cursor
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        return Observable.from(mSongList).toList();
    }

    /**
     * 清空某playlist,用cursor填充
     * @param context
     * @param playlistId
     * @param cursor
     */
    private static void cleanupPlaylist(final Context context, final long playlistId,
                                        final Cursor cursor) {
        final int idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation.newDelete(uri).build());

        final int YIELD_FREQUENCY = 100;

        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            do {
                final ContentProviderOperation.Builder builder =
                        ContentProviderOperation.newInsert(uri)
                                .withValue(MediaStore.Audio.Playlists.Members.PLAY_ORDER, cursor.getPosition())
                                .withValue(MediaStore.Audio.Playlists.Members.AUDIO_ID, cursor.getLong(idCol));

                if ((cursor.getPosition() + 1) % YIELD_FREQUENCY == 0) {
                    builder.withYieldAllowed(true);
                }
                ops.add(builder.build());
            } while (cursor.moveToNext());
        }

        try {
            context.getContentResolver().applyBatch(MediaStore.AUTHORITY, ops);
        } catch (RemoteException e) {
        } catch (OperationApplicationException e) {
        }
    }


    /**
     * 计算某playlist中媒体文件的数量
     * @param context
     * @param playlistId
     * @return
     */
    private static int countPlaylist(final Context context, final long playlistId) {
        Cursor c = null;
        try {
            c = context.getContentResolver().query(
                    MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                    new String[]{
                            MediaStore.Audio.Playlists.Members.AUDIO_ID,
                    }, null, null,
                    MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);

            if (c != null) {
                return c.getCount();
            }
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }

        return 0;
    }

    public static void removeFromPlaylist(final Context context, final long[] ids,
                                          final long playlistId) {
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        final ContentResolver resolver = context.getContentResolver();

        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Playlists.Members.AUDIO_ID + " IN (");
        for (int i = 0; i < ids.length; i++) {
            selection.append(ids[i]);
            if (i < ids.length - 1) {
                selection.append(",");
            }
        }
        selection.append(")");

        resolver.delete(uri, selection.toString(), null);
    }

    /**
     * 获取某playlist中规范音乐文件的数量
     * @param context
     * @param playlistID
     * @return
     */
    private static Cursor makePlaylistSongCursor(final Context context, final Long playlistID) {
        final StringBuilder mSelection = new StringBuilder();
        mSelection.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1");
        mSelection.append(" AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''");
        return context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistID),
                new String[]{
                        MediaStore.Audio.Playlists.Members._ID,
                        MediaStore.Audio.Playlists.Members.AUDIO_ID,
                        MediaStore.Audio.AudioColumns.TITLE,
                        MediaStore.Audio.AudioColumns.ARTIST,
                        MediaStore.Audio.AudioColumns.ALBUM_ID,
                        MediaStore.Audio.AudioColumns.ARTIST_ID,
                        MediaStore.Audio.AudioColumns.ALBUM,
                        MediaStore.Audio.AudioColumns.DURATION,
                        MediaStore.Audio.AudioColumns.TRACK,
                        MediaStore.Audio.Playlists.Members.PLAY_ORDER,
                }, mSelection.toString(), null,
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
    }
}
