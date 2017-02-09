package io.hefuyi.listener.event;

/**
 * Created by hefuyi on 2017/1/17.
 */

public class MetaChangedEvent {

    private long songId;
    private String songName;
    private String artistName;

    public MetaChangedEvent(long songId, String songName, String artistName) {
        this.songId = songId;
        this.songName = songName;
        this.artistName = artistName;
    }

    public long getSongId() {
        return songId;
    }

    public String getSongName() {
        return songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof MetaChangedEvent && ((MetaChangedEvent) obj).songId == songId;
    }
}
