package io.hefuyi.listener.dataloader;

import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import java.util.Arrays;

import io.hefuyi.listener.MusicPlayer;

/**
 * Created by hefuyi on 2016/11/4.
 */

public class NowPlayingCursor extends AbstractCursor {

    private static final String[] PROJECTION = new String[]{

            BaseColumns._ID,

            MediaStore.Audio.AudioColumns.TITLE,

            MediaStore.Audio.AudioColumns.ARTIST,

            MediaStore.Audio.AudioColumns.ALBUM_ID,

            MediaStore.Audio.AudioColumns.ALBUM,

            MediaStore.Audio.AudioColumns.DURATION,

            MediaStore.Audio.AudioColumns.ARTIST_ID,

            MediaStore.Audio.AudioColumns.TRACK,
    };

    private final Context mContext;

    private long[] mNowPlaying;

    private long[] mCursorIndexes;

    private int mSize;

    private Cursor mQueueCursor;


    public NowPlayingCursor(final Context context) {
        mContext = context;
        makeNowPlayingCursor();
    }

    @Override
    public int getCount() {
        return mSize;
    }

    @Override
    public boolean onMove(final int oldPosition, final int newPosition) {
        if (oldPosition == newPosition) {
            return true;
        }

        if (mNowPlaying == null || mCursorIndexes == null || newPosition >= mNowPlaying.length) {
            return false;
        }

        final long id = mNowPlaying[newPosition];
        final int cursorIndex = Arrays.binarySearch(mCursorIndexes, id);
        mQueueCursor.moveToPosition(cursorIndex);
        return true;
    }

    @Override
    public String getString(final int column) {
        try {
            return mQueueCursor.getString(column);
        } catch (final Exception ignored) {
            onChange(true);
            return "";
        }
    }

    @Override
    public short getShort(final int column) {
        return mQueueCursor.getShort(column);
    }

    @Override
    public int getInt(final int column) {
        try {
            return mQueueCursor.getInt(column);
        } catch (final Exception ignored) {
            onChange(true);
            return 0;
        }
    }

    @Override
    public long getLong(final int column) {
        try {
            return mQueueCursor.getLong(column);
        } catch (final Exception ignored) {
            onChange(true);
            return 0;
        }
    }

    @Override
    public float getFloat(final int column) {
        return mQueueCursor.getFloat(column);
    }


    @Override
    public double getDouble(final int column) {
        return mQueueCursor.getDouble(column);
    }


    @Override
    public int getType(final int column) {
        return mQueueCursor.getType(column);
    }

    @Override
    public boolean isNull(final int column) {
        return mQueueCursor.isNull(column);
    }


    @Override
    public String[] getColumnNames() {
        return PROJECTION;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void deactivate() {
        if (mQueueCursor != null) {
            mQueueCursor.deactivate();
        }
    }

    @Override
    public boolean requery() {
        makeNowPlayingCursor();
        return true;
    }

    @Override
    public void close() {
        try {
            if (mQueueCursor != null) {
                mQueueCursor.close();
                mQueueCursor = null;
            }
        } catch (final Exception close) {
        }
        super.close();
    }

    /**
     * 获取播放队列中的歌曲,若本地歌曲已删除则进行校正
     */
    private void makeNowPlayingCursor() {
        mQueueCursor = null;
        mNowPlaying = MusicPlayer.getQueue(); //获取正在播放的歌曲的ID数组(MusicPlaybackTrack的mid)
        mSize = mNowPlaying.length;
        if (mSize == 0) {
            return;
        }

        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Media._ID + " IN (");
        for (int i = 0; i < mSize; i++) {
            selection.append(mNowPlaying[i]);
            if (i < mSize - 1) {
                selection.append(",");
            }
        }
        selection.append(")");

        mQueueCursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, selection.toString(),
                null, MediaStore.Audio.Media._ID);

        if (mQueueCursor == null) {
            mSize = 0;
            return;
        }

        final int playlistSize = mQueueCursor.getCount();
        mCursorIndexes = new long[playlistSize];
        mQueueCursor.moveToFirst();
        final int columnIndex = mQueueCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
        for (int i = 0; i < playlistSize; i++) {
            mCursorIndexes[i] = mQueueCursor.getLong(columnIndex); //mCursorIndexes 保存设备查询出的歌曲的乱序ID
            mQueueCursor.moveToNext();
        }
        mQueueCursor.moveToFirst();

        int removed = 0;
        for (int i = mNowPlaying.length - 1; i >= 0; i--) { //遍历播放列表中歌曲的ID
            final long trackId = mNowPlaying[i];
            final int cursorIndex = Arrays.binarySearch(mCursorIndexes, trackId); //mCursorIndexes是乱序的 mNowPlaying才是真正的播放列表顺序
            if (cursorIndex < 0) {
                removed += MusicPlayer.removeTrack(trackId);
            }
        }
        if (removed > 0) {
            mNowPlaying = MusicPlayer.getQueue();
            mSize = mNowPlaying.length;
            if (mSize == 0) {
                mCursorIndexes = null;
                return;
            }
        }
    }
}
