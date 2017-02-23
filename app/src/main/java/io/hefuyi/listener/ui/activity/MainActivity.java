package io.hefuyi.listener.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.bumptech.glide.Glide;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.hefuyi.listener.Constants;
import io.hefuyi.listener.MusicPlayer;
import io.hefuyi.listener.R;
import io.hefuyi.listener.RxBus;
import io.hefuyi.listener.event.MetaChangedEvent;
import io.hefuyi.listener.listener.PanelSlideListener;
import io.hefuyi.listener.permission.PermissionCallback;
import io.hefuyi.listener.permission.PermissionManager;
import io.hefuyi.listener.ui.fragment.AlbumDetailFragment;
import io.hefuyi.listener.ui.fragment.ArtistDetailFragment;
import io.hefuyi.listener.ui.fragment.FoldersFragment;
import io.hefuyi.listener.ui.fragment.MainFragment;
import io.hefuyi.listener.ui.fragment.PlayRankingFragment;
import io.hefuyi.listener.ui.fragment.PlaylistFragment;
import io.hefuyi.listener.ui.fragment.SearchFragment;
import io.hefuyi.listener.util.ATEUtil;
import io.hefuyi.listener.util.ListenerUtil;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivity implements ATEActivityThemeCustomizer {

    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout panelLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    private TextView songtitle;
    private TextView songartist;
    private ImageView albumart;
    private String action;
    private Map<String, Runnable> navigationMap = new HashMap<String, Runnable>();
    private Handler navDrawerRunnable = new Handler();
    private Runnable runnable;
    private PanelSlideListener mPanelSlideListener;
    private boolean listenerSeted = false;
    private boolean isDarkTheme;

    private final PermissionCallback permissionReadstorageCallback = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            loadEverything();
        }

        @Override
        public void permissionRefused() {
            finish();
        }
    };
    private Runnable navigateLibrary = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_library).setChecked(true);
            Fragment fragment = MainFragment.newInstance(Constants.NAVIGATE_ALLSONG);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();

        }
    };
    private Runnable navigatePlaylist = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_playlists).setChecked(true);
            Fragment fragment = new PlaylistFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };
    private Runnable navigateFavourate = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_favourate).setChecked(true);
            Fragment fragment = MainFragment.newInstance(Constants.NAVIGATE_PLAYLIST_FAVOURATE);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commit();
        }
    };
    private Runnable navigateFolders = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_folders).setChecked(true);
            Fragment fragment = new FoldersFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };
    private Runnable navigateRecentPlay = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_recent_play).setChecked(true);
            Fragment fragment = MainFragment.newInstance(Constants.NAVIGATE_PLAYLIST_RECENTPLAY);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commit();
        }
    };
    private Runnable navigateRecentAdd = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_recent_add).setChecked(true);
            Fragment fragment = MainFragment.newInstance(Constants.NAVIGATE_PLAYLIST_RECENTADD);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commit();
        }
    };
    private Runnable navigatePlayRanking = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_play_ranking).setChecked(true);
            Fragment fragment = new PlayRankingFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commit();
        }
    };

    private Runnable navigateSearch = new Runnable() {
        public void run() {
            Fragment fragment = new SearchFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.add(R.id.fragment_container, fragment);
            transaction.addToBackStack(null).commit();
        }
    };

    private Runnable navigateSetting = new Runnable() {
        public void run() {
            final Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            MainActivity.this.startActivity(intent);
        }
    };

    private Runnable navigateAlbum = new Runnable() {
        public void run() {
            long albumID = getIntent().getExtras().getLong(Constants.ALBUM_ID);
            String albumName = getIntent().getExtras().getString(Constants.ALBUM_NAME);
            Fragment fragment = AlbumDetailFragment.newInstance(albumID, albumName, false, null);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment).commit();
        }
    };
    private Runnable navigateArtist = new Runnable() {
        public void run() {
            long artistID = getIntent().getExtras().getLong(Constants.ARTIST_ID);
            String artistName = getIntent().getExtras().getString(Constants.ARTIST_NAME);
            Fragment fragment = ArtistDetailFragment.newInstance(artistID, artistName, false, null);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment).commit();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        action = getIntent().getAction();

        isDarkTheme = ATEUtil.getATEKey(this).equals("dark_theme");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        navigationMap.put(Constants.NAVIGATE_LIBRARY, navigateLibrary);
        navigationMap.put(Constants.NAVIGATE_ALBUM, navigateAlbum);
        navigationMap.put(Constants.NAVIGATE_ARTIST, navigateArtist);

        View header = navigationView.inflateHeaderView(R.layout.nav_header);
        albumart = (ImageView) header.findViewById(R.id.album_art);
        songtitle = (TextView) header.findViewById(R.id.song_title);
        songartist = (TextView) header.findViewById(R.id.song_artist);

        navDrawerRunnable.postDelayed(new Runnable() {
            @Override
            public void run() {
                setupDrawerContent(navigationView);
                setupNavigationIcons(navigationView);
            }
        }, 700);


        if (ListenerUtil.isMarshmallow()) {
            checkPermissionAndThenLoad();
        } else {
            loadEverything();
        }

        addBackstackListener();

        if (Intent.ACTION_VIEW.equals(action)) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.clearQueue();
                    MusicPlayer.openFile(getIntent().getData().getPath());
                    MusicPlayer.playOrPause();
                }
            }, 350);
        }
        subscribeMetaChangedEvent();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unSubscribe(this);
        if (mPanelSlideListener != null) {
            RxBus.getInstance().unSubscribe(mPanelSlideListener);
        }
    }

    private void loadEverything() {
        Runnable navigation = navigationMap.get(action);
        if (navigation != null) {
            navigation.run();
        } else {
            navigateLibrary.run();
        }

        new initQuickControls().execute("");
    }

    private void checkPermissionAndThenLoad() {
        //check for permission
        if (PermissionManager.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            loadEverything();
        } else {
            if (PermissionManager.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(panelLayout, "Listener will need to read external storage to display songs on your device.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                PermissionManager.askForPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, permissionReadstorageCallback);
                            }
                        }).show();
            } else {
                PermissionManager.askForPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, permissionReadstorageCallback);
            }
        }
    }

    /**
     * 监听menu点击
     * @param navigationView
     */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(final MenuItem menuItem) {
                        updatePosition(menuItem);
                        return true;

                    }
                });
    }

    /**
     * 设置图标
     * @param navigationView
     */
    private void setupNavigationIcons(NavigationView navigationView) {

        //material-icon-lib currently doesn't work with navigationview of design support library 22.2.0+
        //set icons manually for now
        //https://github.com/code-mc/material-icon-lib/issues/15

        if (!isDarkTheme) {
            navigationView.getMenu().findItem(R.id.nav_library).setIcon(R.drawable.ic_music_note_black_48dp);
            navigationView.getMenu().findItem(R.id.nav_playlists).setIcon(R.drawable.ic_queue_music_black_48dp);
            navigationView.getMenu().findItem(R.id.nav_folders).setIcon(R.drawable.ic_folder_black_48dp);
            navigationView.getMenu().findItem(R.id.nav_favourate).setIcon(R.drawable.ic_favorite_black_48dp);
            navigationView.getMenu().findItem(R.id.nav_recent_play).setIcon(R.drawable.ic_watch_later_black_48dp);
            navigationView.getMenu().findItem(R.id.nav_recent_add).setIcon(R.drawable.ic_add_box_black_48dp);
            navigationView.getMenu().findItem(R.id.nav_play_ranking).setIcon(R.drawable.ic_sort_black_48dp);
            navigationView.getMenu().findItem(R.id.nav_settings).setIcon(R.drawable.ic_settings_black_48dp);
            navigationView.getMenu().findItem(R.id.nav_exit).setIcon(R.drawable.ic_exit_to_app_black_48dp);
        } else {
            navigationView.getMenu().findItem(R.id.nav_library).setIcon(R.drawable.ic_music_note_white_48dp);
            navigationView.getMenu().findItem(R.id.nav_playlists).setIcon(R.drawable.ic_queue_music_white_48dp);
            navigationView.getMenu().findItem(R.id.nav_folders).setIcon(R.drawable.ic_folder_white_48dp);
            navigationView.getMenu().findItem(R.id.nav_favourate).setIcon(R.drawable.ic_favorite_white_48dp);
            navigationView.getMenu().findItem(R.id.nav_recent_play).setIcon(R.drawable.ic_watch_later_white_48dp);
            navigationView.getMenu().findItem(R.id.nav_recent_add).setIcon(R.drawable.ic_add_box_white_48dp);
            navigationView.getMenu().findItem(R.id.nav_play_ranking).setIcon(R.drawable.ic_sort_white_48dp);
            navigationView.getMenu().findItem(R.id.nav_settings).setIcon(R.drawable.ic_settings_white_48dp);
            navigationView.getMenu().findItem(R.id.nav_exit).setIcon(R.drawable.ic_exit_to_app_white_48dp);
        }

    }

    /**
     * 导航
     * @param menuItem
     */
    private void updatePosition(final MenuItem menuItem) {
        runnable = null;

        switch (menuItem.getItemId()) {
            case R.id.nav_library:
                runnable = navigateLibrary;
                break;
            case R.id.nav_playlists:
                runnable = navigatePlaylist;
                break;
            case R.id.nav_folders:
                runnable = navigateFolders;
                break;
            case R.id.nav_favourate:
                runnable = navigateFavourate;
                break;
            case R.id.nav_recent_play:
                runnable = navigateRecentPlay;
                break;
            case R.id.nav_recent_add:
                runnable = navigateRecentAdd;
                break;
            case R.id.nav_play_ranking:
                runnable = navigatePlayRanking;
                break;
            case R.id.nav_settings:
                runnable = navigateSetting;
                break;
            case R.id.nav_exit:
                this.finish();
                break;
        }

        if (runnable != null) {
            mDrawerLayout.closeDrawers();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            }, 350);
        }
    }

    /**
     * 设置导航栏header部分信息
     */
    private void setDetailsToHeader() {
        String name = MusicPlayer.getTrackName();
        String artist = MusicPlayer.getArtistName();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(artist)) {
            songtitle.setText(R.string.app_name);
            songartist.setText("");
            Drawable defaultHeader = getResources().getDrawable(R.drawable.icon_drawer_theme_bg);
            defaultHeader.setColorFilter(ATEUtil.getThemePrimaryColor(this), PorterDuff.Mode.DARKEN);
            albumart.setImageDrawable(defaultHeader);
            return;
        }

        songtitle.setText(name);
        songartist.setText(artist);

        Drawable defaultHeader = getResources().getDrawable(R.drawable.icon_drawer_theme_bg);
        defaultHeader.setColorFilter(ATEUtil.getThemePrimaryColor(this), PorterDuff.Mode.DARKEN);

        Glide.with(this).load(ListenerUtil.getAlbumArtUri(MusicPlayer.getCurrentAlbumId()).toString())
                .error(defaultHeader)
                .centerCrop()
                .into(albumart);
    }

    private boolean isNavigatingMain() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        return (currentFragment instanceof MainFragment || currentFragment instanceof PlaylistFragment
                || currentFragment instanceof PlayRankingFragment|| currentFragment instanceof  FoldersFragment);
    }


    private void addBackstackListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                getSupportFragmentManager().findFragmentById(R.id.fragment_container).onResume();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (panelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
                    panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }else if(isNavigatingMain()) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else super.onBackPressed();
                return true;
            }
            case R.id.action_search:
                navigateSearch.run();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (panelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {

            super.onBackPressed();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!listenerSeted && panelLayout.findViewById(R.id.topContainer) != null) {
            mPanelSlideListener = new PanelSlideListener(panelLayout);
            panelLayout.addPanelSlideListener(mPanelSlideListener);
            listenerSeted = true;
        }
    }

    private void subscribeMetaChangedEvent() {
        Subscription subscription = RxBus.getInstance()
                .toObservable(MetaChangedEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .subscribe(new Action1<MetaChangedEvent>() {
                    @Override
                    public void call(MetaChangedEvent event) {
                        setDetailsToHeader();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public int getActivityTheme() {
        return isDarkTheme ? R.style.AppThemeDark : R.style.AppThemeLight;
    }

}
