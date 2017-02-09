package io.hefuyi.listener.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

import io.hefuyi.listener.util.ListenerUtil;

/**
 * This is used by the music playback service to track the music tracks it is playing
 * It has extra meta data to determine where the track came from so that we can show the appropriate
 * song playing indicator
 * 用来记录当前播放曲目来自什么资源(专辑,歌手,播放列表或无),资源id,在资源中的序号
 */

public class MusicPlaybackTrack implements Parcelable{

    public long mId;
    public long mSourceId;
    public ListenerUtil.IdType mSourceType;
    public int mSourcePosition;

    public static final Creator<MusicPlaybackTrack> CREATOR = new Creator<MusicPlaybackTrack>() {
        @Override
        public MusicPlaybackTrack createFromParcel(Parcel in) {
            return new MusicPlaybackTrack(in);
        }

        @Override
        public MusicPlaybackTrack[] newArray(int size) {
            return new MusicPlaybackTrack[size];
        }
    };

    public MusicPlaybackTrack(Parcel in) {
        mId = in.readLong();
        mSourceId = in.readLong();
        mSourceType = ListenerUtil.IdType.getTypeById(in.readInt());
        mSourcePosition = in.readInt();
    }

    public MusicPlaybackTrack(long id, long sourceId, ListenerUtil.IdType type, int sourcePosition) {
        mId = id;
        mSourceId = sourceId;
        mSourceType = type;
        mSourcePosition = sourcePosition;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeLong(mSourceId);
        dest.writeInt(mSourceType.mId);
        dest.writeInt(mSourcePosition);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MusicPlaybackTrack) {
            MusicPlaybackTrack other = (MusicPlaybackTrack) o;
            return mId == other.mId
                    && mSourceId == other.mSourceId
                    && mSourceType == other.mSourceType
                    && mSourcePosition == other.mSourcePosition;
        }
        return super.equals(o);
    }
}
