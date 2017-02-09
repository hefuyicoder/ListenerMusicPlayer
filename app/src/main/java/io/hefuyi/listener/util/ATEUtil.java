package io.hefuyi.listener.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import com.afollestad.appthemeengine.Config;

import io.hefuyi.listener.R;

/**
 * Created by hefuyi on 2017/1/23.
 */

public class ATEUtil {

    public static String getATEKey(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("dark_theme", false) ?
                "dark_theme" : "light_theme";
    }

    public static int getThemePrimaryColor(Context context) {
        return Config.primaryColor(context, getATEKey(context));
    }

    public static int getThemePrimaryColorDark(Context context) {
        return Config.primaryColorDark(context, getATEKey(context));
    }

    public static int getThemeAccentColor(Context context) {
        return Config.accentColor(context, getATEKey(context));
    }

    public static int getThemeTextColorPrimary(Context context) {
        TypedValue textColorPrimary = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, textColorPrimary, true);
        return context.getResources().getColor(textColorPrimary.resourceId);
    }

    public static int getThemeTextColorSecondly(Context context) {
        TypedValue textColorSecondly = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.textColorSecondary, textColorSecondly, true);
        return context.getResources().getColor(textColorSecondly.resourceId);
    }

    public static Drawable getDefaultAlbumDrawable(Context context) {
        TypedValue defaultAlbum = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.default_album_drawable, defaultAlbum, true);
        return context.getResources().getDrawable(defaultAlbum.resourceId);
    }

    public static Drawable getDefaultSingerDrawable(Context context) {
        TypedValue defaultSinger = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.default_singer_drawable, defaultSinger, true);
        return context.getResources().getDrawable(defaultSinger.resourceId);
    }

    public static int getThemeAlbumDefaultPaletteColor(Context context) {
        TypedValue paletteColor = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.album_default_palette_color, paletteColor, true);
        return context.getResources().getColor(paletteColor.resourceId);
    }

}
