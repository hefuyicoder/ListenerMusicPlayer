package io.hefuyi.listener.ui.fragment;


import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.appthemeengine.ATE;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.hefuyi.listener.Constants;
import io.hefuyi.listener.ListenerApp;
import io.hefuyi.listener.MusicPlayer;
import io.hefuyi.listener.R;
import io.hefuyi.listener.injector.component.ApplicationComponent;
import io.hefuyi.listener.injector.component.ArtistInfoComponent;
import io.hefuyi.listener.injector.component.DaggerArtistInfoComponent;
import io.hefuyi.listener.injector.module.ArtistInfoModule;
import io.hefuyi.listener.mvp.contract.ArtistDetailContract;
import io.hefuyi.listener.util.ATEUtil;
import io.hefuyi.listener.util.ColorUtil;
import io.hefuyi.listener.util.DensityUtil;
import io.hefuyi.listener.util.ListenerUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistDetailFragment extends Fragment implements ArtistDetailContract.View{

    @Inject
    ArtistDetailContract.Presenter mPresenter;
    @BindView(R.id.artist_art)
    ImageView artistArt;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.app_bar)
    AppBarLayout appBarLayout;
    @BindView(R.id.fab_play)
    FloatingActionButton fabPlay;

    private ArtistMusicFragment mArtistMusicFragment;
    private long artistID = -1;
    private String artistName = "";
    private int primaryColor;

    public static ArtistDetailFragment newInstance(long id, String name, boolean useTransition, String transitionName) {
        ArtistDetailFragment fragment = new ArtistDetailFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.ARTIST_ID, id);
        args.putString(Constants.ARTIST_NAME, name);
        args.putBoolean("transition", useTransition);
        if (useTransition)
            args.putString("transition_name", transitionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injetDependences();
        mPresenter.attachView(this);
        if (getArguments() != null) {
            artistID = getArguments().getLong(Constants.ARTIST_ID);
            artistName = getArguments().getString(Constants.ARTIST_NAME);
        }

    }

    private void injetDependences() {
        ApplicationComponent applicationComponent = ((ListenerApp) getActivity().getApplication()).getApplicationComponent();
        ArtistInfoComponent artistInfoComponent = DaggerArtistInfoComponent.builder()
                .applicationComponent(applicationComponent)
                .artistInfoModule(new ArtistInfoModule())
                .build();
        artistInfoComponent.injectForFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_artist_detail, container, false);
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            root.findViewById(R.id.app_bar).setFitsSystemWindows(false);
            root.findViewById(R.id.artist_art).setFitsSystemWindows(false);
            root.findViewById(R.id.gradient).setFitsSystemWindows(false);
            Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
            CollapsingToolbarLayout.LayoutParams layoutParams = (CollapsingToolbarLayout.LayoutParams) toolbar.getLayoutParams();
            layoutParams.height += DensityUtil.getStatusBarHeight(getContext());
            toolbar.setLayoutParams(layoutParams);
            toolbar.setPadding(0, DensityUtil.getStatusBarHeight(getContext()), 0, 0);
        }
        return root;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        ATE.apply(this, ATEUtil.getATEKey(getActivity()));

        if (getArguments().getBoolean("transition")) {
            artistArt.setTransitionName(getArguments().getString("transition_name"));
        }

        setupToolbar();

        mArtistMusicFragment = ArtistMusicFragment.newInstance(artistID);
        getChildFragmentManager().beginTransaction().replace(R.id.container, mArtistMusicFragment).commit();
        mPresenter.subscribe(artistID);
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        if (primaryColor != -1 && getActivity() != null) {
            collapsingToolbarLayout.setContentScrimColor(primaryColor);
            collapsingToolbarLayout.setStatusBarScrimColor(ColorUtil.getStatusBarColor(primaryColor));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.unsubscribe();
    }

    private void setupToolbar() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout.setTitle(artistName);
    }

    @Override
    public void showArtistArt(Bitmap bitmap) {
        artistArt.setImageBitmap(bitmap);
        if (ATEUtil.getATEKey(getActivity()).equals("dark_theme")) {
            primaryColor = ATEUtil.getThemePrimaryColor(getContext());
            collapsingToolbarLayout.setContentScrimColor(primaryColor);
            collapsingToolbarLayout.setStatusBarScrimColor(ColorUtil.getStatusBarColor(primaryColor));
            return;
        }
        new Palette.Builder(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch swatch = ColorUtil.getMostPopulousSwatch(palette);
                if (swatch != null) {
                    int color = swatch.getRgb();
                    collapsingToolbarLayout.setContentScrimColor(color);
                    collapsingToolbarLayout.setStatusBarScrimColor(ColorUtil.getStatusBarColor(color));
                    primaryColor = color;
                }
            }
        });
    }

    @Override
    public void showArtistArt(Drawable drawable) {
        artistArt.setImageDrawable(drawable);
        primaryColor = ATEUtil.getThemePrimaryColor(getContext());
        collapsingToolbarLayout.setContentScrimColor(primaryColor);
        collapsingToolbarLayout.setStatusBarScrimColor(ColorUtil.getStatusBarColor(primaryColor));
    }

    @OnClick(R.id.fab_play)
    public void onFabPlayClick() {
        MusicPlayer.playAll(getActivity(), mArtistMusicFragment.mSongAdapter.getSongIds(), 0, artistID, ListenerUtil.IdType.Artist, false);
    }
}
