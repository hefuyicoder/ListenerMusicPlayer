package io.hefuyi.listener.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by hefuyi on 2016/12/31.
 */

public class FavoriteSong {

    private static volatile FavoriteSong sInstance = null;

    private MusicDB mMusicDatabase = null;

    private FavoriteSong(final Context context) {
        mMusicDatabase = MusicDB.getInstance(context);
    }

    public static FavoriteSong getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (FavoriteSong.class) {
                if (sInstance == null) {
                    sInstance = new FavoriteSong(context);
                }
            }
        }
        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FavoriteSong.FavoriteSongColumns.NAME + " ("
                + FavoriteSongColumns.SONGID + " LONG NOT NULL,"
                + FavoriteSongColumns.TIMEADDED + " LONG NOT NULL);");
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteSongColumns.NAME);
        onCreate(db);
    }

    public int addFavoriteSong(final long[] songId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();

        Cursor cursor = null;
        int insert = 0;
        try{
            for (long aSongId : songId) {
                cursor = database.query(FavoriteSongColumns.NAME, new String[]{FavoriteSongColumns.SONGID},
                        FavoriteSongColumns.SONGID + " =? ", new String[]{String.valueOf(songId)}, null, null, null);
                if (cursor != null && cursor.getCount() == 0) { //若无重复则插入
                    ContentValues values = new ContentValues(2);
                    values.put(FavoriteSongColumns.SONGID, aSongId);
                    values.put(FavoriteSongColumns.TIMEADDED, System.currentTimeMillis());
                    database.insert(FavoriteSongColumns.NAME, null, values);
                    insert++;
                }
            }
            return insert;
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    public int removeFavoriteSong(final long[] songId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();

        Cursor cursor = null;
        int deleted = 0;
        try {
            for (long aSongId : songId) {
                cursor = database.query(FavoriteSongColumns.NAME, new String[]{FavoriteSongColumns.SONGID},
                        FavoriteSongColumns.SONGID + " =? ", new String[]{String.valueOf(aSongId)}, null, null, null);
                if (cursor != null && cursor.getCount() >= 0) {
                    database.delete(FavoriteSongColumns.NAME, FavoriteSongColumns.SONGID + " =? ",
                            new String[]{String.valueOf(aSongId)});
                    deleted++;
                }
            }
            return deleted;
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    public Cursor getFavoriteSong() {
        final SQLiteDatabase database = mMusicDatabase.getReadableDatabase();
        Cursor cursor = database.query(FavoriteSongColumns.NAME,
                new String[]{FavoriteSongColumns.SONGID}, null, null, null, null,
                FavoriteSongColumns.TIMEADDED + " DESC", null);
        return cursor;
    }



    public boolean isFavorite(long songId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();

        Cursor cursor = null;
        try{
            cursor = database.query(FavoriteSongColumns.NAME, new String[]{FavoriteSongColumns.SONGID},
                    FavoriteSongColumns.SONGID + " =? ", new String[]{String.valueOf(songId)}, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                return true;
            }
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            database.setTransactionSuccessful();
            database.endTransaction();
        }
        return false;
    }

    public interface FavoriteSongColumns {
        /* Table name */
        String NAME = "favoritesong";

        /* What was searched */
        String SONGID = "songid";

        /* Time of search */
        String TIMEADDED = "timeadded";
    }

}
