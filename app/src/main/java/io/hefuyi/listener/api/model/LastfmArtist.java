package io.hefuyi.listener.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by hefuyi on 2016/11/3.
 */

public class LastfmArtist {

    private static final String NAME = "name";
    private static final String IMAGE = "image";
    private static final String SIMILAR = "similar";
    private static final String TAGS = "tags";
    private static final String BIO = "bio";

    @Expose
    @SerializedName(NAME)
    public String mName;

    @Expose
    @SerializedName(IMAGE)
    public List<Artwork> mArtwork;

    @Expose
    @SerializedName(SIMILAR)
    public SimilarArtist mSimilarArtist;

    @Expose
    @SerializedName(TAGS)
    public ArtistTag mArtistTags;

    @Expose
    @SerializedName(BIO)
    public ArtistBio mArtistBio;

    public class SimilarArtist {

        public static final String ARTIST = "artist";

        @Expose
        @SerializedName(ARTIST)
        public List<LastfmArtist> mSimilarArtist;
    }

    public class ArtistTag {

        public static final String TAG = "tag";

        @Expose
        @SerializedName(TAG)
        public List<ArtistTag> mTags;
    }
}
