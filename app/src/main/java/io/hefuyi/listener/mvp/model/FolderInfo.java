package io.hefuyi.listener.mvp.model;

/**
 * Created by hefuyi on 2016/12/11.
 */

public class FolderInfo {

    public final String folderName;
    public final String folderPath;
    public final int songCount;

    public FolderInfo() {
        this.folderName = "";
        this.folderPath = "";
        this.songCount = -1;
    }

    public FolderInfo(String _folderName, String _folderPath, int _songCount) {
        this.folderName = _folderName;
        this.folderPath = _folderPath;
        this.songCount = _songCount;
    }
}
