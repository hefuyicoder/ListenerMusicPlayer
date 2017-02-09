package io.hefuyi.listener.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import io.hefuyi.listener.mvp.model.MusicPlaybackTrack;
import io.hefuyi.listener.util.ListenerUtil;

/**
 * This keeps track of the music playback and history state of the playback service
 */

public class MusicPlaybackState {

    private static volatile MusicPlaybackState sInstance = null;

    private MusicDB mMusicDatabase = null;

    private MusicPlaybackState(final Context context) {
        mMusicDatabase = MusicDB.getInstance(context);
    }

    public static MusicPlaybackState getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (MusicPlaybackState.class) {
                if (sInstance == null) {
                    sInstance = new MusicPlaybackState(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(PlaybackQueueColumns.NAME);
        builder.append("(");

        builder.append(PlaybackQueueColumns.TRACK_ID);
        builder.append(" LONG NOT NULL,");

        builder.append(PlaybackQueueColumns.SOURCE_ID);
        builder.append(" LONG NOT NULL,");

        builder.append(PlaybackQueueColumns.SOURCE_TYPE);
        builder.append(" INT NOT NULL,");

        builder.append(PlaybackQueueColumns.SOURCE_POSITION);
        builder.append(" INT NOT NULL);");

        db.execSQL(builder.toString());

        builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(PlaybackHistoryColumns.NAME);
        builder.append("(");

        builder.append(PlaybackHistoryColumns.POSITION);
        builder.append(" INT NOT NULL);");

        db.execSQL(builder.toString());
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // this table was created in version 2 so call the onCreate method if we hit that scenario
        if (oldVersion < 2 && newVersion >= 2) {
            onCreate(db);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + PlaybackQueueColumns.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PlaybackHistoryColumns.NAME);
        onCreate(db);
    }

    /**
     * 将输入数据保存到两个表中(旧数据被清除)
     * @param queue
     * @param history
     */
    public synchronized void saveState(final ArrayList<MusicPlaybackTrack> queue,
                                       LinkedList<Integer> history) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();

        //清空旧数据
        try {
            database.delete(PlaybackQueueColumns.NAME, null, null);
            database.delete(PlaybackHistoryColumns.NAME, null, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        final int NUM_PROCESS = 20;
        int position = 0;
        while (position < queue.size()) {
            database.beginTransaction();
            try {
                for (int i = position; i < queue.size() && i < position + NUM_PROCESS; i++) { //一次最多批量插入NUM_PROCESS条记录
                    MusicPlaybackTrack track = queue.get(i);
                    ContentValues values = new ContentValues(4);

                    values.put(PlaybackQueueColumns.TRACK_ID, track.mId);
                    values.put(PlaybackQueueColumns.SOURCE_ID, track.mSourceId);
                    values.put(PlaybackQueueColumns.SOURCE_TYPE, track.mSourceType.mId);
                    values.put(PlaybackQueueColumns.SOURCE_POSITION, track.mSourcePosition);

                    database.insert(PlaybackQueueColumns.NAME, null, values);
                }
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
                position += NUM_PROCESS;
            }
        }

        if (history != null) {
            Iterator<Integer> iter = history.iterator();
            while (iter.hasNext()) {
                database.beginTransaction();
                try {
                    for (int i = 0; iter.hasNext() && i < NUM_PROCESS; i++) {
                        ContentValues values = new ContentValues(1);
                        values.put(PlaybackHistoryColumns.POSITION, iter.next());

                        database.insert(PlaybackHistoryColumns.NAME, null, values);
                    }

                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                }
            }
        }
    }

    /**
     * 获取playbackqueue表中的数据
     * @return
     */
    public ArrayList<MusicPlaybackTrack> getQueue() {
        ArrayList<MusicPlaybackTrack> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaybackQueueColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
                    results.add(new MusicPlaybackTrack(cursor.getLong(0), cursor.getLong(1),
                            ListenerUtil.IdType.getTypeById(cursor.getInt(2)), cursor.getInt(3)));
                } while (cursor.moveToNext());
            }

            return results;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    /**
     * 获取playbackhistory表中的数据
     * @param playlistSize
     * @return
     */
    public LinkedList<Integer> getHistory(final int playlistSize) {
        LinkedList<Integer> results = new LinkedList<>();

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaybackHistoryColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int pos = cursor.getInt(0);
                    if (pos >= 0 && pos < playlistSize) {
                        results.add(pos);
                    }
                } while (cursor.moveToNext());
            }

            return results;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    private static class PlaybackQueueColumns {

        static final String NAME = "playbackqueue";
        static final String TRACK_ID = "trackid";
        static final String SOURCE_ID = "sourceid";
        static final String SOURCE_TYPE = "sourcetype";
        static final String SOURCE_POSITION = "sourceposition";
    }

    private static class PlaybackHistoryColumns {

        static final String NAME = "playbackhistory";
        static final String POSITION = "position";
    }
}
