package io.hefuyi.listener.dataloader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.provider.RecentStore;
import io.hefuyi.listener.provider.SongPlayCount;
import rx.Observable;

public class TopTracksLoader extends SongLoader {

    public static final int NUMBER_OF_SONGS = 99;

    public static Observable<List<Song>> getTopPlaySongs(Context context) {
        Cursor songsIdWithScore = SongPlayCount.getInstance(context).getTopPlayedResults(NUMBER_OF_SONGS);
        SortedCursor retCursor=makeSortedCursor(context, songsIdWithScore,
                songsIdWithScore.getColumnIndex(SongPlayCount.SongPlayCountColumns.ID));

        if (retCursor != null) {
            ArrayList<Long> missingIds = retCursor.getMissingIds();
            if (missingIds != null && missingIds.size() > 0) {
                for (long id : missingIds) {
                    SongPlayCount.getInstance(context).removeItem(id);
                }
            }
        }

        return SongLoader.getSongsWithScoreForCursor(retCursor, songsIdWithScore);
    }

    public static Observable<List<Song>> getTopRecentSongs(Context context) {
        SortedCursor retCursor = makeRecentTracksCursor(context);

        if (retCursor != null) {
            ArrayList<Long> missingIds = retCursor.getMissingIds();
            long[] ids = new long[missingIds.size()];
            int i = 0;
            if (missingIds != null && missingIds.size() > 0) {
                for (long id : missingIds) {
                    ids[i] = id;
                    i++;
                }
                RecentStore.getInstance(context).removeItem(ids);
            }
        }

        return SongLoader.getSongsForCursor(retCursor);
    }

    /**
     * 获取最近播放歌曲的cursor
     * @param context
     * @return
     */
    public static SortedCursor makeRecentTracksCursor(final Context context) {

        Cursor songs = RecentStore.getInstance(context).queryRecentIds(null);

        try {
            return makeSortedCursor(context, songs,
                    songs.getColumnIndex(SongPlayCount.SongPlayCountColumns.ID));
        } finally {
            if (songs != null) {
                songs.close();
            }
        }
    }

    /**
     * 根据包含song id的cursor,获取排序好的song cursor
     * @param context
     * @param cursor
     * @param idColumn
     * @return
     */
    public static SortedCursor makeSortedCursor(final Context context, final Cursor cursor,
                                                final int idColumn) {
        if (cursor != null && cursor.moveToFirst()) {

            StringBuilder selection = new StringBuilder();
            selection.append(BaseColumns._ID);
            selection.append(" IN (");

            long[] order = new long[cursor.getCount()];

            long id = cursor.getLong(idColumn);
            selection.append(id);
            order[cursor.getPosition()] = id;

            while (cursor.moveToNext()) {
                selection.append(",");

                id = cursor.getLong(idColumn);
                order[cursor.getPosition()] = id;
                selection.append(String.valueOf(id));
            }

            selection.append(")");

            Cursor songCursor = makeSongCursor(context, selection.toString(), null);
            if (songCursor != null) {
                return new SortedCursor(songCursor, order, BaseColumns._ID, null);
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return null;
    }

}
