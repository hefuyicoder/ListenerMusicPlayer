package io.hefuyi.listener.ui.fragment;


import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.io.File;
import java.security.InvalidParameterException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.hefuyi.listener.ListenerApp;
import io.hefuyi.listener.MusicPlayer;
import io.hefuyi.listener.R;
import io.hefuyi.listener.RxBus;
import io.hefuyi.listener.event.FavourateSongEvent;
import io.hefuyi.listener.event.MetaChangedEvent;
import io.hefuyi.listener.injector.component.ApplicationComponent;
import io.hefuyi.listener.injector.component.DaggerQuickControlsComponent;
import io.hefuyi.listener.injector.component.QuickControlsComponent;
import io.hefuyi.listener.injector.module.ActivityModule;
import io.hefuyi.listener.injector.module.QuickControlsModule;
import io.hefuyi.listener.listener.PaletteColorChangeListener;
import io.hefuyi.listener.mvp.contract.QuickControlsContract;
import io.hefuyi.listener.provider.FavoriteSong;
import io.hefuyi.listener.ui.dialogs.PlayqueueDialog;
import io.hefuyi.listener.util.ATEUtil;
import io.hefuyi.listener.util.ColorUtil;
import io.hefuyi.listener.util.DensityUtil;
import io.hefuyi.listener.util.ListenerUtil;
import io.hefuyi.listener.util.NavigationUtil;
import io.hefuyi.listener.util.ScrimUtil;
import io.hefuyi.listener.widget.ForegroundImageView;
import io.hefuyi.listener.widget.LyricView;
import io.hefuyi.listener.widget.PlayPauseView;
import io.hefuyi.listener.widget.timely.TimelyView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 */
public class QuickControlsFragment extends Fragment implements QuickControlsContract.View {

    @Inject
    QuickControlsContract.Presenter mPresenter;
    @BindView(R.id.topContainer)
    public View topContainer;
    @BindView(R.id.song_progress_normal)
    ProgressBar mProgress;
    @BindView(R.id.play_pause)
    PlayPauseView mPlayPauseView;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.artist)
    TextView mArtist;
    @BindView(R.id.album_art)
    ForegroundImageView mAlbumArt;
    @BindView(R.id.previous)
    MaterialIconView previous;
    @BindView(R.id.next)
    MaterialIconView next;
    @BindView(R.id.heart)
    MaterialIconView favorite;
    @BindView(R.id.ic_play_queue)
    MaterialIconView iconPlayQueue;
    @BindView(R.id.lyric_view)
    LyricView mLyricView;
    @BindView(R.id.popup_menu)
    ImageView popupMenu;
    @BindView(R.id.seek_song_touch)
    SeekBar mSeekBar;
    @BindView(R.id.timelyView11)
    TimelyView timelyView11;
    @BindView(R.id.timelyView12)
    TimelyView timelyView12;
    @BindView(R.id.timelyView13)
    TimelyView timelyView13;
    @BindView(R.id.timelyView14)
    TimelyView timelyView14;
    @BindView(R.id.timelyView15)
    TimelyView timelyView15;
    @BindView(R.id.hour_colon)
    TextView hourColon;
    @BindView(R.id.minute_colon)
    TextView minuteColon;
    @BindView(R.id.song_elapsedtime)
    LinearLayout songElapsedTime;

    private int blackWhiteColor;
    private Handler mElapsedTimeHandler;
    private int[] timeArr = new int[]{0, 0, 0, 0, 0};
    private static PaletteColorChangeListener sListener;
    private PlayqueueDialog bottomDialogFragment;
    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    private Palette.Swatch mSwatch;
    private boolean mIsFavorite = false;


    private Runnable mUpdateProgress = new Runnable() {

        @Override
        public void run() {

            long position = MusicPlayer.position();
            mProgress.setProgress((int) position);
            mSeekBar.setProgress((int) position);
            mLyricView.setCurrentTimeMillis(position);
            if (MusicPlayer.isPlaying()) {
                mProgress.postDelayed(mUpdateProgress, 50);
            } else mProgress.removeCallbacks(this);

        }
    };

    private Runnable mUpdateElapsedTime = new Runnable() {
        @Override
        public void run() {
            if (getActivity() != null) {
                String time = ListenerUtil.makeShortTimeString(getActivity(), mSeekBar.getProgress() / 1000);
                if (time.length() < 5) {
                    timelyView11.setVisibility(View.GONE);
                    timelyView12.setVisibility(View.GONE);
                    hourColon.setVisibility(View.GONE);
                    tv13(time.charAt(0) - '0');
                    tv14(time.charAt(2) - '0');
                    tv15(time.charAt(3) - '0');
                } else if (time.length() == 5) {
                    timelyView12.setVisibility(View.VISIBLE);
                    tv12(time.charAt(0) - '0');
                    tv13(time.charAt(1) - '0');
                    tv14(time.charAt(3) - '0');
                    tv15(time.charAt(4) - '0');
                } else {
                    timelyView11.setVisibility(View.VISIBLE);
                    hourColon.setVisibility(View.VISIBLE);
                    tv11(time.charAt(0) - '0');
                    tv12(time.charAt(2) - '0');
                    tv13(time.charAt(3) - '0');
                    tv14(time.charAt(5) - '0');
                    tv15(time.charAt(6) - '0');
                }
                mElapsedTimeHandler.postDelayed(this, 600);
            }

        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectDependences();
        mPresenter.attachView(this);
    }

    private void injectDependences() {
        ApplicationComponent applicationComponent = ((ListenerApp) getActivity().getApplication())
                .getApplicationComponent();
        QuickControlsComponent quickControlsComponent = DaggerQuickControlsComponent.builder()
                .applicationComponent(applicationComponent)
                .activityModule(new ActivityModule(getActivity()))
                .quickControlsModule(new QuickControlsModule())
                .build();
        quickControlsComponent.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playback_controls, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ATE.apply(this, ATEUtil.getATEKey(getActivity()));

        mSlidingUpPanelLayout = (SlidingUpPanelLayout) view.getParent().getParent();

        setUpPopupMenu(popupMenu);

        mLyricView.setLineSpace(15.0f);
        mLyricView.setTextSize(17.0f);
        mLyricView.setPlayable(false);
        mLyricView.setTranslationY(DensityUtil.getScreenWidth(getActivity()) + DensityUtil.dip2px(getActivity(), 120));
        mLyricView.setOnPlayerClickListener(new LyricView.OnPlayerClickListener() {
            @Override
            public void onPlayerClicked(long progress, String content) {
                MusicPlayer.seek((long) progress);
                if (!MusicPlayer.isPlaying()) {
                    mPresenter.onPlayPauseClick();
                }
            }
        });

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mProgress.getLayoutParams();
        mProgress.measure(0, 0);
        layoutParams.setMargins(0, -(mProgress.getMeasuredHeight() / 2), 0, 0);
        mProgress.setLayoutParams(layoutParams);
        ScaleDrawable scaleDrawable = (ScaleDrawable) ((LayerDrawable) mProgress.getProgressDrawable()).findDrawableByLayerId(R.id.progress);
        GradientDrawable gradientDrawable = (GradientDrawable) scaleDrawable.getDrawable();
        int colorAccent = ATEUtil.getThemeAccentColor(getActivity());
        gradientDrawable.setColors(new int[]{colorAccent, colorAccent, colorAccent});

        //清除默认的左右边距
        mSeekBar.setPadding(0, DensityUtil.dip2px(getContext(), 36), 0, 0);
        mSeekBar.setSecondaryProgress(mSeekBar.getMax());

        songElapsedTime.setY((DensityUtil.getScreenWidth(getContext()) - songElapsedTime.getHeight()) / 2);

        setUpTimelyView();
        setSeekBarListener();

        if (mPlayPauseView != null) {
            if (MusicPlayer.isPlaying())
                mPlayPauseView.Play();
            else mPlayPauseView.Pause();
        }

        subscribeFavourateSongEvent();
        subscribeMetaChangedEvent();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.unsubscribe();
        sListener = null;
        RxBus.getInstance().unSubscribe(this);
    }

    @Override
    public void showLyric(File file) {
        if (file == null) {
            mLyricView.reset("暂无歌词");
        } else {
            mLyricView.setLyricFile(file, "UTF-8");
        }
    }

    @Override
    public void setPlayPauseButton(boolean isPlaying) {
        if (isPlaying) {
            mPlayPauseView.Play();
        } else {
            mPlayPauseView.Pause();
        }
    }

    private void setUpTimelyView() {
        if (timelyView11 != null) {
            String time = ListenerUtil.makeShortTimeString(getActivity(), MusicPlayer.position() / 1000);
            if (time.length() < 5) {
                timelyView11.setVisibility(View.GONE);
                timelyView12.setVisibility(View.GONE);
                hourColon.setVisibility(View.GONE);

                changeDigit(timelyView13, time.charAt(0) - '0');
                changeDigit(timelyView14, time.charAt(2) - '0');
                changeDigit(timelyView15, time.charAt(3) - '0');

            } else if (time.length() == 5) {
                timelyView12.setVisibility(View.VISIBLE);
                changeDigit(timelyView12, time.charAt(0) - '0');
                changeDigit(timelyView13, time.charAt(1) - '0');
                changeDigit(timelyView14, time.charAt(3) - '0');
                changeDigit(timelyView15, time.charAt(4) - '0');
            } else {
                timelyView11.setVisibility(View.VISIBLE);
                hourColon.setVisibility(View.VISIBLE);
                changeDigit(timelyView11, time.charAt(0) - '0');
                changeDigit(timelyView12, time.charAt(2) - '0');
                changeDigit(timelyView13, time.charAt(3) - '0');
                changeDigit(timelyView14, time.charAt(5) - '0');
                changeDigit(timelyView15, time.charAt(6) - '0');
            }
        }

        if (timelyView11 != null) {
            mElapsedTimeHandler = new Handler();
            mElapsedTimeHandler.postDelayed(mUpdateElapsedTime, 600);
        }

    }

    private void setUpPopupMenu(ImageView popupMenu) {
        popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu menu = new PopupMenu(getContext(), v);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.popup_song_goto_album:
                                if (mSlidingUpPanelLayout != null) {
                                    mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                    NavigationUtil.navigateToAlbum(getActivity(), MusicPlayer.getCurrentAlbumId(),
                                            MusicPlayer.getAlbumName(), null);
                                }
                                break;
                            case R.id.popup_song_goto_artist:
                                if (mSlidingUpPanelLayout != null) {
                                    mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                    NavigationUtil.navigateToAlbum(getActivity(), MusicPlayer.getCurrentArtistId(),
                                            MusicPlayer.getArtistName(), null);
                                }
                                break;
                            case R.id.popup_song_addto_playlist:
                                ListenerUtil.showAddPlaylistDialog(getActivity(), new long[]{MusicPlayer.getCurrentAudioId()});
                                break;
                            case R.id.popup_song_delete:
                                long[] deleteIds = {MusicPlayer.getCurrentAudioId()};
                                ListenerUtil.showDeleteDialog(getContext(), MusicPlayer.getTrackName(), deleteIds,
                                        new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                            }
                                        });
                                break;
                        }
                        return false;
                    }
                });
                menu.inflate(R.menu.menu_now_playing);
                menu.show();
            }
        });
    }

    private void setSeekBarListener() {
        if (mSeekBar != null)
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        if (songElapsedTime.getVisibility() == View.GONE) {
                            songElapsedTime.setVisibility(View.VISIBLE);
                        }
                        mProgress.removeCallbacks(mUpdateProgress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    songElapsedTime.setVisibility(View.GONE);
                    MusicPlayer.seek((long) seekBar.getProgress());
                    mProgress.postDelayed(mUpdateProgress, 10);
                }
            });
    }

    /**
     * 返回暂停播放按钮的状态
     *
     * @return true表示按钮为待暂定状态, false表示按钮为待播放状态
     */
    @Override
    public boolean getPlayPauseStatus() {
        return mPlayPauseView.isPlay();
    }

    @Override
    public void startUpdateProgress() {
        mProgress.postDelayed(mUpdateProgress, 10);
    }

    @Override
    public void setProgressMax(int max) {
        mProgress.setMax(max);
        mSeekBar.setMax(max);
    }

    @Override
    public void setAlbumArt(Bitmap albumArt) {
        mAlbumArt.setImageBitmap(albumArt);
    }

    @Override
    public void setAlbumArt(Drawable albumArt) {
        mAlbumArt.setImageDrawable(albumArt);
        if (TextUtils.isEmpty(MusicPlayer.getTrackName()) && TextUtils.isEmpty(MusicPlayer.getArtistName())) {
            mAlbumArt.setForeground(null);
            TypedValue paletteColor = new TypedValue();
            getContext().getTheme().resolveAttribute(R.attr.album_default_palette_color, paletteColor, true);
            topContainer.setBackgroundColor(paletteColor.data);
            mPlayPauseView.setDrawableColor(ATEUtil.getThemeAccentColor(getActivity()));
            mPlayPauseView.setEnabled(false);
            next.setEnabled(false);
            next.setColor(ATEUtil.getThemeAccentColor(getContext()));
            if (sListener != null) {
                sListener.onPaletteColorChange(paletteColor.data, ATEUtil.getThemeAccentColor(getActivity()));
            }
        }
    }

    @Override
    public void setTitle(String title) {
        mTitle.setText(title);
    }

    @Override
    public void setArtist(String artist) {
        mArtist.setText(artist);
    }

    @Override
    public void setPalette(Palette palette) {
        mSwatch = ColorUtil.getMostPopulousSwatch(palette);
        int paletteColor;
        if (mSwatch != null) {
            paletteColor = mSwatch.getRgb();
            int artistColor = mSwatch.getTitleTextColor();
            mTitle.setTextColor(ColorUtil.getOpaqueColor(artistColor));
            mArtist.setTextColor(artistColor);
        } else {
            mSwatch = palette.getMutedSwatch() == null ? palette.getVibrantSwatch() : palette.getMutedSwatch();
            if (mSwatch != null) {
                paletteColor = mSwatch.getRgb();
                int artistColor = mSwatch.getTitleTextColor();
                mTitle.setTextColor(ColorUtil.getOpaqueColor(artistColor));
                mArtist.setTextColor(artistColor);
            } else {
                paletteColor = ATEUtil.getThemeAlbumDefaultPaletteColor(getContext());
                mTitle.setTextColor(getResources().getColor(android.R.color.primary_text_light));
                mArtist.setTextColor(getResources().getColor(android.R.color.secondary_text_light));
            }

        }
        //set icon color
        blackWhiteColor = ColorUtil.getBlackWhiteColor(paletteColor);
        topContainer.setBackgroundColor(paletteColor);
        if (bottomDialogFragment != null && mSwatch != null) {
            bottomDialogFragment.setPaletteSwatch(mSwatch);
        }
        mLyricView.setHighLightTextColor(blackWhiteColor);
        mLyricView.setDefaultColor(blackWhiteColor);
        mLyricView.setTouchable(false);
        mLyricView.setHintColor(blackWhiteColor);
        mPlayPauseView.setDrawableColor(blackWhiteColor);
        mPlayPauseView.setCircleColor(blackWhiteColor);
        mPlayPauseView.setCircleAlpah(0);
        mPlayPauseView.setEnabled(true);
        next.setEnabled(true);
        next.setColor(blackWhiteColor);
        previous.setColor(blackWhiteColor);
        next.setColor(blackWhiteColor);
        iconPlayQueue.setColor(blackWhiteColor);

        //set timely color
        setTimelyColor(blackWhiteColor);

        //set seekbar progressdrawable
        ScaleDrawable scaleDrawable = (ScaleDrawable) ((LayerDrawable) mSeekBar.getProgressDrawable()).findDrawableByLayerId(R.id.progress);
        GradientDrawable gradientDrawable = (GradientDrawable) scaleDrawable.getDrawable();
        gradientDrawable.setColors(new int[]{blackWhiteColor, blackWhiteColor, blackWhiteColor});

        mIsFavorite = FavoriteSong.getInstance(getContext()).isFavorite(MusicPlayer.getCurrentAudioId());
        if (mIsFavorite) {
            favorite.setColor(Color.parseColor("#E97767"));
        } else {
            favorite.setColor(blackWhiteColor);
        }
        //set albumart foreground
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            mAlbumArt.setForeground(
                    ScrimUtil.makeCubicGradientScrimDrawable(
                            paletteColor, //颜色
                            8, //渐变层数
                            Gravity.CENTER_HORIZONTAL)); //起始方向

        }

        if (sListener != null) {
            sListener.onPaletteColorChange(paletteColor, blackWhiteColor);
        }

    }

    @OnClick(R.id.upIndicator)
    public void onUpIndicatorClick() {
        if (mSlidingUpPanelLayout != null) {
            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    @OnClick(R.id.play_pause)
    public void onPlayPauseClick() {
        mPresenter.onPlayPauseClick();
    }

    @OnClick(R.id.next)
    public void onNextClick() {
        mPresenter.onNextClick();
    }

    @OnClick(R.id.previous)
    public void onPreviousClick() {
        mPresenter.onPreviousClick();
    }

    @OnClick(R.id.ic_play_queue)
    public void onPlayQueueClick() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if (bottomDialogFragment == null) {
            bottomDialogFragment = new PlayqueueDialog();
        }
        bottomDialogFragment.show(fm, "fragment_bottom_dialog");
        if (mSwatch != null) {
            bottomDialogFragment.setPaletteSwatch(mSwatch);

        }
    }

    @OnClick(R.id.heart)
    public void onFavoriteClick() {
        if (mIsFavorite) {
            int num = FavoriteSong.getInstance(getContext()).removeFavoriteSong(new long[]{MusicPlayer.getCurrentAudioId()});
            if (num == 1) {
                favorite.setColor(blackWhiteColor);
                mIsFavorite = false;
                RxBus.getInstance().post(new FavourateSongEvent());
                Toast.makeText(getContext(), R.string.remove_favorite_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.remove_favorite_fail, Toast.LENGTH_SHORT).show();
            }
        } else {
            int num = FavoriteSong.getInstance(getContext()).addFavoriteSong(new long[]{MusicPlayer.getCurrentAudioId()});
            if (num == 1) {
                favorite.setColor(Color.parseColor("#E97767"));
                mIsFavorite = true;
                RxBus.getInstance().post(new FavourateSongEvent());
                Toast.makeText(getContext(), R.string.add_favorite_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.add_favorite_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void subscribeFavourateSongEvent() {
        Subscription subscription = RxBus.getInstance()
                .toObservable(FavourateSongEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FavourateSongEvent>() {
                    @Override
                    public void call(FavourateSongEvent event) {
                        mIsFavorite = FavoriteSong.getInstance(getContext()).isFavorite(MusicPlayer.getCurrentAudioId());
                        if (mIsFavorite) {
                            favorite.setColor(Color.parseColor("#E97767"));
                        } else {
                            favorite.setColor(blackWhiteColor);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }

    private void subscribeMetaChangedEvent() {
        Subscription subscription = RxBus.getInstance()
                .toObservable(MetaChangedEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MetaChangedEvent>() {
                    @Override
                    public void call(MetaChangedEvent event) {
                        mPresenter.updateNowPlayingCard();
                        mPresenter.loadLyric();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }

    public static void setPaletteColorChangeListener(PaletteColorChangeListener paletteColorChangeListener) {
        sListener = paletteColorChangeListener;
    }

    private void changeDigit(TimelyView tv, int end) {
        ObjectAnimator obja = tv.animate(end);
        obja.setDuration(400);
        obja.start();
    }

    private void setTimelyColor(@ColorInt int color) {
        hourColon.setTextColor(color);
        minuteColon.setTextColor(color);
        timelyView11.setTextColor(color);
        timelyView12.setTextColor(color);
        timelyView13.setTextColor(color);
        timelyView14.setTextColor(color);
        timelyView15.setTextColor(color);
    }

    private void changeDigit(TimelyView tv, int start, int end) {
        try {
            ObjectAnimator obja = tv.animate(start, end);
            obja.setDuration(400);
            obja.start();
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        }
    }

    private void tv11(int a) {
        if (a != timeArr[0]) {
            changeDigit(timelyView11, timeArr[0], a);
            timeArr[0] = a;
        }
    }

    private void tv12(int a) {
        if (a != timeArr[1]) {
            changeDigit(timelyView12, timeArr[1], a);
            timeArr[1] = a;
        }
    }

    private void tv13(int a) {
        if (a != timeArr[2]) {
            changeDigit(timelyView13, timeArr[2], a);
            timeArr[2] = a;
        }
    }

    private void tv14(int a) {
        if (a != timeArr[3]) {
            changeDigit(timelyView14, timeArr[3], a);
            timeArr[3] = a;
        }
    }

    private void tv15(int a) {
        if (a != timeArr[4]) {
            changeDigit(timelyView15, timeArr[4], a);
            timeArr[4] = a;
        }
    }
}
