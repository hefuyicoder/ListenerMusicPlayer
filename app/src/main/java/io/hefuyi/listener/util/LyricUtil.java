package io.hefuyi.listener.util;

import android.util.Base64;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import rx.Observable;

/**
 * Created by hefuyi on 2016/11/8.
 */

public class LyricUtil {

    private static final String lrcRootPath = android.os.Environment
            .getExternalStorageDirectory().toString()
            + "/Listener/lyric/";

    public static File writeLrcToLoc(String title, String artist, String lrcContext) {
        FileWriter writer = null;
        try {
            File file = new File(getLrcPath(title, artist));
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            writer = new FileWriter(getLrcPath(title, artist));
            writer.write(lrcContext);
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isLrcFileExist(String title, String artist) {
        File file = new File(getLrcPath(title, artist));
        return file.exists();
    }

    public static Observable<File> getLocalLyricFile(String title, String artist) {
        File file = new File(getLrcPath(title, artist));
        if (file.exists()) {
            return Observable.just(file);
        }else {
            return Observable.error(new Throwable("lyric file not exist"));
        }
    }

    private static String getLrcPath(String title, String artist) {
        return lrcRootPath + title + " - " + artist + ".lrc";
    }

    public static  String decryptBASE64(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        try {
            byte[] encode = str.getBytes("UTF-8");
            // base64 解密
            return new String(Base64.decode(encode, 0, encode.length, Base64.DEFAULT), "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
