package io.hefuyi.listener.util;

import android.provider.MediaStore;

/**
 * Holds all of the sort orders for each list type.
 */

public class SortOrder {

    /**
     * This class is never instantiated
     */
    public SortOrder() {
    }

    /**
     * Artist sort order entries.
     */
    public interface ArtistSortOrder {
        /* Artist sort order A-Z */
        String ARTIST_A_Z = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER;

        /* Artist sort order Z-A */
        String ARTIST_Z_A = ARTIST_A_Z + " DESC";

        /* Artist sort order number of songs */
        String ARTIST_NUMBER_OF_SONGS = MediaStore.Audio.Artists.NUMBER_OF_TRACKS
                + " DESC";

        /* Artist sort order number of albums */
        String ARTIST_NUMBER_OF_ALBUMS = MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
                + " DESC";
    }

    /**
     * Album sort order entries.
     */
    public interface AlbumSortOrder {
        /* Album sort order A-Z */
        String ALBUM_A_Z = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;

        /* Album sort order Z-A */
        String ALBUM_Z_A = ALBUM_A_Z + " DESC";

        /* Album sort order songs */
        String ALBUM_NUMBER_OF_SONGS = MediaStore.Audio.Albums.NUMBER_OF_SONGS
                + " DESC";

    }

    /**
     * Song sort order entries.
     */
    public interface SongSortOrder {
        /* Song sort order A-Z */
        String SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        /* Song sort order Z-A */
        String SONG_Z_A = SONG_A_Z + " DESC";

        /* Song sort order artist */
        String SONG_ARTIST = MediaStore.Audio.Media.ARTIST;

        /* Song sort order album */
        String SONG_ALBUM = MediaStore.Audio.Media.ALBUM;

        /* Song sort order duration */
        String SONG_DURATION = MediaStore.Audio.Media.DURATION + " DESC";

        /* Song sort order date */
        String SONG_DATE = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        /* Song sort order filename */
        String SONG_FILENAME = MediaStore.Audio.Media.DATA;
    }

    /**
     * Album song sort order entries.
     */
    public interface AlbumSongSortOrder {
        /* Album song sort order A-Z */
        String SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        /* Album song sort order Z-A */
        String SONG_Z_A = SONG_A_Z + " DESC";

        /* Album song sort order track list */
        String SONG_TRACK_LIST = MediaStore.Audio.Media.TRACK + ", "
                + MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        /* Album song sort order duration */
        String SONG_DURATION = SongSortOrder.SONG_DURATION;

        /* Album Song sort order year */
        String SONG_YEAR = MediaStore.Audio.Media.YEAR + " DESC";

    }

    /**
     * Artist song sort order entries.
     */
    public interface ArtistSongSortOrder {
        /* Artist song sort order A-Z */
        String SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

    }

}
