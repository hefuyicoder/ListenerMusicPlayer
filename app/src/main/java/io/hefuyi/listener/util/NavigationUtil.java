package io.hefuyi.listener.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import io.hefuyi.listener.Constants;
import io.hefuyi.listener.R;
import io.hefuyi.listener.ui.activity.MainActivity;
import io.hefuyi.listener.ui.activity.SettingActivity;
import io.hefuyi.listener.ui.fragment.AlbumDetailFragment;
import io.hefuyi.listener.ui.fragment.ArtistDetailFragment;
import io.hefuyi.listener.ui.fragment.FolderSongsFragment;
import io.hefuyi.listener.ui.fragment.PlaylistDetailFragment;

import static io.hefuyi.listener.util.ListenerUtil.isLollipop;

/**
 * Created by hefuyi on 2016/11/6.
 */

public class NavigationUtil {

    @TargetApi(21)
    public static void navigateToAlbum(Activity context, long albumID, String albumName, Pair<View, String> transitionViews) {
        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        Fragment fragment;

        if (isLollipop() && transitionViews != null) {
            Transition changeImage = TransitionInflater.from(context).inflateTransition(R.transition.image_transform);
            transaction.addSharedElement(transitionViews.first, transitionViews.second);
            fragment = AlbumDetailFragment.newInstance(albumID, albumName, true, transitionViews.second);
            fragment.setSharedElementEnterTransition(changeImage);
            fragment.setSharedElementReturnTransition(changeImage);
        } else {
            transaction.setCustomAnimations(R.anim.activity_fade_in,
                    R.anim.activity_fade_out, R.anim.activity_fade_in, R.anim.activity_fade_out);
            fragment = AlbumDetailFragment.newInstance(albumID, albumName, false, null);
        }
        transaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_container));
        transaction.add(R.id.fragment_container, fragment);
        transaction.addToBackStack(null).commit();

    }

    @TargetApi(21)
    public static void navigateToArtist(Activity context, long artistID, String name, Pair<View, String> transitionViews) {
        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        Fragment fragment;

        if (ListenerUtil.isLollipop() && transitionViews != null) {
            Transition changeImage = TransitionInflater.from(context).inflateTransition(R.transition.image_transform);
            transaction.addSharedElement(transitionViews.first, transitionViews.second);
            fragment = ArtistDetailFragment.newInstance(artistID, name, true, transitionViews.second);
            fragment.setSharedElementEnterTransition(changeImage);
            fragment.setSharedElementReturnTransition(changeImage);
        } else {
            transaction.setCustomAnimations(R.anim.activity_fade_in,
                    R.anim.activity_fade_out, R.anim.activity_fade_in, R.anim.activity_fade_out);
            fragment = ArtistDetailFragment.newInstance(artistID, name, false, null);
        }
        transaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_container));
        transaction.add(R.id.fragment_container, fragment);
        transaction.addToBackStack(null).commit();
    }

    public static void goToArtist(Context context, long artistId, String artistName) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_ARTIST);
        intent.putExtra(Constants.ARTIST_ID, artistId);
        intent.putExtra(Constants.ARTIST_NAME, artistName);
        context.startActivity(intent);
    }

    public static void goToAlbum(Context context, long albumId, String albumName) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_ALBUM);
        intent.putExtra(Constants.ALBUM_ID, albumId);
        intent.putExtra(Constants.ALBUM_NAME, albumName);
        context.startActivity(intent);
    }

    public static Intent getNowPlayingIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Constants.NAVIGATE_LIBRARY);
        return intent;
    }

    public static void navigateToSettings(Activity context) {
        final Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }

    @TargetApi(21)
    public static void navigateToPlaylistDetail(Activity context, long playlistID, String playlistName, long firstAlbumID, Pair<View, String> transitionViews) {
        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        Fragment fragment;

        if (isLollipop() && transitionViews != null) {
            Transition changeImage = TransitionInflater.from(context).inflateTransition(R.transition.image_transform);
            transaction.addSharedElement(transitionViews.first, transitionViews.second);
            fragment = PlaylistDetailFragment.newInstance(playlistID, playlistName, firstAlbumID, true, transitionViews.second);
            fragment.setSharedElementEnterTransition(changeImage);
            fragment.setSharedElementReturnTransition(changeImage);
        } else {
            transaction.setCustomAnimations(R.anim.activity_fade_in,
                    R.anim.activity_fade_out, R.anim.activity_fade_in, R.anim.activity_fade_out);
            fragment = PlaylistDetailFragment.newInstance(playlistID, playlistName, firstAlbumID, false, transitionViews.second);
        }
        transaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_container));
        transaction.add(R.id.fragment_container, fragment);
        transaction.addToBackStack(null).commit();
    }

    public static void navigateToFolderSongs(Activity context, String path) {
        FragmentTransaction transaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
        Fragment fragment = FolderSongsFragment.newInstance(path);
        transaction.setCustomAnimations(R.anim.activity_fade_in,
                R.anim.activity_fade_out, R.anim.activity_fade_in, R.anim.activity_fade_out);
        transaction.hide(((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_container));
        transaction.add(R.id.fragment_container, fragment);
        transaction.addToBackStack(null).commit();
    }

    public static void navigateToEqualizer(Activity context) {
        try {
            // The google MusicFX apps need to be started using startActivityForResult
            context.startActivityForResult(ListenerUtil.createEffectsIntent(), 666);
        } catch (final ActivityNotFoundException notFound) {
            Toast.makeText(context, "Equalizer not found", Toast.LENGTH_SHORT).show();
        }
    }

}
