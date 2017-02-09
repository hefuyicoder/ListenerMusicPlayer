package io.hefuyi.listener.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by hefuyi on 2016/11/5.
 */

public class SearchHistory {

    private static final int MAX_ITEMS_IN_DB = 25;

    private static volatile SearchHistory sInstance = null;

    private MusicDB mMusicDatabase = null;

    private SearchHistory(final Context context) {
        mMusicDatabase = MusicDB.getInstance(context);
    }

    public static SearchHistory getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (SearchHistory.class) {
                if (sInstance == null) {
                    sInstance = new SearchHistory(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SearchHistoryColumns.NAME + " ("
                + SearchHistoryColumns.SEARCHSTRING + " STRING NOT NULL,"
                + SearchHistoryColumns.TIMESEARCHED + " LONG NOT NULL);");
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SearchHistoryColumns.NAME);
        onCreate(db);
    }

    /**
     * 添加搜索记录,并删除溢出记录
     * @param searchString
     */
    public void addSearchString(final String searchString) {
        if (searchString == null) {
            return;
        }

        String trimmedString = searchString.trim();

        if (trimmedString.isEmpty()) {
            return;
        }

        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();

        try {

            database.delete(SearchHistoryColumns.NAME,
                    SearchHistoryColumns.SEARCHSTRING + " = ? COLLATE NOCASE",
                    new String[]{trimmedString});

            final ContentValues values = new ContentValues(2);
            values.put(SearchHistoryColumns.SEARCHSTRING, trimmedString);
            values.put(SearchHistoryColumns.TIMESEARCHED, System.currentTimeMillis());
            database.insert(SearchHistoryColumns.NAME, null, values);

            Cursor oldest = null;
            try {
                oldest=database.query(SearchHistoryColumns.NAME,
                        new String[]{SearchHistoryColumns.TIMESEARCHED}, null, null, null, null,
                        SearchHistoryColumns.TIMESEARCHED + " ASC");

                if (oldest != null && oldest.getCount() > MAX_ITEMS_IN_DB) {
                    oldest.moveToPosition(oldest.getCount() - MAX_ITEMS_IN_DB);
                    long timeOfRecordToKeep = oldest.getLong(0);

                    database.delete(SearchHistoryColumns.NAME,
                            SearchHistoryColumns.TIMESEARCHED + " < ?",
                            new String[]{String.valueOf(timeOfRecordToKeep)});

                }
            } finally {
                if (oldest != null) {
                    oldest.close();
                    oldest = null;
                }
            }
        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    /**
     * 获取最近搜索的n条记录
     * @param limit
     * @return
     */
    public Cursor queryRecentSearches(final String limit) {
        final SQLiteDatabase database = mMusicDatabase.getReadableDatabase();
        return database.query(SearchHistoryColumns.NAME,
                new String[]{SearchHistoryColumns.SEARCHSTRING}, null, null, null, null,
                SearchHistoryColumns.TIMESEARCHED + " DESC", limit);
    }

    interface SearchHistoryColumns {
        /* Table name */
        String NAME = "searchhistory";

        /* What was searched */
        String SEARCHSTRING = "searchstring";

        /* Time of search */
        String TIMESEARCHED = "timesearched";
    }

}
