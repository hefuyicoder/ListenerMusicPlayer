package io.hefuyi.listener.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.List;

import io.hefuyi.listener.MusicPlayer;
import io.hefuyi.listener.R;
import io.hefuyi.listener.RxBus;
import io.hefuyi.listener.dataloader.PlaylistLoader;
import io.hefuyi.listener.event.FavourateSongEvent;
import io.hefuyi.listener.event.MediaUpdateEvent;
import io.hefuyi.listener.event.PlaylistUpdateEvent;
import io.hefuyi.listener.event.RecentlyPlayEvent;
import io.hefuyi.listener.mvp.model.Playlist;
import io.hefuyi.listener.provider.FavoriteSong;
import io.hefuyi.listener.provider.RecentStore;
import io.hefuyi.listener.provider.SongPlayCount;
import io.hefuyi.listener.ui.dialogs.CreatePlaylistDialog;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by hefuyi on 2016/11/4.
 */

public class ListenerUtil {

    public static final String MUSIC_ONLY_SELECTION = MediaStore.Audio.AudioColumns.IS_MUSIC + "=1"
            + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''";

    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static boolean isJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static Uri getAlbumArtUri(long paramInt) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), paramInt);
    }

    public static final String makeLabel(final Context context, final int pluralInt,
                                         final int number) {
        return context.getResources().getQuantityString(pluralInt, number, number);
    }

    public static final String makeShortTimeString(final Context context, long secs) {
        long hours, mins;

        hours = secs / 3600;
        secs %= 3600;
        mins = secs / 60;
        secs %= 60;

        final String durationFormat = context.getResources().getString(
                hours == 0 ? R.string.durationformatshort : R.string.durationformatlong);
        return String.format(durationFormat, hours, mins, secs);
    }

    public static boolean hasEffectsPanel(final Activity activity) {
        final PackageManager packageManager = activity.getPackageManager();
        return packageManager.resolveActivity(createEffectsIntent(),
                PackageManager.MATCH_DEFAULT_ONLY) != null;
    }

    public static Intent createEffectsIntent() {
        final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
        effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, MusicPlayer.getAudioSessionId());
        return effects;
    }

    public static void showDeleteDialog(final Context context, final String name, final long[] list,
                                        final MaterialDialog.SingleButtonCallback deleteCallback) {
        new MaterialDialog.Builder(context)
                .title(R.string.delete_song)
                .content(context.getString(R.string.delete) +" "+ name + " ?")
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ListenerUtil.deleteTracks(context, list);
                        deleteCallback.onClick(dialog, which);
                        RxBus.getInstance().post(new MediaUpdateEvent());
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static void showAddPlaylistDialog(final Context context, final long[] songIds) {
        PlaylistLoader.getPlaylists(context, true)
                .map(new Func1<List<Playlist>, Dialog>() {
                    @Override
                    public Dialog call(final List<Playlist> playlists) {
                        final CharSequence[] chars = new CharSequence[playlists.size() + 1];
                        chars[0] = context.getResources().getString(R.string.create_new_playlist);
                        for (int i = 0; i < playlists.size(); i++) {
                            chars[i + 1] = playlists.get(i).name;
                        }
                        return new MaterialDialog.Builder(context)
                                .title(R.string.add_to_playlist)
                                .items(chars)
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                        if (which == 0) {
                                            CreatePlaylistDialog.newInstance(songIds)
                                                    .show(((AppCompatActivity) context)
                                                            .getSupportFragmentManager(), context.getString(R.string.create_new_playlist));
                                            return;
                                        } else if (which == 1) {
                                            //我喜欢
                                            int num = FavoriteSong.getInstance(context).addFavoriteSong(songIds);
                                            Toast.makeText(context, R.string.add_favorite_success, Toast.LENGTH_SHORT).show();
                                            RxBus.getInstance().post(new FavourateSongEvent());
                                            dialog.dismiss();
                                            return;
                                        }

                                        MusicPlayer.addToPlaylist(context, songIds, playlists.get(which - 1).id);
                                        RxBus.getInstance().post(new PlaylistUpdateEvent());
                                        dialog.dismiss();

                                    }
                                }).build();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Dialog>() {
                    @Override
                    public void call(Dialog dialog) {
                        dialog.show();
                    }
                });
    }

    public static void showDeleteFromFavourate(final Context context, final long[] ids) {
        new MaterialDialog.Builder(context)
                .title(context.getResources().getString(R.string.delete_song_favourate) + "?")
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        FavoriteSong.getInstance(context).removeFavoriteSong(ids);
                        RxBus.getInstance().post(new FavourateSongEvent());
                        Toast.makeText(context, R.string.remove_favorite_success, Toast.LENGTH_SHORT).show();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static void showDeleteFromRecentlyPlay(final Context context, final long[] ids) {
        new MaterialDialog.Builder(context)
                .title(context.getResources().getString(R.string.delete_song_recentlyplay) + "?")
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        RecentStore.getInstance(context).removeItem(ids);
                        RxBus.getInstance().post(new RecentlyPlayEvent());
                        Toast.makeText(context, R.string.remove_recentlyplay_success, Toast.LENGTH_SHORT).show();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static void deleteTracks(final Context context, final long[] list) {
        final String[] projection = new String[]{
                BaseColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM_ID
        };
        final StringBuilder selection = new StringBuilder();
        selection.append(BaseColumns._ID + " IN (");
        for (int i = 0; i < list.length; i++) {
            selection.append(list[i]);
            if (i < list.length - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        final Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(),
                null, null);
        if (c != null) {
            // Step 1: Remove selected tracks from the current playlist, as well
            // as from the album art cache
            c.moveToFirst();
            while (!c.isAfterLast()) {
                // Remove from current playlist
                final long id = c.getLong(0);
                MusicPlayer.removeTrack(id);
                // Remove the track from the play count
                SongPlayCount.getInstance(context).removeItem(id);
                // Remove any items in the recents database
                RecentStore.getInstance(context).removeItem(new long[]{id});
                c.moveToNext();
            }

            // Step 2: Remove selected tracks from the database
            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    selection.toString(), null);

            // Step 3: Remove files from card
            c.moveToFirst();
            while (!c.isAfterLast()) {
                final String name = c.getString(1);
                final File f = new File(name);
                try { // File.delete can throw a security exception
                    if (!f.delete()) {
                        // I'm not sure if we'd ever get here (deletion would
                        // have to fail, but no exception thrown)
                        Log.e("MusicUtils", "Failed to delete file " + name);
                    }
                    c.moveToNext();
                } catch (final SecurityException ex) {
                    c.moveToNext();
                }
            }
            c.close();
        }

        final String message = makeLabel(context, R.plurals.NNNtracksdeleted, list.length);

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        MusicPlayer.refresh();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRtl(Resources res) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) &&
                (res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    public enum IdType {
        NA(0),
        Artist(1),
        Album(2),
        Playlist(3),
        Folder(4);

        public final int mId;

        IdType(final int id) {
            mId = id;
        }

        public static IdType getTypeById(int id) {
            for (IdType type : values()) {
                if (type.mId == id) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Unrecognized id: " + id);
        }
    }

    public enum PlaylistType {
        LastAdded(-1, R.string.playlist_last_added),
        RecentlyPlayed(-2, R.string.playlist_recently_played),
        Favourate(-3, R.string.playlist_top_tracks);

        public long mId;
        public int mTitleId;

        PlaylistType(long id, int titleId) {
            mId = id;
            mTitleId = titleId;
        }

        public static PlaylistType getTypeById(long id) {
            for (PlaylistType type : PlaylistType.values()) {
                if (type.mId == id) {
                    return type;
                }
            }

            return null;
        }
    }
}
