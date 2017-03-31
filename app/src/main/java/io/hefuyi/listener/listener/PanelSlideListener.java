package io.hefuyi.listener.listener;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.steamcrafted.materialiconlib.MaterialIconView;

import io.hefuyi.listener.MusicPlayer;
import io.hefuyi.listener.R;
import io.hefuyi.listener.RxBus;
import io.hefuyi.listener.event.MetaChangedEvent;
import io.hefuyi.listener.ui.fragment.QuickControlsFragment;
import io.hefuyi.listener.util.ATEUtil;
import io.hefuyi.listener.util.DensityUtil;
import io.hefuyi.listener.util.ImageUtil;
import io.hefuyi.listener.util.ScrimUtil;
import io.hefuyi.listener.widget.ForegroundImageView;
import io.hefuyi.listener.widget.LyricView;
import io.hefuyi.listener.widget.PlayPauseView;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by hefuyi on 2016/11/7.
 */

public class PanelSlideListener implements SlidingUpPanelLayout.PanelSlideListener{

    private static final String TAG = PanelSlideListener.class.getSimpleName();

    private final SlidingUpPanelLayout mPanelLayout;
    private final Context mContext;
    private View nowPlayingCard;
    private RelativeLayout toolbar;
    private LinearLayout content;
    private ProgressBar progressBar;
    private TextView title;
    private TextView artist;
    private ForegroundImageView albumImage;
    private Drawable albumImageDrawable;
    private RelativeLayout iconContainer;
    private MaterialIconView heart;
    private MaterialIconView previous;
    private PlayPauseView playPause;
    private MaterialIconView next;
    private MaterialIconView playqueue;
    private LyricView lyricView;
    private SeekBar seekbar;

    private int screenWidth;
    private int screenHeight;

    private int titleEndTranslationX;
    private int artistEndTranslationX;
    private int artistNormalEndTranslationY;
    private int artistFullEndTranslationY;
    private int contentNormalEndTranslationY;
    private int contentFullEndTranslationY;

    private int lyricLineHeight;
    private int lyricFullHeight;

    private int lyricLineStartTranslationY;
    private int lyricLineEndTranslationY;
    private int lyricFullTranslationY;

    private int heartStartX;
    private int previousStartX;
    private int playPauseStartX;
    private int nextStartX;
    private int playqueueStartX;
    private int playPauseEndX;
    private int previousEndX;
    private int heartEndX;
    private int nextEndX;
    private int playqueueEndX;
    private int iconContainerStartY;
    private int iconContainerEndY;

    private IntEvaluator intEvaluator = new IntEvaluator();
    private FloatEvaluator floatEvaluator = new FloatEvaluator();
    private ArgbEvaluator colorEvaluator = new ArgbEvaluator();
    private int nowPlayingCardColor;
    private int playpauseDrawableColor;
    private Status mStatus = Status.COLLAPSED;

    public enum Status {
        EXPANDED,
        COLLAPSED,
        FULLSCREEN
    }

    public PanelSlideListener(SlidingUpPanelLayout slidingUpPanelLayout) {
        mPanelLayout = slidingUpPanelLayout;
        nowPlayingCard = mPanelLayout.findViewById(R.id.topContainer);
        toolbar = (RelativeLayout) nowPlayingCard.findViewById(R.id.custom_toolbar);
        mContext = mPanelLayout.getContext();
        albumImage = (ForegroundImageView) nowPlayingCard.findViewById(R.id.album_art);

        content = (LinearLayout) nowPlayingCard.findViewById(R.id.content);
        progressBar = (ProgressBar) nowPlayingCard.findViewById(R.id.song_progress_normal);
        title = (TextView) nowPlayingCard.findViewById(R.id.title);
        artist = (TextView) nowPlayingCard.findViewById(R.id.artist);
        screenWidth = DensityUtil.getScreenWidth(mContext);
        screenHeight = DensityUtil.getScreenHeight(mContext);

        iconContainer = (RelativeLayout) nowPlayingCard.findViewById(R.id.icon_container);
        heart = (MaterialIconView) nowPlayingCard.findViewById(R.id.heart);
        previous = (MaterialIconView) nowPlayingCard.findViewById(R.id.previous);
        playPause = (PlayPauseView) nowPlayingCard.findViewById(R.id.play_pause);
        next = (MaterialIconView) nowPlayingCard.findViewById(R.id.next);
        playqueue = (MaterialIconView) nowPlayingCard.findViewById(R.id.ic_play_queue);
        lyricView = (LyricView) nowPlayingCard.findViewById(R.id.lyric_view);
        playpauseDrawableColor = ATEUtil.getThemeAccentColor(mContext);

        seekbar = (SeekBar) mPanelLayout.findViewById(R.id.seek_song_touch);

        QuickControlsFragment.setPaletteColorChangeListener(new PaletteColorChangeListener() {
            @Override
            public void onPaletteColorChange(int paletteColor, int blackWhiteColor) {
                nowPlayingCardColor = paletteColor;
                playpauseDrawableColor = blackWhiteColor;

                if (mPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    playPause.setCircleAlpah(0);
                    playPause.setDrawableColor(playpauseDrawableColor);
                }else {
                    playPause.setCircleAlpah(255);
                    playPause.setDrawableColor(nowPlayingCardColor);
                }

                switch (mStatus) {
                    case FULLSCREEN:
                        albumImageDrawable = albumImage.getDrawable();
                        setBlurredAlbumArt();
                }
            }
        });

        if (MusicPlayer.getQueueSize() == 0) {
            mPanelLayout.setTouchEnabled(false);
        }

        caculateTitleAndArtist();
        caculateIcons();
        caculateLyricView();

        subscribeMetaChangedEvent();
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {

        CoordinatorLayout.LayoutParams frameLayout = (CoordinatorLayout.LayoutParams) albumImage.getLayoutParams();

        //animate albumImage
        int tempt = intEvaluator.evaluate(slideOffset, DensityUtil.dip2px(nowPlayingCard.getContext(), 55), screenWidth);
        frameLayout.width = tempt;
        frameLayout.height = tempt;
        albumImage.setLayoutParams(frameLayout);

        //animate title and artist
        title.setTranslationX(floatEvaluator.evaluate(slideOffset, 0, titleEndTranslationX));
        artist.setTranslationX(floatEvaluator.evaluate(slideOffset, 0, artistEndTranslationX));
        artist.setTranslationY(floatEvaluator.evaluate(slideOffset, 0, artistNormalEndTranslationY));
        content.setTranslationY(floatEvaluator.evaluate(slideOffset, 0, contentNormalEndTranslationY));

        //aniamte icons
        playPause.setX(intEvaluator.evaluate(slideOffset, playPauseStartX, playPauseEndX));
        playPause.setCircleAlpah(intEvaluator.evaluate(slideOffset, 0, 255));
        playPause.setDrawableColor((int) colorEvaluator.evaluate(slideOffset, playpauseDrawableColor, nowPlayingCardColor));
        previous.setX(intEvaluator.evaluate(slideOffset, previousStartX, previousEndX));
        heart.setX(intEvaluator.evaluate(slideOffset, heartStartX, heartEndX));
        next.setX(intEvaluator.evaluate(slideOffset, nextStartX, nextEndX));
        playqueue.setX(intEvaluator.evaluate(slideOffset, playqueueStartX, playqueueEndX));
        heart.setAlpha(floatEvaluator.evaluate(slideOffset, 0, 1));
        previous.setAlpha(floatEvaluator.evaluate(slideOffset, 0, 1));
        iconContainer.setY(intEvaluator.evaluate(slideOffset, iconContainerStartY, iconContainerEndY));

        //animate lyric view
        lyricView.setTranslationY(lyricLineStartTranslationY - (lyricLineStartTranslationY - lyricLineEndTranslationY) * slideOffset);
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        if (previousState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            progressBar.setVisibility(View.INVISIBLE);
            heart.setVisibility(View.VISIBLE);
            previous.setVisibility(View.VISIBLE);
        } else if (previousState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            if (mStatus == Status.FULLSCREEN) {
                animateToNormal();
            }
        }

        if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            mStatus = Status.EXPANDED;
            toolbarSlideIn();
            heart.setClickable(true);
            previous.setClickable(true);
            nowPlayingCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mStatus==Status.EXPANDED){
                        animateToFullscreen();
                    }else if (mStatus==Status.FULLSCREEN){
                        animateToNormal();
                    }else {
                        mPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                }
            });
        } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            mStatus = Status.COLLAPSED;
            progressBar.setVisibility(View.VISIBLE);
            heart.setVisibility(View.GONE);
            previous.setVisibility(View.GONE);
            nowPlayingCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPanelLayout.isTouchEnabled()) {
                        mPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    }
                }
            });
        } else if (newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
            toolbar.setVisibility(View.INVISIBLE);
        }
    }

    private void caculateTitleAndArtist() {
        Rect titleBounds = new Rect();
        title.getPaint().getTextBounds(title.getText().toString(), 0, title.getText().length(), titleBounds);
        int titleWidth = titleBounds.width();

        Rect artistBounds = new Rect();
        artist.getPaint().getTextBounds(artist.getText().toString(), 0, artist.getText().length(), artistBounds);
        int artistWidth = artistBounds.width();

        titleEndTranslationX = (screenWidth / 2) - (titleWidth / 2) - DensityUtil.dip2px(mContext, 67);

        artistEndTranslationX = (screenWidth / 2) - (artistWidth / 2) - DensityUtil.dip2px(mContext, 67);
        artistNormalEndTranslationY = DensityUtil.dip2px(mContext, 12);
        artistFullEndTranslationY = 0;

        contentNormalEndTranslationY = screenWidth + DensityUtil.dip2px(mContext, 32);
        contentFullEndTranslationY = DensityUtil.dip2px(mContext, 32);

        if (mStatus==Status.COLLAPSED) {
            title.setTranslationX(0);
            artist.setTranslationX(0);
        }else {
            title.setTranslationX(titleEndTranslationX);
            artist.setTranslationX(artistEndTranslationX);
        }
    }

    private void caculateIcons() {
        heartStartX = heart.getLeft();
        previousStartX = previous.getLeft();
        playPauseStartX = playPause.getLeft();
        nextStartX = next.getLeft();
        playqueueStartX = playqueue.getLeft();
        int size = DensityUtil.dip2px(mContext, 36);
        int gap = (screenWidth - 5 * (size)) / 6;
        playPauseEndX = (screenWidth / 2) - (size / 2);
        previousEndX = playPauseEndX - gap - size;
        heartEndX = previousEndX - gap - size;
        nextEndX = playPauseEndX + gap + size;
        playqueueEndX = nextEndX + gap + size;
        iconContainerStartY = iconContainer.getTop();
        iconContainerEndY = screenHeight - iconContainer.getHeight() - seekbar.getHeight();
    }

    private void caculateLyricView() {
        int lyricFullMarginTop = toolbar.getTop() + toolbar.getHeight() + DensityUtil.dip2px(mContext, 32);
        int lyricFullMarginBottom = iconContainer.getBottom() + iconContainer.getHeight()
                + DensityUtil.dip2px(mContext, 32);
        lyricLineHeight = DensityUtil.dip2px(mContext, 32);
        lyricFullHeight = screenHeight - lyricFullMarginTop - lyricFullMarginBottom;

        lyricLineStartTranslationY = screenHeight;
        int gapBetweenArtistAndLyric = iconContainerEndY - contentNormalEndTranslationY - content.getHeight();
        lyricLineEndTranslationY = iconContainerEndY - gapBetweenArtistAndLyric / 2 - lyricLineHeight / 2;
        lyricFullTranslationY = toolbar.getTop() + toolbar.getHeight() + DensityUtil.dip2px(mContext, 32);
    }


    private void setBlurredAlbumArt() {
        Observable.create(new Observable.OnSubscribe<Drawable>() {
            @Override
            public void call(Subscriber<? super Drawable> subscriber) {
                Bitmap bitmap = ((BitmapDrawable)albumImage.getDrawable()).getBitmap();
                Drawable drawable = ImageUtil.createBlurredImageFromBitmap(bitmap, mContext, 3);
                subscriber.onNext(drawable);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Drawable>() {
                    @Override
                    public void call(Drawable drawable) {
                        CoordinatorLayout.LayoutParams imageLayout = (CoordinatorLayout.LayoutParams) albumImage.getLayoutParams();
                        imageLayout.height = FrameLayout.LayoutParams.MATCH_PARENT;
                        imageLayout.width = FrameLayout.LayoutParams.MATCH_PARENT;
                        albumImage.setLayoutParams(imageLayout);
                        albumImage.setImageDrawable(drawable);
                        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            ColorDrawable colorDrawable = new ColorDrawable(nowPlayingCardColor);
                            colorDrawable.setAlpha(200);
                            albumImage.setForeground(colorDrawable);
                        }
                    }
                });
    }

    private void toolbarSlideIn() {
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.toolbar_slide_in);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        toolbar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                toolbar.startAnimation(animation);
            }
        });
    }

    private void animateToFullscreen(){
        //album art fullscreen
        albumImageDrawable = albumImage.getDrawable();
        setBlurredAlbumArt();

        //animate title and artist
        Animation contentAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                content.setTranslationY(contentNormalEndTranslationY - (contentNormalEndTranslationY - contentFullEndTranslationY) * interpolatedTime);
                artist.setTranslationY(artistNormalEndTranslationY - (artistNormalEndTranslationY - artistFullEndTranslationY) * interpolatedTime);
            }
        };
        contentAnimation.setDuration(150);
        artist.startAnimation(contentAnimation);

        //animate lyric
        Animation lyricAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                CoordinatorLayout.LayoutParams lyricLayout = (CoordinatorLayout.LayoutParams) lyricView.getLayoutParams();
                lyricLayout.height = (int) (lyricLineHeight + (lyricFullHeight - lyricLineHeight) * interpolatedTime);
                lyricView.setLayoutParams(lyricLayout);
                lyricView.setTranslationY(lyricLineEndTranslationY - (lyricLineEndTranslationY - lyricFullTranslationY) * interpolatedTime);
            }
        };
        lyricAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lyricView.setHighLightTextColor(ATEUtil.getThemeAccentColor(mContext));
                lyricView.setPlayable(true);
                lyricView.setTouchable(true);
                lyricView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        animateToNormal();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        lyricAnimation.setDuration(150);
        lyricView.startAnimation(lyricAnimation);

        mStatus = Status.FULLSCREEN;
    }

    private void animateToNormal() {
        //album art
        CoordinatorLayout.LayoutParams imageLayout = (CoordinatorLayout.LayoutParams) albumImage.getLayoutParams();
        imageLayout.height = screenWidth;
        imageLayout.width = screenWidth;
        albumImage.setImageDrawable(albumImageDrawable);
        albumImage.setLayoutParams(imageLayout);
        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            albumImage.setForeground(
                    ScrimUtil.makeCubicGradientScrimDrawable(
                            nowPlayingCardColor, //颜色
                            8, //渐变层数
                            Gravity.CENTER_HORIZONTAL)); //起始方向

        }

        //animate title and artist
        Animation contentAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                content.setTranslationY(contentFullEndTranslationY + (contentNormalEndTranslationY - contentFullEndTranslationY) * interpolatedTime);
                artist.setTranslationY(artistFullEndTranslationY + (artistNormalEndTranslationY - artistFullEndTranslationY) * interpolatedTime);
            }
        };
        contentAnimation.setDuration(300);
        artist.startAnimation(contentAnimation);

        //adjust lyricview
        Animation lyricAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) lyricView.getLayoutParams();
                layoutParams.height = (int) (lyricFullHeight - (lyricFullHeight - lyricLineHeight) * interpolatedTime);
                lyricView.setLayoutParams(layoutParams);
                lyricView.setTranslationY(lyricFullTranslationY + (lyricLineEndTranslationY - lyricFullTranslationY) * interpolatedTime);
            }
        };
        lyricAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lyricView.setPlayable(false);
                lyricView.setHighLightTextColor(lyricView.getDefaultColor());
                lyricView.setTouchable(false);
                lyricView.setClickable(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        lyricAnimation.setDuration(300);
        lyricView.setPlayable(false);
        lyricView.startAnimation(lyricAnimation);

        mStatus = Status.EXPANDED;
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
                        caculateTitleAndArtist();
                        if (TextUtils.isEmpty(MusicPlayer.getTrackName()) || TextUtils.isEmpty(MusicPlayer.getArtistName())) {
                            if (mStatus == Status.EXPANDED || mStatus == Status.FULLSCREEN) {
                                mPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                            }
                            mPanelLayout.setTouchEnabled(false);
                        } else {
                            mPanelLayout.setTouchEnabled(true);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }

}
