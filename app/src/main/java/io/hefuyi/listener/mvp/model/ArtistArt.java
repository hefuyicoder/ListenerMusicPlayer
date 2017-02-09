package io.hefuyi.listener.mvp.model;

/**
 * Created by hefuyi on 2016/11/30.
 */

public class ArtistArt {

    private String small;
    private String medium;
    private String large;
    private String extralarge;

    public ArtistArt(String small, String medium, String large, String extralarge) {
        this.small = small;
        this.medium = medium;
        this.large = large;
        this.extralarge = extralarge;
    }

    public String getSmall() {
        return small;
    }

    public String getMedium() {
        return medium;
    }

    public String getLarge() {
        return large;
    }

    public String getExtralarge() {
        return extralarge;
    }

    public void setSmall(String small) {
        this.small = small;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public void setLarge(String large) {
        this.large = large;
    }

    public void setExtralarge(String extralarge) {
        this.extralarge = extralarge;
    }

}
